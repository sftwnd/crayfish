package com.github.sftwnd.crayfish.common.format.parser;

import com.github.sftwnd.crayfish.common.exception.ExceptionUtils;
import com.github.sftwnd.crayfish.common.state.DefaultsHolder;
import com.github.sftwnd.crayfish.common.state.DefaultsHolder.ValueLevel;
import com.github.sftwnd.crayfish.common.state.StateHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;

public class TemporalParser<T> {

    public static final ZoneId DEFAUT_ZONE_ID = ZoneId.of("UTC");

    private DateTimeFormatter coreFormatter;
    private DefaultsHolder<DateTimeFormatter> defaults;
    @Nonnull Function<TemporalAccessor, T> converter;

    public TemporalParser(@Nonnull Function<TemporalAccessor, T> converter) {
        this(converter, null, null);
    }

    public TemporalParser(@Nonnull Function<TemporalAccessor, T> converter, @Nullable DateTimeFormatter baseFormatter) {
        this(converter, null, baseFormatter);
    }

    public TemporalParser(@Nonnull Function<TemporalAccessor, T> converter, @Nonnull ZoneId zoneId) {
        this(converter, dateTimeFormatter(ISO_LOCAL_DATE_TIME.withZone(Objects.requireNonNull(zoneId, "TemporalParser::new(ZoneID) - zoneId is null"))));
    }

    public TemporalParser(@Nonnull Function<TemporalAccessor, T> converter, @Nullable DateTimeFormatter coreFormatter, @Nullable DateTimeFormatter baseFormatter) {
        this.converter = Objects.requireNonNull(converter, "TemporalParser::new - converter is null");
        this.coreFormatter = Optional.ofNullable(coreFormatter).orElse(ISO_ZONED_DATE_TIME);
        this.defaults = new DefaultsHolder<>(() -> dateTimeFormatter(baseFormatter));
    }

    public void clearDefaultZoneId() {
        defaults.clearDefaultValue();
    }

    public void setDefaultZoneId(@Nonnull final ZoneId zoneId) {
        defaults.setDefaultValue(defaults.getDefaultValue().withZone(Objects.requireNonNull(zoneId, "TemporalParser::setDefaultZoneId - zoneId is null")));
    }

    public void clearCurrentZoneId() {
        defaults.clearCurrentValue();
    }

    public void setCurrentZoneId(@Nonnull final ZoneId zoneId) {
        defaults.setCurrentValue(defaults.getCurrentValue().withZone(Objects.requireNonNull(zoneId, "TemporalParser::setDefaultZoneId - zoneId is null")));
    }

    public ZoneId getZoneId() {
        return defaults.getCurrentValue().getZone();
    }

    public ValueLevel getValueLevel() {
        return defaults.getValueLevel();
    }

    private @Nullable T parse(@Nullable String text, @Nonnull Function<TemporalAccessor, T> converter) {
        Objects.requireNonNull(converter, "TemporalParser::parse(text, converter) - converter is null");
        return Optional.ofNullable(text)
                .filter(txt -> !txt.isBlank())
                .map(str -> ExceptionUtils.wrapUncheckedExceptions(
                                () -> coreFormatter.parse(text),
                                () -> defaults.getCurrentValue().parse(text)
                            )
                )
                .map(converter::apply)
                .orElse(null);
    }

    public @Nullable T parse(@Nullable String text) {
        return this.parse(text, converter);
    }

    public @Nullable T parse(@Nullable String text, @Nonnull ZoneId zoneId) {
        return StateHolder.supply(
                Objects.requireNonNull(zoneId, "TemporalParser::parse(String, ZoneId) - zoneId is null"),
                this::getZoneId,
                this::setCurrentZoneId,
                this.defaults.getValueLevel() == ValueLevel.CURRENT ? this::setCurrentZoneId : z -> this.clearCurrentZoneId(),
                () -> parse(text)
        );
    }

    protected static @Nonnull DateTimeFormatter dateTimeFormatter(@Nullable DateTimeFormatter formatter) {
        return Optional.ofNullable(formatter)
                .map(f -> f.getZone() == null ? f.withZone(DEFAUT_ZONE_ID) : f)
                .orElseGet(() -> ISO_LOCAL_DATE_TIME.withZone(DEFAUT_ZONE_ID));
    }

    private static final Map<Object, TemporalParser<?>> parserMap = new WeakHashMap<>();

    public static TemporalParser<?> register(@Nonnull Object obj, @Nonnull Supplier<TemporalParser<?>> constructor) {
        synchronized (parserMap) {
            return parserMap.computeIfAbsent(
                    Objects.requireNonNull(obj, "TemporalParser::register - obj is null"),
                    o -> Objects.requireNonNull(constructor, "TemporalParser::register - constructor is null").get()
            );
        }
    }

    public static TemporalParser<?> unregister(@Nonnull Object obj) {
        synchronized (parserMap) {
            return parserMap.remove(obj);
        }
    }

    public static TemporalParser<?> parser(@Nonnull Object obj) {
        synchronized (parserMap) {
            return parserMap.get(obj);
        }
    }

}
