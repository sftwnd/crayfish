/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.sftwnd.crayfish.common.json.deserialize.JsonInstantDeserializer;
import com.github.sftwnd.crayfish.common.json.serialize.JsonInstantSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JsonZonedMapperTest {

    @Test
    @SneakyThrows
    void testClearConstruct() {
        JsonZonedMapper mapper = new JsonZonedMapper();
        for (ZoneId zoneId: new ZoneId[]{null, ZoneId.systemDefault()}) {
            Object objectMapper = Executors.newSingleThreadExecutor().submit(() -> getObjectMapper(mapper, zoneId)).get();
            assertSame(objectMapper, getObjectMapper(mapper, zoneId), "JsonZonedMapper has got the same ObjectMapper for zoneId: '"+zoneId+"' on different threads");
        }
    }

    @Test
    @SneakyThrows
    void testFalseConstruct() {
        JsonZonedMapper mapper = new JsonZonedMapper(false);
        for (ZoneId zoneId: new ZoneId[]{null, ZoneId.systemDefault()}) {
            Object objectMapper = Executors.newSingleThreadExecutor().submit(() -> getObjectMapper(mapper, zoneId)).get();
            assertSame(objectMapper, getObjectMapper(mapper, zoneId), "JsonZonedMapper(false) has got the same ObjectMapper for zoneId: '"+zoneId+"' on different threads");
        }
    }

    @Test
    @SneakyThrows
    void testTrueConstruct() {
        JsonZonedMapper mapper = new JsonZonedMapper(true);
        for (ZoneId zoneId: new ZoneId[]{null, ZoneId.systemDefault()}) {
            Object objectMapper = Executors.newSingleThreadExecutor().submit(() -> getObjectMapper(mapper, zoneId)).get();
            assertNotSame(objectMapper, getObjectMapper(mapper, zoneId), "JsonZonedMapper(false) has got not the same ObjectMapper for zoneId: '"+zoneId+"' on different threads");
        }
    }

    @Test
    @SneakyThrows
    void testRemoveZoneId() {
        for (ZoneId zoneId: new ZoneId[] {null, ZoneId.systemDefault(), JsonZonedMapper.DEFAULT_ZONE_ID}) {
            for (Boolean param : new Boolean[]{null, false, true}) {
                String name = "JsonZonedMapper(" + (param == null ? "" : param) + ")";
                JsonZonedMapper mapper = Optional.ofNullable(param).map(prm -> new JsonZonedMapper(prm)).orElseGet(JsonZonedMapper::new);
                Object objectMapper = getObjectMapper(mapper, zoneId);
                assertSame(objectMapper, getObjectMapper(mapper, zoneId), name + " has got the same ObjectMapper for zoneId: '" + zoneId + "'");
                mapper.remove(zoneId);
                assertNotSame(objectMapper, getObjectMapper(mapper, zoneId), name + " has got different ObjectMapper for zoneId: '" + zoneId + "' after call remove(zoneId)");
            }
        }
    }

    @Test
    @SneakyThrows
    void testClear() {
        for (Boolean param : new Boolean[] {null, false, true}) {
            String name = "JsonZonedMapper("+(param == null ? "" : param)+")";
            JsonZonedMapper mapper = Optional.ofNullable(param).map(prm -> new JsonZonedMapper(prm)).orElseGet(JsonZonedMapper::new);
            Map<ZoneId, ObjectMapper> map = ZoneId.getAvailableZoneIds().stream().limit(20).map(ZoneId::of).collect(
                    Collectors.toMap(z -> z, z -> getObjectMapper(mapper, z))
            );
            map.entrySet().forEach(
                    e -> assertSame(e.getValue(), getObjectMapper(mapper, e.getKey()), name + " - mapper must be the same")
            );
            mapper.clear();
            map.entrySet().forEach(
                    e -> assertNotSame(e.getValue(), getObjectMapper(mapper, e.getKey()), name + " - mapper must return different value from saved")
            );
        }
    }

    @Test
    void testParseObjectClass() throws IOException {
        JsonZonedMapper mapper = new JsonZonedMapper();
        String date = "2020-03-05T11:22:33";
        for (ZoneId zoneId: new ZoneId[] {null, ZoneId.systemDefault(), JsonZonedMapper.DEFAULT_ZONE_ID}) {
            Instant instant = Instant.from(ISO_LOCAL_DATE_TIME.withZone(Optional.ofNullable(zoneId).orElse(JsonZonedMapper.DEFAULT_ZONE_ID)).parse(date));
            assertEquals(instant, mapper.parseObject(zoneId, "{\"instant\":\""+date+"\"}", JsonZonedMapperDataObject.class).instant);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void testParseObjectTypeReference() throws IOException {
        JsonZonedMapper mapper = new JsonZonedMapper();
        String date = "2020-03-05T11:22:33";
        TypeReference<JsonZonedMapperDataObject> typeReference = mock(TypeReference.class);
        when(typeReference.getType()).thenReturn(JsonZonedMapperDataObject.class);
        for (ZoneId zoneId: new ZoneId[] {null, ZoneId.systemDefault(), JsonZonedMapper.DEFAULT_ZONE_ID}) {
            Instant instant = Instant.from(ISO_LOCAL_DATE_TIME.withZone(Optional.ofNullable(zoneId).orElse(JsonZonedMapper.DEFAULT_ZONE_ID)).parse(date));
            assertEquals(instant, mapper.parseObject(zoneId, "{\"instant\":\""+date+"\"}", typeReference).instant);
        }
    }

    @Test
    void testSerializeObject() throws IOException {
        JsonZonedMapper mapper = new JsonZonedMapper();
        String date = "2020-03-05T11:22:33";
        for (ZoneId zoneId: new ZoneId[] {null, ZoneId.systemDefault(), JsonZonedMapper.DEFAULT_ZONE_ID}) {
            ZoneId realZoneId = Optional.ofNullable(zoneId).orElse(JsonZonedMapper.DEFAULT_ZONE_ID);
            Instant checkInstant = Instant.from(ISO_LOCAL_DATE_TIME.withZone(realZoneId).parse(date));
            String controlStr = ISO_OFFSET_DATE_TIME.withZone(JsonZonedMapper.DEFAULT_ZONE_ID).format(checkInstant);
            JsonZonedMapperDataObject object = new JsonZonedMapperDataObject(checkInstant);
            assertTrue(mapper.formatObject(object).contains(controlStr));
        }
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private static final ObjectMapper getObjectMapper(JsonZonedMapper mapper, ZoneId zoneId) {
        Method method = JsonZonedMapper.class.getDeclaredMethod("getObjectMapper", ZoneId.class);
        method.setAccessible(true);
        return (ObjectMapper) method.invoke(mapper, zoneId);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class JsonZonedMapperDataObject {
        @JsonDeserialize(using = JsonInstantDeserializer.class)
        @JsonSerialize(using = JsonInstantSerializer.class)
        private Instant instant;
    }

}