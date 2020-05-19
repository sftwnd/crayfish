package com.github.sftwnd.crayfish.common.format.parser;

import com.github.sftwnd.crayfish.common.exception.ExceptionUtils;
import com.github.sftwnd.crayfish.common.format.TemporalBase;
import com.github.sftwnd.crayfish.common.state.DefaultsHolder.ValueLevel;
import com.github.sftwnd.crayfish.common.state.StateHelper;

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

@SuppressWarnings({
        /*
            Generic wildcard types should not be used in return parameters
            Usage of <?> is more usefull in our case
        */
        "squid:S1452"
})
public class TemporalParser<T> extends TemporalBase {

    private DateTimeFormatter coreFormatter;
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
        super(dateTimeFormatter(baseFormatter));
        this.converter = Objects.requireNonNull(converter, "TemporalParser::new - converter is null");
        this.coreFormatter = Optional.ofNullable(coreFormatter).orElse(ISO_ZONED_DATE_TIME);
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
        return StateHelper.supply(
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
        return register(parserMap, () -> null, obj, constructor);
    }

    public static TemporalParser<?> unregister(@Nonnull Object obj) {
        return unregister(parserMap, obj);
    }

    public static TemporalParser<?> parser(@Nonnull Object obj) {
        return base(parserMap, obj);
    }

}
