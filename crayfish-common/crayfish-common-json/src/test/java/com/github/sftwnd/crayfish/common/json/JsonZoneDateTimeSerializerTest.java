/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JsonZoneDateTimeSerializerTest {
/*
    @Test
    public void testExtendedSerialize() throws IOException {
        String zoneId = "Asia/Novosibirsk";
        Instant instant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.of(zoneId));
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        // Create object
        JsonZoneDateTimeSerializerDateObject obj = new JsonZoneDateTimeSerializerDateObject(zdt);
        // Serialize object to string (date save timezone)
        String json = mapper.writerFor(JsonZoneDateTimeSerializerDateObject.class).writeValueAsString(obj);
        ZonedDateTimeFormatter1.formatter(JsonTemporalAccessorSerializer.class).setCurrentZoneId(ZoneId.of("Europe/Moscow"));
        String json1 = mapper.writerFor(JsonZoneDateTimeSerializerDateObject.class).writeValueAsString(obj);
        ZonedDateTimeFormatter1.formatter(JsonTemporalAccessorSerializer.class).clearCurrentZoneId();
        json = mapper.writerFor(JsonZoneDateTimeSerializerDateObject.class).writeValueAsString(obj);
// Deserialize object from string (date have to save timezone)
        obj = mapper.readerFor(JsonZoneDateTimeSerializerDateObject.class).readValue(json);
        assertEquals(zdt.toInstant(), obj.dateTime.toInstant(), "Local date after reserialization have to be the same");
        obj.setDateTime(null);
        json = mapper.writerFor(JsonZoneDateTimeSerializerDateObject.class).writeValueAsString(obj);
        obj = mapper.readerFor(JsonZoneDateTimeSerializerDateObject.class).readValue(json);
        assertNull(obj.dateTime,"Date have to be null after reserialization of object with null value");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    static class JsonZoneDateTimeSerializerDateObject {
        @JsonSerialize(using= JsonTemporalAccessorSerializer.class)
        private ZonedDateTime dateTime;
    }
*/
}