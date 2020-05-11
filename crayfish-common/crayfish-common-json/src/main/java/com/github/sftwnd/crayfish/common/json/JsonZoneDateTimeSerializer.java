package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.sftwnd.crayfish.common.format.DateSerializeUtility;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Created by ashindarev on 08.02.16.
 */
@Slf4j
public final class JsonZoneDateTimeSerializer extends JsonSerializer<ZonedDateTime> {

    public static final String DATE_FORMAT_STR = "yyyy-MM-dd'T'HH:mm:ssXXX";

    @Override
    public void serialize(ZonedDateTime dateTime, JsonGenerator gen, SerializerProvider provider) throws IOException {
        logger.trace("serialize(date:`{}`)", dateTime);
        gen.writeString(
                Optional.ofNullable(dateTime).
                        map(dtt -> Date.from(dtt.withZoneSameLocal(dtt.getZone()).toInstant())).
                        map(dat -> DateSerializeUtility.getDateSerializeUtility(TimeZone.getTimeZone(dateTime.getZone()), DATE_FORMAT_STR).serialize(dat))
                        .orElse(null));
    }

}
