package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.sftwnd.crayfish.common.format.DateSerializeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

/**
 * Created by ashindarev on 08.02.16.
 */
public final class JsonDateSerializer extends JsonSerializer<Date> {

    public static final Logger logger = LoggerFactory.getLogger(JsonDateSerializer.class);
    public  static final String dateFormatStr = "yyyy-MM-dd'T'HH:mm:ssXXX";

    @Override
    public void serialize(Date date, JsonGenerator gen, SerializerProvider provider) throws IOException {
        logger.trace("serialize(date:`{}`)", date);
        gen.writeString(DateSerializeUtility.defaultSerialize(date));
    }

}
