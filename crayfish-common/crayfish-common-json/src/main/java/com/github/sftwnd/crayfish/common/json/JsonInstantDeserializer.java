/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.github.sftwnd.crayfish.common.format.DateSerializeUtility;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * <p>Json Instant Deserializer using {@link DateSerializeUtility} with ability to control timeZone</p>
 *
 * Created 2016-02-08
 *
 * @author <ul><li>Andrey D. Shindarev (ashindarev@gmail.com)</li><li>...</li>...</ul>
 * @version 1.1.1
 * @since 1.0.0
 */
@Slf4j
public final class JsonInstantDeserializer extends JsonDeserializer<Instant> {

    private final JsonDateDeserializer jsonDateDeserializer = new JsonDateDeserializer();

    /**
     * Deserialize Instant from String
     * @param jsonParser
     * @param deserializationContext
     * @return Instant
     * @throws IOException
     */
    @Override
    public Instant deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return Optional.ofNullable(this.jsonDateDeserializer.deserialize(jsonParser, deserializationContext))
                       .map(Date::toInstant)
                       .orElse(null);
    }

}
