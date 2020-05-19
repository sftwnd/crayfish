package com.github.sftwnd.crayfish.common.format.parser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class ZonedDateTimeParser extends TemporalParser<ZonedDateTime> {

    public ZonedDateTimeParser() {
        this(null, null);
    }

    public ZonedDateTimeParser(@Nullable DateTimeFormatter baseFormatter) {
        this(null, baseFormatter);
    }

    public ZonedDateTimeParser(@Nonnull ZoneId zoneId) {
        this(dateTimeFormatter(ISO_LOCAL_DATE_TIME.withZone(Objects.requireNonNull(zoneId, "TemporalParser::new(ZoneID) - zoneId is null"))));
    }

    public ZonedDateTimeParser(@Nullable DateTimeFormatter coreFormatter, @Nullable DateTimeFormatter baseFormatter) {
        super(ZonedDateTime::from, coreFormatter, baseFormatter);
    }

}
