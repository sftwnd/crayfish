package com.github.sftwnd.crayfish.common.format.parser;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

import static com.github.sftwnd.crayfish.common.format.parser.TemporalParser.DEFAUT_ZONE_ID;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DateParserTest {

    private static final String parseStr = "2020-01-12T10:11:12";

    @Test
    void testDateParser() {
        DateParser parser = new DateParser();
        assertNull(parser.parse(null), "DateParser.parse(null) has to be null in DEFAUT_ZONE_ID");
        assertEquals(parse(parseStr, DEFAUT_ZONE_ID), parser.parse(parseStr), "DateParser.parse(str without zoneId) has to parse in DEFAUT_ZONE_ID");
    }

    @Test
    void testDateParserZoneId() {
        ZoneId zoneId = ZoneId.of("Europe/Moscow");
        DateParser parser = new DateParser(zoneId);
                assertEquals(parse(parseStr, zoneId), parser.parse(parseStr),
                "DateParser.parse(str without zoneId) has to parse in parser zoneId");
    }

    private static final Date parse(String str, ZoneId zoneId) {
        return Date.from(Instant.from(ISO_LOCAL_DATE_TIME.withZone(zoneId).parse(str)));
    }

}