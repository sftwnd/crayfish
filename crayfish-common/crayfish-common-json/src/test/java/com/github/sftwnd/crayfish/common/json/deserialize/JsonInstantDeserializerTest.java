/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json.deserialize;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.sftwnd.crayfish.common.format.parser.TemporalParser;
import com.github.sftwnd.crayfish.common.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.TimeZone;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by ashindarev on 29.02.16.
 */
class JsonInstantDeserializerTest {

    @Test
    void testJsonZonedDeserializerOfNull() throws IOException {
        assertNull(
                new JsonMapper().parseObject("{\"instant\":null}", JsonInstantDeserializerTestDataObject.class).instant
                ,"JsonMapper has to format object with nullable instant from string by JsonInstantDeserializer with null instant value"
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void testJsonZonedDeserializer() throws IOException {
        String dateStr = "2020-01-11T11:33:55";
        assertEquals(
                Instant.from(ISO_LOCAL_DATE_TIME.withZone(ZoneId.of("UTC")).parse(dateStr))
                ,new JsonMapper().parseObject("{\"instant\":\""+dateStr+"\"}", JsonInstantDeserializerTestDataObject.class).instant
                ,"JsonMapper has to format object with instant from string by JsonInstantDeserializer with right value"
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void testJsonZonedDeserializerInThreadZoneId() throws IOException {
        String dateStr = "2020-01-11T11:33:55";
        TemporalParser<?> parser = TemporalParser.parser(JsonInstantDeserializer.class);
        try {
            ZoneId zoneId = ZoneId.of("+07:00");
            parser.setCurrentZoneId(zoneId);
            assertEquals(
                    Instant.from(ISO_LOCAL_DATE_TIME.withZone(zoneId).parse(dateStr))
                    , new JsonMapper().parseObject("{\"instant\":\"" + dateStr + "\"}", JsonInstantDeserializerTestDataObject.class).instant
                    , "JsonMapper has to format object with instant from string by JsonInstantDeserializer with right value in thread-defined timeZone"
            );
        } finally {
            parser.clearCurrentZoneId();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void testJsonZonedDeserializerInObjectMapperZoneId() throws IOException {
        String dateStr = "2020-01-11T11:33:55";
        TemporalParser<?> parser = TemporalParser.parser(JsonInstantDeserializer.class);
        JsonMapper jsonMapper = new JsonMapper();
        try {
            ZoneId zoneId = ZoneId.of("+07:00");
            jsonMapper.getObjectMapper().setTimeZone(TimeZone.getTimeZone(zoneId));
            assertEquals(
                    Instant.from(ISO_LOCAL_DATE_TIME.withZone(zoneId).parse(dateStr))
                    , jsonMapper.parseObject("{\"instant\":\"" + dateStr + "\"}", JsonInstantDeserializerTestDataObject.class).instant
                    , "JsonMapper has to format object with instant from string by JsonInstantDeserializer with right value in thread-defined timeZone"
            );
        } finally {
            jsonMapper.getObjectMapper().setTimeZone(TimeZone.getTimeZone("UTC"));
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    static class JsonInstantDeserializerTestDataObject {
        @JsonDeserialize(using = JsonInstantDeserializer.class)
        Instant instant;
    }

}