package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.type.TypeReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public interface IJsonMapper {

    <T> T parseObject(@Nullable byte[] json, @Nonnull Class<T> clazz) throws IOException;
    <T> T parseObject(@Nullable byte[] json, @Nonnull TypeReference<T> type) throws IOException;
    String formatObject(@Nullable Object object) throws IOException;

    default <T> T parseObject(@Nullable String json, @Nonnull Class<T> clazz) throws IOException  {
        return json == null ? null : parseObject(json.getBytes(), clazz);
    }
    default <T> T parseObject(@Nullable String json, @Nonnull TypeReference<T> type) throws IOException {
        return json == null ? null : parseObject(json.getBytes(), type);
    }

}
