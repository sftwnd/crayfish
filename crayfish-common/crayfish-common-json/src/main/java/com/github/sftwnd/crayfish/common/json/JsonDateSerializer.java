/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.sftwnd.crayfish.common.format.DateSerializeUtility;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Date;

/**
 * <p>Json Data Serializer using {@link DateSerializeUtility}</p>
 *
 * Created 2016-02-08
 *
 * @author <ul><li>Andrey D. Shindarev (ashindarev@gmail.com)</li><li>...</li>...</ul>
 * @version 1.1.1
 * @since 1.0.0
 */
@Slf4j
public class JsonDateSerializer extends JsonSerializer<Date> {

    /**
     * Default date format
     */
    public static final String DATE_FORMAT_STR = "yyyy-MM-dd'T'HH:mm:ssXXX";

    /**
     * Serialize date to String
     *
     * @param date Serialized data
     * @param gen Json Generator
     * @param provider Serializer Provider
     * @throws IOException
     */
    @Override
    public void serialize(Date date, JsonGenerator gen, SerializerProvider provider) throws IOException {
        logger.trace("serialize(date:`{}`)", date);
        gen.writeString(getSerializeUtility().serialize(date));
    }

    protected DateSerializeUtility getSerializeUtility() {
        return DateSerializeUtility.getDateSerializeUtility((String) null, DATE_FORMAT_STR);
    }

}
