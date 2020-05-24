/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json.serialize;

import com.github.sftwnd.crayfish.common.format.formatter.TemporalFormatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

/**
 * Json ZoneDateTime Serializer using {@link TemporalFormatter}
 *
 * Created 2016-02-08
 * @since 0.0.1
 * @author Andrey D. Shindarev
 */
public class JsonNoZoneTrimDateSerializer extends JsonZonedSerializer<Date> {

    @Override
    protected @Nullable TemporalAccessor temporalValue(@Nonnull Date dateTime) {
        return dateTime.toInstant().truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    protected @Nonnull TemporalFormatter constructSerializer() {
        return new TemporalFormatter(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

}
