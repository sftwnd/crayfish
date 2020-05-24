package com.github.sftwnd.crayfish.common.format.parser;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;

import static com.github.sftwnd.crayfish.common.format.parser.TemporalParser.DEFAUT_ZONE_ID;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TemporalAccessorParserTest {

    private static final String parseStr = "2020-03-12T10:11:12";

    @Test
    void testTemporalAccessorParser() {
        TemporalAccessorParser parser = new TemporalAccessorParser();
        assertEquals(parse(parseStr, DEFAUT_ZONE_ID), ZonedDateTime.from(parser.parse(parseStr)), "TemporalAccessorParser.parse(str without zoneId) has to parse in DEFAUT_ZONE_ID");
    }

    @Test
    void testTemporalAccessorParserZoneId() {
        ZoneId zoneId = ZoneId.of("Europe/Moscow");
        TemporalAccessorParser parser = new TemporalAccessorParser(zoneId);
                assertEquals(parse(parseStr, zoneId), ZonedDateTime.from(parser.parse(parseStr)),
                "TemporalAccessorParser.parse(str without zoneId) has to parse in parser zoneId");
    }

    @SneakyThrows
    private static final TemporalAccessor parse(String str, ZoneId zoneId) {
        return ZonedDateTime.from(ISO_LOCAL_DATE_TIME.withZone(zoneId).parse(str));
    }

}