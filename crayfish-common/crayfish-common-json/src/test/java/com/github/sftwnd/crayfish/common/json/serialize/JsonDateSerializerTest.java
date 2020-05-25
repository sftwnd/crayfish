/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json.serialize;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.sftwnd.crayfish.common.format.formatter.TemporalFormatter;
import com.github.sftwnd.crayfish.common.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Date;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by ashindarev on 09.02.16.
 */
class JsonDateSerializerTest {

    @Test
    void testJsonSerialization() throws IOException {
        Date date = new Date();
        JsonDateSerializerTestDateObject obj = new JsonDateSerializerTestDateObject(date);
        String json = new JsonMapper(TemporalFormatter.DEFAUT_ZONE_ID).formatObject(obj);
        String dateStr = ISO_OFFSET_DATE_TIME.withZone(TemporalFormatter.DEFAUT_ZONE_ID).format(date.toInstant());
        assertTrue(json.contains(dateStr), "Formatted POJO to json has to contain valid dateTime value");
    }

    @AllArgsConstructor
    @Data
    static class JsonDateSerializerTestDateObject {
        @JsonSerialize(using= JsonDateSerializer.class)
        private Date date;

    }

}
