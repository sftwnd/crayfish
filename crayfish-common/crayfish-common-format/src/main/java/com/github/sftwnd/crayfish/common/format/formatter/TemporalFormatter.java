package com.github.sftwnd.crayfish.common.format.formatter;

import com.github.sftwnd.crayfish.common.state.DefaultsHolder;
import com.github.sftwnd.crayfish.common.state.DefaultsHolder.ValueLevel;
import com.github.sftwnd.crayfish.common.state.StateHolder;
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

public class TemporalFormatter {

    public static final ZoneId DEFAUT_ZONE_ID = ZoneId.of("UTC");
    public static final DateTimeFormatter DEFAULT_FORMATTER = ISO_OFFSET_DATE_TIME.withZone(DEFAUT_ZONE_ID);

    private DefaultsHolder<DateTimeFormatter> defaults;

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
        this.defaults = new DefaultsHolder<>(() -> Optional.ofNullable(baseFormatter).orElse(DEFAULT_FORMATTER));
        DateTimeFormatter defaultFormatter = Optional.ofNullable(formatter).orElse(DEFAULT_FORMATTER);
        if (defaultFormatter.getZone() == null) {
            defaultFormatter = defaultFormatter.withZone(DEFAUT_ZONE_ID);
        }
        this.defaults.setDefaultValue(defaultFormatter);
    }

    public void clearDefaultZoneId() {
        defaults.clearDefaultValue();
    }

    public void setDefaultZoneId(@Nonnull final ZoneId zoneId) {
        defaults.setDefaultValue(defaults.getDefaultValue().withZone(Objects.requireNonNull(zoneId, "ZonedDateTimeFormatter::setDefaultZoneId - zoneId is null")));
    }

    public void clearCurrentZoneId() {
        defaults.clearCurrentValue();
    }

    public void setCurrentZoneId(@Nonnull final ZoneId zoneId) {
        defaults.setCurrentValue(defaults.getCurrentValue().withZone(Objects.requireNonNull(zoneId, "ZonedDateTimeFormatter::setDefaultZoneId - zoneId is null")));
    }

    public ZoneId getZoneId() {
        return defaults.getCurrentValue().getZone();
    }

    public ValueLevel getValueLevel() {
        return defaults.getValueLevel();
    }

    public @Nullable String format(@Nullable TemporalAccessor temporal) {
        return Optional.ofNullable(temporal)
                .map(defaults.getCurrentValue()::format)
                .orElse(null);
    }

    public @Nullable String format(@Nullable TemporalAccessor temporal, @NonNull ZoneId zoneId) {
        return StateHolder.supply(
                Objects.requireNonNull(zoneId, "ZonedDateTimeFormatter.format(TemporalAccessor, ZoneId) - zoneId is null"),
                this::getZoneId,
                this::setCurrentZoneId,
                this.defaults.getValueLevel() == ValueLevel.CURRENT ? this::setCurrentZoneId : z -> this.clearCurrentZoneId(),
                () -> format(temporal)
        );
    }

    private static final Map<Object, TemporalFormatter> formatterMap = new WeakHashMap<>();

    public static TemporalFormatter register(@Nonnull Object obj, @Nullable Supplier<TemporalFormatter> constructor) {
        synchronized (formatterMap) {
            return formatterMap.computeIfAbsent(
                    Objects.requireNonNull(obj, "ZonedDateTimeParser::register - obj is null"),
                    o -> Optional.ofNullable(constructor)
                            .map(Supplier::get)
                            .orElseGet(TemporalFormatter::new)
            );
        }
    }

    public static TemporalFormatter unregister(@Nonnull Object obj) {
        synchronized (formatterMap) {
            return formatterMap.remove(obj);
        }
    }

    public static TemporalFormatter formatter(@Nonnull Object obj) {
        synchronized (formatterMap) {
            return formatterMap.get(obj);
        }
    }

}
