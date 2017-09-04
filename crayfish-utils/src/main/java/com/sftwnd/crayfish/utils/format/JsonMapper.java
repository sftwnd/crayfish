package com.sftwnd.crayfish.utils.format;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created by ashindarev on 02.02.16.
 */
public final class JsonMapper {

    private static ThreadLocal<ObjectMapper> objectMapper = new ThreadLocal<ObjectMapper>() {
        @Override
        protected ObjectMapper initialValue() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            mapper.configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true);
            mapper.setTimeZone(Calendar.getInstance().getTimeZone());
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return mapper;
        }
    };

    public static <T>T parseObject(String json, Class<T> clazz) throws IOException {
        return objectMapper.get().readerFor(clazz).readValue(json);
    }

    public static <T>T parseObject(byte[] json, Class<T> clazz) throws IOException {
        return objectMapper.get().readerFor(clazz).readValue(json);
    }

    public static String serializeObject(Object object) throws IOException {
        return objectMapper.get().writeValueAsString(object);
    }
}
