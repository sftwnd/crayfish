package com.github.sftwnd.crayfish.common.state;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@SuppressWarnings({
        /*
            Generic wildcard types should not be used in return parameters
            Usage of <?> is more usefull in our case
        */
        "squid:S1452"
})
public class DefaultsHolder<T> {

    /**
     * Level of value - System, default or on thread
     */
    public enum ValueLevel {
        /**
         * Core value (unchangable value in the case of no default and thread values - same for all threads)
         */
        SYSTEM,
        /**
         * Default value (changable and cleanable, same for all threads)
         */
        DEFAULT,
        /**
         * Value only for current thread
         */
        CURRENT
    }

    // Absolutly default value - global
    private final @Nonnull Supplier<T> systemValue;
    // Changeable global default value
    private Supplier<T> defaultValue = null;
    // Current thread value
    @SuppressWarnings({
            /*
                "ThreadLocal" variables should be cleaned up  when no longer used
                So, it's normal case to use DefaultsHolder without clear of state
                You are able co control currentValue clearance by himself  or  by
                StateHelper helper
             */
            "squid:S5164"
    })
    private ThreadLocal<Supplier<T>> currentValue = new ThreadLocal<>();

    // Constructor

    public DefaultsHolder() {
        this(() -> null);
    }

    public DefaultsHolder(@Nonnull Supplier<T> systemValaue) {
        this.systemValue = Optional.ofNullable(systemValaue).orElse(() -> null);
    }

    // Suppliers

    private synchronized @Nonnull Supplier<T> getDefaultSupplier() {
        return Optional.ofNullable(defaultValue).orElse(systemValue);
    }

    private @Nonnull Supplier<T> getCurrentSupplier() {
        return Optional.ofNullable(currentValue.get()).orElseGet(this::getDefaultSupplier);
    }

    // Default value

    public synchronized DefaultsHolder<T> setDefaultValue(Supplier<T> value) {
        this.defaultValue = value;
        return this;
    }

    public synchronized DefaultsHolder<T> setDefaultValue(T value) {
        return setDefaultValue(() -> value);
    }

    @SuppressWarnings("unchecked")
    public synchronized DefaultsHolder<T> clearDefaultValue() {
        return setDefaultValue((Supplier<T>)null);
    }

    public synchronized T getDefaultValue() {
        return getDefaultSupplier().get();
    }

    // Current

    public DefaultsHolder<T> setCurrentValue(Supplier<T> value) {
        // In Java 11 change to Optional and ifPresentOrElse
        if (value == null) {
            currentValue.remove();
        } else {
            currentValue.set(value);
        }
        return this;
    }

    public DefaultsHolder<T> setCurrentValue(T value) {
        return setCurrentValue(() -> value);
    }

    public DefaultsHolder<T> clearCurrentValue() {
        return setCurrentValue((Supplier<T>)null);
    }

    public synchronized T getCurrentValue() {
        return getCurrentSupplier().get();
    }

    public ValueLevel getValueLevel() {
        if (currentValue.get() == null) {
            return defaultValue == null ? ValueLevel.SYSTEM : ValueLevel.DEFAULT;
        } else {
            return ValueLevel.CURRENT;
        }
    }

    //

    private static Map<Object, DefaultsHolder<?>> holderMap = new WeakHashMap<>();

    public static @Nonnull <T> DefaultsHolder<T> register(@Nonnull Object obj, @Nonnull DefaultsHolder<T> holder) {
        synchronized (holderMap) {
            holderMap.put(
                    Objects.requireNonNull(obj, "DefaultsHolder::register - obj is null"),
                    Objects.requireNonNull(holder, "DefaultsHolder::register - holder is null")
            );
            return holder;
        }
    }

    public static @Nullable DefaultsHolder<?> unregister(@Nonnull Object obj) {
        synchronized (holderMap) {
            DefaultsHolder<?> result = holderMap.get(Objects.requireNonNull(obj, "DefaultsHolder::unregister - obj is null"));
            if (result != null) {
                holderMap.remove(obj);
            }
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    public static @Nullable <T> DefaultsHolder<T> holder(@Nonnull Object obj) {
        return (DefaultsHolder<T>)holder(obj, null);
    }

    @SuppressWarnings("unchecked")
    public static @Nullable <T> DefaultsHolder<T> holder(@Nonnull Object obj, @Nullable Supplier<DefaultsHolder<T>> holder) {
        synchronized (holderMap) {
            DefaultsHolder<?> result = holderMap.get(Objects.requireNonNull(obj, "DefaultsHolder::holder - obj is null"));
            if (result == null && holder != null) {
                result = holder.get();
                if (result != null) {
                    register(obj, result);
                }
            }
            return (DefaultsHolder<T>)result;
        }
    }

}
