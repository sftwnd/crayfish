package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

/**
 * Created by ashindarev on 08.02.16.
 */
public final class JsonInstantDeserializer extends JsonDeserializer<Instant> {

    private static final Logger logger = LoggerFactory.getLogger(JsonInstantDeserializer.class);

    private JsonDateDeserializer jsonDateDeserializer = null;

    @Override
    public Instant deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        if (jsonDateDeserializer == null) {
            synchronized (this) {
                if (jsonDateDeserializer == null) {
                    jsonDateDeserializer = new JsonDateDeserializer();
                }
            }
        }
        Date date = jsonDateDeserializer.deserialize(jsonParser, deserializationContext);
        return date == null ? null : date.toInstant();
    }

}
