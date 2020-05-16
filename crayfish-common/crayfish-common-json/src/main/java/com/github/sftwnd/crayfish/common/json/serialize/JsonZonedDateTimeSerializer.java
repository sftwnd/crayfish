/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json.serialize;

import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;

/**
 * <p>Json ZoneDateTime Serializer</p>
 *
 * Created 2016-02-08
 *
 * @author <ul><li>Andrey D. Shindarev (ashindarev@gmail.com)</li><li>...</li>...</ul>
 * @version 1.1.1
 * @since 1.0.0
 */

@Slf4j
public final class JsonZonedDateTimeSerializer extends JsonZonedSerializer<ZonedDateTime> {

    @Override
    protected TemporalAccessor temporalValue(ZonedDateTime dateTime) {
        return dateTime.truncatedTo(ChronoUnit.SECONDS);
    }

}
