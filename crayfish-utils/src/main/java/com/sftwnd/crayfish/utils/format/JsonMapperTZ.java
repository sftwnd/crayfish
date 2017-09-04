package com.sftwnd.crayfish.utils.format;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by ashindarev on 02.02.16.
 */
public final class JsonMapperTZ {

    public static final TimeZone currentTimeZone = Calendar.getInstance().getTimeZone();

    private static ThreadLocal<Map<String, ObjectMapper>> objectMappers = new ThreadLocal<Map<String, ObjectMapper>>() {
        @Override
        protected Map<String, ObjectMapper> initialValue() {
             return new HashMap<>();
        }
    };

    public static ObjectMapper getObjectMapper(TimeZone timeZone) {
        Map<String, ObjectMapper> mappers = objectMappers.get();
        if (mappers.containsKey(timeZone.getID())) {
            return mappers.get(timeZone.getID());
        } else {
            synchronized (mappers) {
                if (mappers.containsKey(timeZone.getID())) {
                    return getObjectMapper(timeZone);
                }
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                mapper.configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, false);
                mapper.setTimeZone(timeZone);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                mappers.put(timeZone.getID(), mapper);
                return mapper;
            }
        }
    }

    public static <T>T parseObject(TimeZone timeZone, String json, Class<T> clazz) throws IOException {
        return getObjectMapper(timeZone).readerFor(clazz).readValue(json);
    }

    public static <T>T parseObject(String json, Class<T> clazz) throws IOException {
        return parseObject(currentTimeZone, json, clazz);
    }

    public static <T>T parseObject(TimeZone timeZone, byte[] json, Class<T> clazz) throws IOException {
        return getObjectMapper(timeZone).readerFor(clazz).readValue(json);
    }

    public static <T>T parseObject(byte[] json, Class<T> clazz) throws IOException {
        return parseObject(currentTimeZone, json, clazz);
    }

    public static String serializeObject(TimeZone timeZone, Object object) throws IOException {
        return getObjectMapper(timeZone).writeValueAsString(object);
    }

    public static String serializeObject(Object object) throws IOException {
        return serializeObject(currentTimeZone, object);
    }

}
