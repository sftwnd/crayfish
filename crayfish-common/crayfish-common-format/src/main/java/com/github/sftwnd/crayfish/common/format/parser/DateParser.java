package com.github.sftwnd.crayfish.common.format.parser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class DateParser extends TemporalParser<Date> {

    public DateParser() {
        this(null, null);
    }

    public DateParser(@Nullable DateTimeFormatter baseFormatter) {
        this(null, baseFormatter);
    }

    public DateParser(@Nonnull ZoneId zoneId) {
        this(dateTimeFormatter(ISO_LOCAL_DATE_TIME.withZone(Objects.requireNonNull(zoneId, "TemporalParser::new(ZoneID) - zoneId is null"))));
    }

    public DateParser(@Nullable DateTimeFormatter coreFormatter, @Nullable DateTimeFormatter baseFormatter) {
        super(temporalAccessor -> Date.from(Instant.from(temporalAccessor)),
              coreFormatter,
              baseFormatter);
    }

}
