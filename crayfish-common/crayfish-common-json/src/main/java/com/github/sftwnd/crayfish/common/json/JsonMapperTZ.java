package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.sftwnd.crayfish.common.exception.ExceptionUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.Callable;

/**
 * <p>Json Mapper with ablity to control thread and JVM time zone</p>
 *
 * Created 2016-02-02
 *
 * @author Andrey D. Shindarev
 * @version 1.1.1
 * @since 1.0.0
 */
public final class JsonMapperTZ {

    public static final TimeZone currentTimeZone = Calendar.getInstance().getTimeZone();

    private JsonMapperTZ() {
        super();
    }

    // Подразумевается, что mapper дйствует на проект и пересоздание, как и стирание mapper-конфигурации не требуется
    @SuppressWarnings("squid:S5164")
    private static ThreadLocal<Map<TimeZone, ObjectMapper>> objectMappers = ThreadLocal.withInitial(HashMap::new);

    public static ObjectMapper getObjectMapper(TimeZone timeZone) {
        return timeZone == null
                ? getObjectMapper(currentTimeZone)
                : objectMappers.get().computeIfAbsent(
                        timeZone, tz -> new ObjectMapper()
                        .findAndRegisterModules()
                        .registerModule(new JavaTimeModule())
                        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                        .configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, false)
                        .setTimeZone(tz)
                        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        );
    }

    public static void clear() {
        objectMappers.remove();
    }

    public static void remove(TimeZone timeZone) {
        objectMappers.get().remove(timeZone == null ? currentTimeZone : timeZone);
    }

    public static <T>T parseObject(TimeZone timeZone, String json, Class<T> clazz) throws IOException {
        return process(timeZone, () -> getObjectMapper(timeZone).readerFor(clazz).readValue(json));
    }

    public static <T>T parseObject(TimeZone timeZone, byte[] json, Class<T> clazz) throws IOException {
        return process(timeZone, () -> getObjectMapper(timeZone).readerFor(clazz).readValue(json));
    }

    public static <T>T parseObject(TimeZone timeZone, String json, TypeReference<T> type) throws IOException {
        return process(timeZone, () -> getObjectMapper(timeZone).readerFor(type).readValue(json));
    }

    public static <T>T parseObject(TimeZone timeZone, byte[] json, TypeReference<T> type) throws IOException {
        return process(timeZone, () -> getObjectMapper(timeZone).readerFor(type).readValue(json));
    }

    public static String serializeObject(TimeZone timeZone, Object object) throws IOException {
        return process(timeZone, () -> getObjectMapper(timeZone).writeValueAsString(object));
    }

    @SuppressWarnings("squid:S1130")
    private static <T>T process(TimeZone timeZone, Callable<T> callable) throws IOException {
        String zoneId = JsonDateDeserializer.getTimeZoneId();
        try {
            JsonDateDeserializer.setTimeZoneId(Optional.ofNullable(timeZone).map(TimeZone::getID).orElse(null));
            return ExceptionUtils.wrapUncheckedExceptions(callable::call);
        } finally {
            JsonDateDeserializer.setTimeZoneId(zoneId);
        }
    }

}
