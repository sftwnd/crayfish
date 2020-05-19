/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Optional;

/**
 * <p>Json Mapper with ablity to control thread and JVM time zone</p>
 *
 * Created 2016-02-02
 *
 * @author <ul><li>Andrey D. Shindarev (ashindarev@gmail.com)</li><li>...</li>...</ul>
 * @version 1.1.1
 * @since 1.0.0
 */
public class JsonMapper implements IJsonMapper {

    public static final ZoneId DEFAULT_ZONE_ID = JsonZonedMapper.DEFAULT_ZONE_ID;

    private final JsonZonedMapper mapper = new JsonZonedMapper();
    private final ZoneId zoneId;

    public JsonMapper() {
        this(DEFAULT_ZONE_ID);
    }

    public JsonMapper(ZoneId zoneId) {
        this.zoneId = Optional.ofNullable(zoneId).orElse(DEFAULT_ZONE_ID);
    }

    @Override
    public <T> T parseObject(@Nullable byte[] json, @Nonnull Class<T> clazz) throws IOException {
        return mapper.parseObject(zoneId, json, clazz);
    }

    @Override
    public <T> T parseObject(@Nullable byte[] json, @Nonnull TypeReference<T> type) throws IOException {
        return mapper.parseObject(zoneId, json, type);
    }

    @Override
    public String formatObject(@Nullable Object object) throws IOException {
        return mapper.formatObject(zoneId, object);
    }

    public ObjectMapper getObjectMapper() {
        return mapper.getObjectMapper();
    }

}
