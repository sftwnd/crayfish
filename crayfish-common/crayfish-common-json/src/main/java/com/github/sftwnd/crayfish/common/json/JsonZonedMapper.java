/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Json Mapper with ablity to control thread and JVM time zone
 *
 * Created 2016-02-02
 *
 * @author Andrey D. Shindarev (ashindarev@gmail.com)
 * @version 1.1.1
 * @since 1.0.0
 */
public class JsonZonedMapper implements IJsonZonedMapper {

    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("UTC");

    public JsonZonedMapper() {
        this(false);
    }

    public JsonZonedMapper(boolean threadLocal) {
        if (threadLocal) {
            final ThreadLocal<Map<ZoneId, ObjectMapper>> threadLocalMap = ThreadLocal.withInitial(HashMap::new);
            this.objectMappers = threadLocalMap::get;
        } else {
            final Map<ZoneId, ObjectMapper> map = new ConcurrentHashMap<>();
            this.objectMappers = () -> map;
        }
    }

    private @Nonnull Supplier<Map<ZoneId, ObjectMapper>> objectMappers;

    public ObjectMapper getObjectMapper() {
        return getObjectMapper(null);
    }

    public ObjectMapper getObjectMapper(ZoneId zoneId) {
        return zoneId == null
             ? getObjectMapper(DEFAULT_ZONE_ID)
             : objectMappers.get().computeIfAbsent(
                      zoneId, zid -> new ObjectMapper()
                     .findAndRegisterModules()
                     .registerModule(new JavaTimeModule())
                     .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                     .configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, false)
                     .setTimeZone(TimeZone.getTimeZone(zid))
                     .setSerializationInclusion(JsonInclude.Include.NON_NULL)
               );
    }

    public void clear() {
        objectMappers.get().clear();
    }

    public void remove(ZoneId zoneId) {
        objectMappers.get().remove(zoneId == null ? DEFAULT_ZONE_ID : zoneId);
    }

    @Override
    public <T>T parseObject(ZoneId zoneId, byte[] json, Class<T> clazz) throws IOException {
        return getObjectMapper(zoneId).readerFor(clazz).readValue(json);
    }

    @Override
    public <T>T parseObject(ZoneId zoneId, byte[] json, TypeReference<T> type) throws IOException {
        return getObjectMapper(zoneId).readerFor(type).readValue(json);
    }

    public String formatObject(ZoneId zoneId, Object object) throws IOException {
        return getObjectMapper(zoneId).writeValueAsString(object);
    }

    @Override
    public String formatObject(Object object) throws IOException {
        return formatObject(null, object);
    }

}
