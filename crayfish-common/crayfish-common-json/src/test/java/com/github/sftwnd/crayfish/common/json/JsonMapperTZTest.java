/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.sftwnd.crayfish.common.format.DateSerializeUtility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class JsonMapperTZTest {

    @Test
    void testClearConstruct() {
        TimeZone[] timeZones = new TimeZone[] { TimeZone.getTimeZone("Asia/Novosibirsk"), TimeZone.getTimeZone("Europe/Moscow") };
        ObjectMapper[] oms = new ObjectMapper[timeZones.length];
        for (int i=0; i<timeZones.length; i++) {
            ObjectMapper objectMapper = JsonMapperTZ.getObjectMapper(timeZones[i]);
            assertNotNull(objectMapper, "JsonMapperTZ.getObjectMapper(TimeZone) has not got to be null");
            assertSame(objectMapper, JsonMapperTZ.getObjectMapper(timeZones[i]), "JsonMapperTZ.getObjectMapper(TimeZone) has return same value evey time");
            JsonMapperTZ.remove(timeZones[i]);
            assertNotSame(objectMapper, JsonMapperTZ.getObjectMapper(timeZones[i]), "JsonMapperTZ.getObjectMapper(TimeZone) has return other value after delete(TimeZone)");
            oms[i] = JsonMapperTZ.getObjectMapper(timeZones[i]);
        }
        JsonMapperTZ.clear();
        for (int i=0; i<timeZones.length; i++) {
            assertNotSame(oms[i], JsonMapperTZ.getObjectMapper(timeZones[i]), "JsonMapperTZ.getObjectMapper(TimeZone) has return other value after clear()");
        }
        JsonMapperTZ.clear();
    }

    @Test
    void testParseObjectTZ() throws IOException, ParseException {
        TimeZone mskZone = TimeZone.getTimeZone("Europe/Moscow");
        TimeZone prsZone = TimeZone.getTimeZone("Asia/Novosibirsk");
        String dateTime = "2020-03-04T13:05:06";
        Instant mskDate = DateSerializeUtility.getDateSerializeUtility(mskZone, "yyyy-MM-dd'T'HH:mm:ss").deserialize(dateTime).toInstant();
        Instant prsDate = DateSerializeUtility.getDateSerializeUtility(prsZone, "yyyy-MM-dd'T'HH:mm:ss").deserialize(dateTime).toInstant();
        assertNotEquals(mskDate, prsDate,"Unable to process testParseObjectTZ() because DateSerializeUtility.getDateSerializeUtility result is wrong");
        JsonMapperTZTestDataObject prsDataObject = JsonMapperTZ.parseObject( prsZone, "{\"instant\":\""+dateTime+"\"}", JsonMapperTZTestDataObject.class);
        assertEquals(prsDate, prsDataObject.getInstant(), "Checked instant and parsed object instant has to be equals for the timeZone: "+prsZone.getID());
        JsonMapperTZTestDataObject mskDataObject = JsonMapperTZ.parseObject( mskZone, "{\"instant\":\""+dateTime+"\"}", JsonMapperTZTestDataObject.class);
        assertEquals(mskDate, mskDataObject.getInstant(), "Checked instant and parsed object instant has to be equals for the timeZone: "+mskZone.getID());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class JsonMapperTZTestDataObject {
        @JsonSerialize(using=JsonInstantSerializer.class)
        @JsonDeserialize(using=JsonInstantDeserializer.class)
        private Instant instant;
    }


}