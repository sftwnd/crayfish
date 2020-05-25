package com.github.sftwnd.crayfish.common.format.parser;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.github.sftwnd.crayfish.common.format.parser.TemporalParser.DEFAUT_ZONE_ID;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ZonedDateTimeParserTest {

    private static final String parseStr = "2020-04-12T10:11:12";

    @Test
    void testZonedDateTimeParser() {
        ZonedDateTimeParser parser = new ZonedDateTimeParser();
        assertEquals(parse(parseStr, DEFAUT_ZONE_ID), parser.parse(parseStr), "ZonedDateTimeParser.parse(str without zoneId) has to parse in DEFAUT_ZONE_ID");
    }

    @Test
    void testZonedDateTimeParserZoneId() {
        ZoneId zoneId = ZoneId.of("Europe/Moscow");
        ZonedDateTimeParser parser = new ZonedDateTimeParser(zoneId);
                assertEquals(parse(parseStr, zoneId), parser.parse(parseStr),
                "ZonedDateTimeParser.parse(str without zoneId) has to parse in parser zoneId");
    }

    private static final ZonedDateTime parse(String str, ZoneId zoneId) {
        return ZonedDateTime.from(ISO_LOCAL_DATE_TIME.withZone(zoneId).parse(str));
    }

}