package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

/**
 * Created by ashindarev on 08.02.16.
 */
@Slf4j
public final class JsonInstantDeserializer extends JsonDeserializer<Instant> {

    private JsonDateDeserializer jsonDateDeserializer = null;

    @Override
    public Instant deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonDateDeserializer checkForInit = this.jsonDateDeserializer;
        if (checkForInit == null) {
            synchronized (this) {
                checkForInit = this.jsonDateDeserializer;
                if (checkForInit == null) {
                    this.jsonDateDeserializer = new JsonDateDeserializer();
                }
            }
        }
        return Optional.ofNullable(this.jsonDateDeserializer.deserialize(jsonParser, deserializationContext))
                       .map(date -> date.toInstant())
                       .orElse(null);
    }

}
