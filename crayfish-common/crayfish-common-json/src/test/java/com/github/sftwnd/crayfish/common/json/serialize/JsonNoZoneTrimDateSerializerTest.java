/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json.serialize;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.sftwnd.crayfish.common.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by ashindarev on 09.02.16.
 */
class JsonNoZoneTrimDateSerializerTest {

    @Test
    void testJsonSerialization() throws IOException {
        Date date = Date.from(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        JsonNoZoneDateSerializerTestDataObject obj = new JsonNoZoneDateSerializerTestDataObject(date);
        for (ZoneId zoneId : new ZoneId[] { ZoneId.of("+03:00"), ZoneId.of("+07:30"), ZoneId.of("UTC"), ZoneId.systemDefault() }) {
            String json = new JsonMapper(zoneId).formatObject(obj);
            String dateStr = '"'+ISO_LOCAL_DATE_TIME.withZone(zoneId).format(date.toInstant())+'"';
            assertTrue(json.contains(dateStr), "Formatted POJO to json has to contain valid dateTime value");
        }
    }

    @AllArgsConstructor
    @Data
    static class JsonNoZoneDateSerializerTestDataObject {
        @JsonSerialize(using= JsonNoZoneTrimDateSerializer.class)
        private Date date;
    }

}
