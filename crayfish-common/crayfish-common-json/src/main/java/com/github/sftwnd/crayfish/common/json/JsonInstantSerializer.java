package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.sftwnd.crayfish.common.format.DateSerializeUtility;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Created by ashindarev on 08.02.16.
 */
@Slf4j
public final class JsonInstantSerializer extends JsonSerializer<Instant> {

    public static final String DATE_FORMAT_STR = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private final DateSerializeUtility dateSerializeUtility = DateSerializeUtility.getDateSerializeUtility(TimeZone.getTimeZone("UTC"), DATE_FORMAT_STR);

    /**
     * Serialize date to String
     *
     * @param instant Serialized instant
     * @param gen Json Generator
     * @param provider Serializer Provider
     * @throws IOException
     */
    @Override
    public void serialize(Instant instant, JsonGenerator gen, SerializerProvider provider) throws IOException {
        logger.trace("serialize(date:`{}`)", instant);
        gen.writeString(Optional.ofNullable(instant)
                .map(dateSerializeUtility::serialize)
                .orElse(null));
    }

}
