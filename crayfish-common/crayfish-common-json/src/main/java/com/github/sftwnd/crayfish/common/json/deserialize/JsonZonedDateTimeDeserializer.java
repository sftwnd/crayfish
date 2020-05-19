/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json.deserialize;

import com.github.sftwnd.crayfish.common.format.parser.TemporalParser;
import com.github.sftwnd.crayfish.common.format.parser.ZonedDateTimeParser;

import java.time.ZonedDateTime;

/**
 * <p>Json ZonedDateTime Deserializer using {@link ZonedDateTimeParser} with ability to control timeZone</p>
 *
 * Created 2016-02-08
 *
 * @author <ul><li>Andrey D. Shindarev (ashindarev@gmail.com)</li><li>...</li>...</ul>
 * @version 1.1.1
 * @since 1.0.0
 */
public final class JsonZonedDateTimeDeserializer extends JsonZonedDeserializer<ZonedDateTime> {

    @Override
    public TemporalParser<ZonedDateTime> constructParser() {
        return new ZonedDateTimeParser();
    }

}
