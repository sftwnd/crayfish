package com.github.sftwnd.crayfish.common.format;

import com.github.sftwnd.crayfish.common.state.DefaultsHolder;
import com.github.sftwnd.crayfish.common.state.DefaultsHolder.ValueLevel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class TemporalBase {

    public static final ZoneId DEFAUT_ZONE_ID = ZoneId.of("UTC");

    protected DefaultsHolder<DateTimeFormatter> defaults;

    public TemporalBase(@Nullable DateTimeFormatter baseFormatter) {
        this.defaults = new DefaultsHolder<>(() -> baseFormatter);
    }

    public void clearDefaultZoneId() {
        defaults.clearDefaultValue();
    }

    public void setDefaultZoneId(@Nonnull final ZoneId zoneId) {
        defaults.setDefaultValue(defaults.getDefaultValue().withZone(Objects.requireNonNull(zoneId, this.getClass().getSimpleName()+"::setDefaultZoneId - zoneId is null")));
    }

    public void clearCurrentZoneId() {
        defaults.clearCurrentValue();
    }

    public void setCurrentZoneId(@Nonnull final ZoneId zoneId) {
        defaults.setCurrentValue(defaults.getCurrentValue().withZone(Objects.requireNonNull(zoneId, this.getClass().getSimpleName()+"::setDefaultZoneId - zoneId is null")));
    }

    public ZoneId getZoneId() {
        return defaults.getCurrentValue().getZone();
    }

    public ValueLevel getValueLevel() {
        return defaults.getValueLevel();
    }

    @SuppressWarnings({
            /*
                Blocks should be synchronized on "private final" fields
                We needed in synchronization because this code is 'deduplication' of code in child class
            */
            "squid:S2445"
    })
    protected static <X extends TemporalBase> X register(@Nonnull Map<Object, X> formatterMap, @Nonnull Supplier<X> constructNew, @Nonnull Object obj, @Nullable Supplier<X> constructor) {
        synchronized (Objects.requireNonNull(formatterMap, "TemporalBase::register - formatterMap is null")) {
            return formatterMap.computeIfAbsent(
                    Objects.requireNonNull(obj, "TemporalBase::register - obj is null"),
                    o -> Optional.ofNullable(constructor)
                            .map(Supplier::get)
                            .orElseGet(Objects.requireNonNull(constructNew::get, "TemporalBase::register - constructNew is null"))
            );
        }
    }

    @SuppressWarnings("squid:S2445")
    protected static <X extends TemporalBase> X unregister(@Nonnull Map<Object, X> formatterMap, @Nonnull Object obj) {
        synchronized (Objects.requireNonNull(formatterMap, "TemporalBase::unregister - formatterMap is null")) {
            return formatterMap.remove(obj);
        }
    }

    @SuppressWarnings({"unchecked","squid:S2445"})
    protected static <X extends TemporalBase> X base(@Nonnull Map<Object, X> formatterMap, @Nonnull Object obj) {
        synchronized (Objects.requireNonNull(formatterMap, "TemporalBase::base - formatterMap is null")) {
            return Optional.ofNullable(formatterMap.get(obj))
                           .orElseGet(() -> obj instanceof Class
                                   ? initClassRegistry(formatterMap, (Class<?>)obj)
                                   : null);
        }
    }

    private static @Nullable <X extends TemporalBase> X initClassRegistry(@Nonnull Map<Object, X> formatterMap, @Nonnull Class<?> clazz) {
        try {
            return Optional.ofNullable(clazz.getDeclaredConstructor())
                    .map( constructor -> {
                         try {
                             constructor.newInstance();
                             return formatterMap.get(clazz);
                         } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                             return null;
                         }
                    }).orElse(null);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

}
