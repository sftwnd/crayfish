package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Created by ashindarev on 08.02.16.
 */
@Slf4j
public final class JsonInstantDeserializer extends JsonDeserializer<Instant> {

    private final JsonDateDeserializer jsonDateDeserializer = new JsonDateDeserializer();

    @Override
    public Instant deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return Optional.ofNullable(this.jsonDateDeserializer.deserialize(jsonParser, deserializationContext))
                       .map(Date::toInstant)
                       .orElse(null);
    }

}
