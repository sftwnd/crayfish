package com.github.sftwnd.crayfish.common.format.parser;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

import static com.github.sftwnd.crayfish.common.exception.ExceptionUtils.wrapUncheckedExceptions;
import static com.github.sftwnd.crayfish.common.format.parser.TemporalParser.DEFAUT_ZONE_ID;
import static com.github.sftwnd.crayfish.common.state.DefaultsHolder.ValueLevel.CURRENT;
import static com.github.sftwnd.crayfish.common.state.DefaultsHolder.ValueLevel.DEFAULT;
import static com.github.sftwnd.crayfish.common.state.DefaultsHolder.ValueLevel.SYSTEM;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class TemporalParserTest {

    private static final String dateTimeStr = "2020-01-02T10:11:11";
    private static final String dateTimeOffsetStr = "2020-01-02T10:11:11+03:00";

    @Test
    void testTemporalParserZoneId() {
        ZoneId zoneId = ZoneId.of("Europe/Kaliningrad");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(zoneId);
        TemporalParser<ZonedDateTime> parser = new TemporalParser<>(ZonedDateTime::from, zoneId);
        assertEquals(
                ZonedDateTime.from(ISO_LOCAL_DATE_TIME.withZone(zoneId).parse(dateTimeStr)),
                parser.parse(dateTimeStr), "TemporalParser::parse(dateStr) has to be equals dateTimeFormatter.parse(dateStr)"
        );
    }

    @Test
    void testDefaultZoneId() {
        ZoneId zoneId = ZoneId.of("Europe/Kaliningrad");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(zoneId);
        TemporalParser<ZonedDateTime> parser = new TemporalParser<>(ZonedDateTime::from, dateTimeFormatter);
        ZoneId newZoneId = ZoneId.of("Asia/Novosibirsk");
        parser.setDefaultZoneId(newZoneId);
        assertEquals(
                ZonedDateTime.from(ISO_LOCAL_DATE_TIME.withZone(newZoneId).parse(dateTimeStr)),
                parser.parse(dateTimeStr), "TemporalParser::parse(dateStr) has to be equals dateTimeFormatter.parse(dateStr) after set default zoneId"
        );
        parser.clearDefaultZoneId();
        assertNotEquals(
                ZonedDateTime.from(ISO_LOCAL_DATE_TIME.withZone(newZoneId).parse(dateTimeStr)),
                parser.parse(dateTimeStr), "TemporalParser::parse(dateStr) has not got to be equals dateTimeFormatter.parse(dateStr) after clear default zoneId"
        );
    }

    @Test
    void testCurrentZoneId() {
        ZoneId zoneId = ZoneId.of("Europe/Kaliningrad");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(zoneId);
        TemporalParser<ZonedDateTime> parser = new TemporalParser<>(ZonedDateTime::from, ISO_ZONED_DATE_TIME, dateTimeFormatter);
        ZoneId newZoneId = ZoneId.of("Asia/Novosibirsk");
        parser.setCurrentZoneId(newZoneId);
        assertEquals(
                ZonedDateTime.from(dateTimeFormatter.withZone(newZoneId).parse(dateTimeStr)),
                parser.parse(dateTimeStr), "TemporalParser::parse(dateStr) has to be equals dateTimeFormatter.parse(dateStr) after change current zoneId"
        );
        parser.clearCurrentZoneId();
        assertEquals(
                ZonedDateTime.from(ISO_LOCAL_DATE_TIME.withZone(zoneId).parse(dateTimeStr)),
                parser.parse(dateTimeStr), "TemporalParser::parse(dateStr) has to be equals dateTimeFormatter.parse(dateStr) with default zone id after clear current zoneId"
        );
    }

    @Test
    void testValueLevel() {
        TemporalParser<ZonedDateTime> parser = new TemporalParser<>(ZonedDateTime::from);
        parser.clearDefaultZoneId();
        assertEquals(SYSTEM, parser.getValueLevel(), "Value level for new TemporalParser without default zoneId has to be equals SYSTEM");
        parser.setDefaultZoneId(ZoneId.of("+04:00"));
        assertEquals(DEFAULT, parser.getValueLevel(), "Value level for new TemporalParser with default zoneId has to be equals DEFAULT");
        parser.setCurrentZoneId(ZoneId.of("Asia/Novosibirsk"));
        assertEquals(CURRENT, parser.getValueLevel(), "Value level for new TemporalParser has to be equals DEFAULT");
    }

    @Test
    void testParse() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.of("Europe/Moscow"));
        TemporalParser<ZonedDateTime> parser = new TemporalParser<>(ZonedDateTime::from, dateTimeFormatter);
        assertNull(parser.parse(null), "TemporalParser::parse(null) has to be null");
        assertNull(parser.parse(" "), "TemporalParser::parse(blank) has to be null");
        assertEquals(ZoneId.of("+03:00"), parser.parse(dateTimeOffsetStr).getZone(), "TemporalParser::parse(strWithOffset) has to return defined in str offset");
        assertEquals(
                ZonedDateTime.from(ISO_LOCAL_DATE_TIME.withZone(ZoneId.of("Europe/Moscow")).parse(dateTimeStr)),
                parser.parse(dateTimeStr), "TemporalParser::parse(dateStr) has to be equals dateTimeFormatter.parse(dateStr)"
        );
        assertNotEquals(
                ZonedDateTime.from(ISO_LOCAL_DATE_TIME.withZone(ZoneId.of("Europe/Kaliningrad")).parse(dateTimeStr)),
                parser.parse(dateTimeStr), "TemporalParser::parse(dateStr) has to be equals dateTimeFormatter.parse(dateStr)"
        );
    }

    @Test
    void testParseZoneId() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.of("Europe/Moscow"));
        TemporalParser<ZonedDateTime> parser = new TemporalParser<>(ZonedDateTime::from, dateTimeFormatter);
        assertNull(parser.parse(null, DEFAUT_ZONE_ID), "TemporalParser::parse(null, zoneId) has to be null");
        assertNull(parser.parse(" ", DEFAUT_ZONE_ID), "TemporalParser::parse(blank, zoneId) has to be null");
        ZoneId parseZoneId = ZoneId.of("Europe/Kaliningrad");
        assertEquals(
                ZonedDateTime.from(ISO_LOCAL_DATE_TIME.withZone(parseZoneId).parse(dateTimeStr)),
                parser.parse(dateTimeStr, parseZoneId), "TemporalParser::parse(dateStr) has to be equals dateTimeFormatter.parse(dateStr)"
        );
        ZoneId currentZoneId = ZoneId.of("Europe/Moscow");
        parser.setCurrentZoneId(currentZoneId);
        assertEquals(
                ZonedDateTime.from(ISO_LOCAL_DATE_TIME.withZone(currentZoneId).parse(dateTimeStr)),
                parser.parse(dateTimeStr, currentZoneId), "TemporalParser::parse(dateStr) has to be equals dateTimeFormatter.parse(dateStr)"
        );

    }

    @Test
    void testRegisterParserUnregister() {
        TemporalParser<TemporalAccessor> parser = new TemporalParser<>(Function.identity());
        Object obj = new Object();
        TemporalParser.register(obj, () -> parser);
        try {
            assertSame(parser, TemporalParser.parser(obj), "TemporalParser::parser(obj) has return registered parser");
        } finally {
            TemporalParser.unregister(obj);
        }
        assertNull(TemporalParser.parser(obj), "TemporalParser::parser(obj) has return null after unregister parser");

    }

    @Test
    void testDateTimeFormatter() throws NoSuchMethodException {
        TemporalParser<TemporalAccessor> parser = new TemporalParser<>(Function.identity());
        Method dateTimeFormatterMethod = TemporalParser.class.getDeclaredMethod("dateTimeFormatter", DateTimeFormatter.class);
        Function<DateTimeFormatter, DateTimeFormatter> dateTimeFormatter = dtf -> (DateTimeFormatter)(wrapUncheckedExceptions(() -> dateTimeFormatterMethod.invoke(parser, dtf)));
        assertNotNull(dateTimeFormatter.apply(null), "TemporalParser::dateTimeFormatter unable to return null");
        assertEquals(DEFAUT_ZONE_ID, dateTimeFormatter.apply(null).getZone(), "TemporalParser::dateTimeFormatter(null) has to return formatter with DEFAUT_ZONE_ID");
        assertEquals(DEFAUT_ZONE_ID, dateTimeFormatter.apply(ISO_LOCAL_DATE_TIME.withZone(null)).getZone(), "TemporalParser::dateTimeFormatter(formatter with nulled zone) has to return formatter with DEFAUT_ZONE_ID");
        ZoneId zoneId = ZoneId.of("Europe/Moscow");
        assertEquals(zoneId, dateTimeFormatter.apply(ISO_LOCAL_DATE_TIME.withZone(zoneId)).getZone(), "TemporalParser::dateTimeFormatter(formatter with zoneId) has to return formatter with same zoneId");
    }

}