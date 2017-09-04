package com.sftwnd.crayfish.utils.format;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by ashindarev on 08.02.16.
 */
public final class JsonZoneDateTimeSerializer extends JsonSerializer<ZonedDateTime> {

    public static final Logger logger = LoggerFactory.getLogger(JsonZoneDateTimeSerializer.class);
    public  static final String dateFormatStr = "yyyy-MM-dd'T'HH:mm:ssXXX";

    @Override
    public void serialize(ZonedDateTime dateTime, JsonGenerator gen, SerializerProvider provider) throws IOException {
        logger.trace("serialize(date:`{}`)", dateTime);
        Date date = null;
        if (dateTime != null) {
            date = new Date(dateTime.withZoneSameLocal(TimeZone.getDefault().toZoneId()).toInstant().getEpochSecond()*1000L);
        }
        gen.writeString(date == null ? null : DateSerializeUtility.getDateSerializeUtility(TimeZone.getTimeZone(dateTime.getZone()), dateFormatStr).serialize(date));
    }

}
