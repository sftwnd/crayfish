/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Created by ashindarev on 09.02.16.
 */
public class JsonDateSerializerTest {
/*
    @Test
    public void testExtendedSerialize() throws IOException {
        Date minutesDate  = Date.from(Instant.now().truncatedTo(ChronoUnit.MINUTES));
        Date secondsDate  = Date.from(minutesDate.toInstant().plus(17, ChronoUnit.SECONDS));
        Date originalDate = Date.from(secondsDate.toInstant().plus(37, ChronoUnit.MILLIS));
        final ObjectMapper mapper = new ObjectMapper();
        JsonDateSerializerTestDateObject obj1 = new JsonDateSerializerTestDateObject(originalDate, minutesDate);
        String str1= mapper.writeValueAsString(obj1);
        JsonDateSerializerTestDateObject obj2 = mapper.readerFor(JsonDateSerializerTestDateObject.class).readValue(str1);
        assertEquals(secondsDate, obj2.getDate(), "POJO().date after reserialization has to be equals of original date truncted to seconds");
        assertNotEquals(originalDate, obj2.getDate(), "POJO().date after reserialization has not to be equals of original nontrancated date");
        assertEquals(minutesDate, obj2.getMinutesDate(), "POJO().minutesDate after reserialization has to be equals value truncted to minutes");
        assertNotEquals(secondsDate, obj2.getMinutesDate(), "POJO().minutesDate after reserialization has to be truncted to minutes");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    static class JsonDateSerializerTestDateObject {
        @JsonSerialize(using=JsonDateSerializer.class)
        @JsonDeserialize(using=JsonDateDeserializer.class)
        private Date date;
        @JsonSerialize(using=JsonDateTestSerializer.class)
        @JsonDeserialize(using=JsonDateDeserializer.class)
        private Date minutesDate;
    }

    static class JsonDateTestSerializer extends JsonDateSerializer {
        public JsonDateTestSerializer() {
            super();
        }
        protected DateSerializeUtility getSerializeUtility() {
            return DateSerializeUtility.getDateSerializeUtility(TimeZone.getTimeZone("UTC"), "yyyy-MM-dd'T'HH:mmXXX");
        }
    }
*/
}
