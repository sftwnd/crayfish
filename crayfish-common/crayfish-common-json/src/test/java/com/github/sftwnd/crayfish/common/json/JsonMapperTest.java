/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.sftwnd.crayfish.common.json.deserialize.JsonInstantDeserializer;
import com.github.sftwnd.crayfish.common.json.serialize.JsonInstantSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JsonMapperTest {

    @Test
    void testParseObjectClass() throws IOException {
        JsonMapper mapper = new JsonMapper();
        String date = "2020-04-05T11:21:31";
        Instant instant = Instant.from(ISO_LOCAL_DATE_TIME.withZone(JsonMapper.DEFAULT_ZONE_ID).parse(date));
        assertEquals(instant, mapper.parseObject("{\"instant\":\""+date+"\"}", JsonMapperDataObject.class).instant);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testParseObjectTypeReference() throws IOException {
        JsonMapper mapper = new JsonMapper();
        String date = "2020-03-05T11:22:33";
        TypeReference<JsonMapperDataObject> typeReference = mock(TypeReference.class);
        when(typeReference.getType()).thenReturn(JsonMapperDataObject.class);
        Instant instant = Instant.from(ISO_LOCAL_DATE_TIME.withZone(JsonMapper.DEFAULT_ZONE_ID).parse(date));
        assertEquals(instant, mapper.parseObject("{\"instant\":\""+date+"\"}", typeReference).instant);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class JsonMapperDataObject {
        @JsonDeserialize(using = JsonInstantDeserializer.class)
        @JsonSerialize(using = JsonInstantSerializer.class)
        private Instant instant;
    }

}