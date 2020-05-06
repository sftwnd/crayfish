package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.sftwnd.crayfish.common.format.DateSerializeUtility;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Date;

/**
 * Created by ashindarev on 08.02.16.
 */
@Slf4j
public class JsonDateSerializer extends JsonSerializer<Date> {

    public static final String DATE_FORMAT_STR = "yyyy-MM-dd'T'HH:mm:ssXXX";

    @Override
    public void serialize(Date date, JsonGenerator gen, SerializerProvider provider) throws IOException {
        logger.trace("serialize(date:`{}`)", date);
        gen.writeString(getSerializeUtility().serialize(date));
    }

    protected DateSerializeUtility getSerializeUtility() {
        return DateSerializeUtility.getDateSerializeUtility((String) null, DATE_FORMAT_STR);
    }

}
