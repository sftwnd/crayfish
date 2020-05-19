/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json.serialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.sftwnd.crayfish.common.format.formatter.TemporalFormatter;
import com.github.sftwnd.crayfish.common.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonInstantSerializationTest {

    @Test
    void testJsonSerialization() throws IOException {
        ZonedDateTime zonedDateTime = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        JsonInstantSerializationTestDateObject obj = new JsonInstantSerializationTestDateObject(zonedDateTime, null, null);
        String json = new JsonMapper(ZoneId.systemDefault()).formatObject(obj);
        String dateStr = ISO_ZONED_DATE_TIME.format(zonedDateTime);
        assertTrue(json.contains(dateStr), "Formatted POJO to json has to contain valid dateTime value");
    }

    @Test
    void testJsonSerializationWrong() throws IOException {
        JsonInstantSerializationTestDateObject obj = new JsonInstantSerializationTestDateObject(null, null, new Date());
        assertThrows(JsonMappingException.class
                ,() -> new JsonMapper(ZoneId.systemDefault()).formatObject(obj)
                ,"JsonMapper unable to format Date as TemporalAccessor - you have to mace additional Serializer"
        );
    }

    @Test
    void testJsonSerializationOnThread() throws IOException {
        Instant instant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        JsonInstantSerializationTestDateObject obj = new JsonInstantSerializationTestDateObject(null, instant, null);
        ZoneId initialZoneId = ZoneId.of("+02:30");
        ZoneId localZoneId = ZoneId.of("+04:30");
        String dateStr = ISO_OFFSET_DATE_TIME.withZone(localZoneId).format(instant);
        String json = new JsonMapper(initialZoneId).formatObject(obj);
        assertFalse(json.contains(dateStr), "Formatted POJO to json unable to contain defined dateTime value in in zoneId.of(+02:30)");
        try {
            TemporalFormatter formatter = TemporalFormatter.formatter(JsonInstantSerializer.class);
            formatter.setCurrentZoneId(localZoneId);
            json = new JsonMapper(ZoneId.systemDefault()).formatObject(obj);
            assertTrue(json.contains(dateStr), "Formatted POJO to json has to contain defined dateTime value in zoneId.of(+04:30)");
        } finally {
            TemporalFormatter.unregister(JsonInstantSerializer.class);
        }
    }

    static class JsonZonedDateTimeSerializationTestSerializer extends JsonZonedDateTimeSerializer {
        @Override
        protected TemporalFormatter constructSerializer() {
            return new TemporalFormatter(ISO_ZONED_DATE_TIME);
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @JsonInclude
    static class JsonInstantSerializationTestDateObject {
        @JsonSerialize(using= JsonZonedDateTimeSerializationTestSerializer.class)
        private ZonedDateTime dateTime;
        @JsonSerialize(using= JsonInstantSerializer.class)
        private Instant instant;
        @JsonSerialize(using= JsonInstantSerializer.class)
        private Date wrongType;

    }

}