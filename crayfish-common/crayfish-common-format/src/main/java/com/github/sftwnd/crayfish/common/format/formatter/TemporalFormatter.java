package com.github.sftwnd.crayfish.common.format.formatter;

import com.github.sftwnd.crayfish.common.format.TemporalBase;
import com.github.sftwnd.crayfish.common.state.DefaultsHolder.ValueLevel;
import com.github.sftwnd.crayfish.common.state.StateHelper;
import lombok.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class TemporalFormatter extends TemporalBase {

    public static final DateTimeFormatter DEFAULT_FORMATTER = ISO_OFFSET_DATE_TIME.withZone(DEFAUT_ZONE_ID);

    public TemporalFormatter() {
        this(null, null);
    }

    public TemporalFormatter(@Nullable DateTimeFormatter formatter) {
        this(null, formatter);
    }

    public TemporalFormatter(@Nonnull ZoneId zoneId) {
        this(DEFAULT_FORMATTER.withZone(Objects.requireNonNull(zoneId, "ZonedDateTimeFormatter::new(ZoneId) - zoneId is null")));
    }

    public TemporalFormatter(@Nullable DateTimeFormatter baseFormatter, @Nullable DateTimeFormatter formatter) {
        super(Optional.ofNullable(baseFormatter).orElse(DEFAULT_FORMATTER));
        DateTimeFormatter defaultFormatter = Optional.ofNullable(formatter).orElse(DEFAULT_FORMATTER);
        if (defaultFormatter.getZone() == null) {
            defaultFormatter = defaultFormatter.withZone(DEFAUT_ZONE_ID);
        }
        this.defaults.setDefaultValue(defaultFormatter);
    }

    public @Nullable String format(@Nullable TemporalAccessor temporal) {
        return Optional.ofNullable(temporal)
                .map(defaults.getCurrentValue()::format)
                .orElse(null);
    }

    public @Nullable String format(@Nullable TemporalAccessor temporal, @Nonnull ZoneId zoneId) {
        return StateHelper.supply(
                Objects.requireNonNull(zoneId, "ZonedDateTimeFormatter.format(TemporalAccessor, ZoneId) - zoneId is null"),
                this::getZoneId,
                this::setCurrentZoneId,
                this.defaults.getValueLevel() == ValueLevel.CURRENT ? this::setCurrentZoneId : z -> this.clearCurrentZoneId(),
                () -> format(temporal)
        );
    }

    private static final Map<Object, TemporalFormatter> formatterMap = new WeakHashMap<>();

    public static TemporalFormatter register(@Nonnull Object obj, @Nullable Supplier<TemporalFormatter> constructor) {
       return register(formatterMap, TemporalFormatter::new, obj, constructor);
    }

    public static TemporalFormatter unregister(@Nonnull Object obj) {
        return unregister(formatterMap, obj);
    }

    public static TemporalFormatter formatter(@Nonnull Object obj) {
        return base(formatterMap, obj);
    }

}
