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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JsonInstantSerializationTest {
/*
    @Test
    public void testExtendedSerialize() throws IOException {
        Instant instant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        final ObjectMapper mapper = new ObjectMapper();
        // Create object
        JsonInstantSerializationTestDateObject obj = new JsonInstantSerializationTestDateObject(instant);
        // Serialize object to string (date save timezone)
        String json = mapper.writerFor(JsonInstantSerializationTestDateObject.class).writeValueAsString(obj);
        // Deserialize object from string (date have to save timezone)
        obj = mapper.readerFor(JsonInstantSerializationTestDateObject.class).readValue(json);
        assertEquals(instant, obj.getInstant(), "Instant after reserialization have to be the same");
        obj.setInstant(null);
        json = mapper.writerFor(JsonInstantSerializationTestDateObject.class).writeValueAsString(obj);
        obj = mapper.readerFor(JsonInstantSerializationTestDateObject.class).readValue(json);
        assertNull(obj.getInstant(),"Instant have to be null after reserialization of object with null value");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    static class JsonInstantSerializationTestDateObject {
        @JsonSerialize(using=JsonInstantSerializer.class)
        @JsonDeserialize(using=JsonInstantDeserializer.class)
        private Instant instant;
    }
*/
}