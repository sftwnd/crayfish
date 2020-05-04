package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.sftwnd.crayfish.common.format.DateSerializeUtility;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by ashindarev on 08.02.16.
 */
@Slf4j
public final class JsonInstantSerializer extends JsonSerializer<Instant> {

    public static final String DATE_FORMAT_STR = "yyyy-MM-dd'T'HH:mm:ssXXX";

    @Override
    public void serialize(Instant instant, JsonGenerator gen, SerializerProvider provider) throws IOException {
        logger.trace("serialize(date:`{}`)", instant);
        Date date = null;
        if (instant != null) {
            date = Date.from(instant);
        }
        gen.writeString(date == null ? null : DateSerializeUtility.getDateSerializeUtility(TimeZone.getDefault(), DATE_FORMAT_STR).serialize(date));
    }

}
