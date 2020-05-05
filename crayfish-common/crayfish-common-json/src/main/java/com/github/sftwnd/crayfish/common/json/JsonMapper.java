package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.sftwnd.crayfish.common.exception.ExceptionUtils;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created by ashindarev on 02.02.16.
 */
public final class JsonMapper {

    // Подразумевается, что mapper дйствует на проект и пересоздание, как и стирание mapper-конфигурации не требуется
    @SuppressWarnings("squid:S5164")
    private static ThreadLocal<ObjectMapper> objectMapper = ThreadLocal.withInitial(() -> new ObjectMapper()
              .registerModule(new JavaTimeModule())
              .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
              .configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true)
              .setTimeZone(Calendar.getInstance().getTimeZone())
              .setSerializationInclusion(JsonInclude.Include.NON_NULL)
              .findAndRegisterModules());

    public static <T>T parseObject(String json, Class<T> clazz) throws IOException {
        return objectMapper.get().readerFor(clazz).readValue(json);
    }

    public static <T>T parseObject(byte[] json, Class<T> clazz) throws IOException {
        return objectMapper.get().readerFor(clazz).readValue(json);
    }

    public static <T>T parseObject(String json, TypeReference<?> type) throws IOException {
        return objectMapper.get().readerFor(type).readValue(json);
    }

    public static <T>T parseObject(byte[] json, TypeReference<?> type) throws IOException {
        return objectMapper.get().readerFor(type).readValue(json);
    }

    public static String serializeObject(Object object) throws IOException {
        return objectMapper.get().writeValueAsString(object);
    }

    public static <T>T snakyParseObject(String json, Class<T> clazz) {
        try {
            return objectMapper.get().readerFor(clazz).readValue(json);
        } catch (IOException ioex) {
            return ExceptionUtils.uncheckExceptions(ioex);
        }
    }

    public static <T>T snakyParseObject(byte[] json, Class<T> clazz) {
        try {
            return objectMapper.get().readerFor(clazz).readValue(json);
        } catch (IOException ioex) {
            return ExceptionUtils.uncheckExceptions(ioex);
        }
    }

    public static String snakySerializeObject(Object object) {
        try {
            return objectMapper.get().writeValueAsString(object);
        } catch (IOException ioex) {
            return ExceptionUtils.uncheckExceptions(ioex);
        }
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper.get();
    }

}
