package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by ashindarev on 02.02.16.
 */
public final class JsonMapper {

    private JsonMapper() {
        super();
    }

    public static void clear() {
        JsonMapperTZ.remove(null);
    }

    public static <T>T parseObject(String json, Class<T> clazz) throws IOException {
        return JsonMapperTZ.parseObject(null, json, clazz);
    }

    public static <T>T parseObject(byte[] json, Class<T> clazz) throws IOException {
        return JsonMapperTZ.parseObject(null, json, clazz);
    }

    public static <T>T parseObject(String json, TypeReference<T> type) throws IOException {
        return JsonMapperTZ.parseObject(null, json, type);
    }

    public static <T>T parseObject(byte[] json, TypeReference<T> type) throws IOException {
        return JsonMapperTZ.parseObject(null, json, type);
    }

    public static String serializeObject(Object object) throws IOException {
        return JsonMapperTZ.serializeObject(null, object);
    }
    public static ObjectMapper getObjectMapper() {
        return JsonMapperTZ.getObjectMapper(null);
    }

}
