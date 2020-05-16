/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json.serialize;

import com.github.sftwnd.crayfish.common.format.formatter.TemporalFormatter;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

/**
 * <p>Json ZoneDateTime Serializer using {@link TemporalFormatter}</p>
 *
 * Created 2016-02-08
 *
 * @author <ul><li>Andrey D. Shindarev (ashindarev@gmail.com)</li><li>...</li>...</ul>
 * @version 1.1.1
 * @since 1.0.0
 */

@Slf4j
public final class JsonNoZoneDateSerializer extends JsonZonedSerializer<Date> {

    protected TemporalAccessor temporalValue(Date dateTime) {
        return dateTime == null ? null
             : dateTime.toInstant().truncatedTo(ChronoUnit.SECONDS);
    }

    protected TemporalFormatter constructSerializer() {
        return new TemporalFormatter(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

}
