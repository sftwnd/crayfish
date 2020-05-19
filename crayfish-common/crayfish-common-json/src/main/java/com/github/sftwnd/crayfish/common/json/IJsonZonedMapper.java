package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.type.TypeReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.ZoneId;

public interface IJsonZonedMapper {

    <T> T parseObject(@Nullable ZoneId zoneId, @Nullable byte[] json, @Nonnull Class<T> clazz) throws IOException;
    <T> T parseObject(@Nullable ZoneId zoneId, @Nullable byte[] json, @Nonnull TypeReference<T> type) throws IOException;
    String formatObject(@Nullable ZoneId zoneId, @Nullable Object object) throws IOException;

    default <T> T parseObject(@Nullable ZoneId zoneId, @Nullable String json, @Nonnull Class<T> clazz) throws IOException  {
        return json == null ? null : parseObject(zoneId, json.getBytes(), clazz);
    }
    default <T> T parseObject(@Nullable ZoneId zoneId, @Nullable String json, @Nonnull TypeReference<T> type) throws IOException {
        return json == null ? null : parseObject(zoneId, json.getBytes(), type);
    }
    default String formatObject(@Nullable Object object) throws IOException {
        return object == null ? null : formatObject(null, object);
    }

}
