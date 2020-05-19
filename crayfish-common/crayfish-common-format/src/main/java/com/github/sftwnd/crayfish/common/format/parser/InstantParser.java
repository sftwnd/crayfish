package com.github.sftwnd.crayfish.common.format.parser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class InstantParser extends TemporalParser<Instant> {

    public InstantParser() {
        this(null, null);
    }

    public InstantParser(@Nullable DateTimeFormatter baseFormatter) {
        this(null, baseFormatter);
    }

    public InstantParser(@Nonnull ZoneId zoneId) {
        this(dateTimeFormatter(ISO_LOCAL_DATE_TIME.withZone(Objects.requireNonNull(zoneId, "TemporalParser::new(ZoneID) - zoneId is null"))));
    }

    public InstantParser(@Nullable DateTimeFormatter coreFormatter, @Nullable DateTimeFormatter baseFormatter) {
        super(Instant::from, coreFormatter, baseFormatter);
    }

}
