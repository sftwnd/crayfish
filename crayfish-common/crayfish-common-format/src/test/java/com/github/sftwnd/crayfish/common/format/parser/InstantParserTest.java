package com.github.sftwnd.crayfish.common.format.parser;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;

import static com.github.sftwnd.crayfish.common.format.parser.TemporalParser.DEFAUT_ZONE_ID;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InstantParserTest {

    private static final String parseStr = "2020-02-12T10:11:12";

    @Test
    void testInstantParser() {
        InstantParser parser = new InstantParser();
        assertEquals(parse(parseStr, DEFAUT_ZONE_ID), parser.parse(parseStr), "InstantParser.parse(str without zoneId) has to parse in DEFAUT_ZONE_ID");
    }

    @Test
    void testInstantParserZoneId() {
        ZoneId zoneId = ZoneId.of("Europe/Moscow");
        InstantParser parser = new InstantParser(zoneId);
                assertEquals(parse(parseStr, zoneId), parser.parse(parseStr),
                "InstantParser.parse(str without zoneId) has to parse in parser zoneId");
    }

    private static final Instant parse(String str, ZoneId zoneId) {
        return Instant.from(ISO_LOCAL_DATE_TIME.withZone(zoneId).parse(str));
    }

}