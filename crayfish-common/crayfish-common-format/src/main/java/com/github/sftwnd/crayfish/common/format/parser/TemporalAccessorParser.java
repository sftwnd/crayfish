package com.github.sftwnd.crayfish.common.format.parser;

import lombok.Generated;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;
import java.util.function.Function;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class TemporalAccessorParser extends TemporalParser<TemporalAccessor> {

    public TemporalAccessorParser() {
        this(null, null);
    }

    public TemporalAccessorParser(@Nullable DateTimeFormatter baseFormatter) {
        this(null, baseFormatter);
    }

    public TemporalAccessorParser(@Nonnull ZoneId zoneId) {
        this(dateTimeFormatter(ISO_LOCAL_DATE_TIME.withZone(Objects.requireNonNull(zoneId, "TemporalParser::new(ZoneID) - zoneId is null"))));
    }

    public TemporalAccessorParser(@Nullable DateTimeFormatter coreFormatter, @Nullable DateTimeFormatter baseFormatter) {
        super(Function.identity(), coreFormatter, baseFormatter);
    }

}
