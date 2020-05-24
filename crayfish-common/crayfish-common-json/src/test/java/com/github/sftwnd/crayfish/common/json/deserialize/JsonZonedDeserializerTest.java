/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json.deserialize;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.sftwnd.crayfish.common.format.parser.InstantParser;
import com.github.sftwnd.crayfish.common.format.parser.TemporalParser;
import com.github.sftwnd.crayfish.common.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by ashindarev on 29.02.16.
 */
class JsonZonedDeserializerTest {

    private static final String dateStr = "2020-01-11T11:33:55";

    @Test
    @SuppressWarnings("unchecked")
    void testJsonZonedDeserializerNullValue() throws IOException {
        assertNull(
                new JsonMapper().parseObject("{\"instant\":null}", JsonZonedDeserializerTestDataObject.class).instant
                ,"JsonMapper has to format object with nullable instant from string by JsonInstantDeserializer with null instant value"
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void testJsonZonedDeserializerDefault() throws IOException {
        assertEquals(
                Instant.from(ISO_LOCAL_DATE_TIME.withZone(ZoneId.of("UTC")).parse(dateStr))
                ,new JsonMapper().parseObject("{\"instant\":\""+dateStr+"\"}", JsonZonedDeserializerTestDataObject.class).instant
                ,"JsonMapper has to format object with instant from string by JsonInstantDeserializer with right value"
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void testJsonZonedDeserializerCurrent() throws IOException {
        ZoneId zoneId = ZoneId.of("+07:00");
        JsonInstantDeserializerTestDeserializer c = new JsonInstantDeserializerTestDeserializer();
        try {
            TemporalParser.register(JsonInstantDeserializerTestDeserializer.class, null).setCurrentZoneId(zoneId);
            assertEquals(
                    Instant.from(ISO_LOCAL_DATE_TIME.withZone(zoneId).parse(dateStr))
                    , new JsonMapper().parseObject("{\"instant\":\"" + dateStr + "\"}", JsonZonedDeserializerTestDataObject.class).instant
                    , "JsonMapper has to format object with instant from string by JsonInstantDeserializer with right value"
            );
        } finally {
            TemporalParser.unregister(JsonInstantDeserializerTestDeserializer.class);
        }
    }

    static class JsonInstantDeserializerTestDeserializer extends JsonZonedDeserializer<Instant> {
        @Override
        public TemporalParser<Instant> constructParser() {
            return new InstantParser();
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    static class JsonZonedDeserializerTestDataObject {
        @JsonDeserialize(using = JsonInstantDeserializerTestDeserializer.class)
        Instant instant;
    }

}