/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json.deserialize;

import com.github.sftwnd.crayfish.common.format.parser.TemporalParser;
import com.github.sftwnd.crayfish.common.format.parser.InstantParser;

import java.time.Instant;

/**
 * Json Instant Deserializer using {@link InstantParser} with ability to control timeZone
 *
 * Created 2016-02-08
 * @since 0.0.1
 * @author Andrey D. Shindarev
 */
public final class JsonInstantDeserializer extends JsonZonedDeserializer<Instant> {

    @Override
    public TemporalParser<Instant> constructParser() {
        return new InstantParser();
    }

}
