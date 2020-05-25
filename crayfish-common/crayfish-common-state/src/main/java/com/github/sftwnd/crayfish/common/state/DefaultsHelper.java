package com.github.sftwnd.crayfish.common.state;

import com.github.sftwnd.crayfish.common.exception.Processor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.Callable;

public class DefaultsHelper<T> {

    private DefaultsHolder<T> holder;

    public DefaultsHelper(DefaultsHolder<T> holder) {
        this.holder = holder;
    }

    public <R> R call(@Nullable T state, @Nonnull Callable<R> callable) throws Exception {
        return DefaultsHelper.call(holder, state, callable);
    }

    public <E extends Exception> void process(@Nullable T state, @Nonnull Processor<E> processor) throws E {
        DefaultsHelper.process(holder, state, processor);
    }

    public void run(@Nullable T state, @Nonnull Runnable runnable) {
        DefaultsHelper.run(holder, state, runnable);
    }

    private static final String HOLDER_IS_NULL="DefaultsHelper::call - holder is null";

    public static <T, R> R call(@Nonnull DefaultsHolder<T> holder, @Nullable T state, @Nonnull Callable<R> callable) throws Exception {
        Objects.requireNonNull(holder, HOLDER_IS_NULL);
        return StateHelper.call(
                state,
                holder::getCurrentValue,
                holder::setCurrentValue,
                holder.getValueLevel() == DefaultsHolder.ValueLevel.CURRENT ? holder::setCurrentValue : z -> holder.clearCurrentValue(),
                callable
        );
    }

    public static <T, E extends Exception> void process(@Nonnull DefaultsHolder<T> holder, @Nullable T state, @Nonnull Processor<E> processor) throws E {
        Objects.requireNonNull(holder, HOLDER_IS_NULL);
        StateHelper.process(
                state,
                holder::getCurrentValue,
                holder::setCurrentValue,
                holder.getValueLevel() == DefaultsHolder.ValueLevel.CURRENT ? holder::setCurrentValue : z -> holder.clearCurrentValue(),
                processor
        );
    }

    public static <T> void run(@Nonnull DefaultsHolder<T> holder, @Nullable T state, @Nonnull Runnable runnable) {
        Objects.requireNonNull(holder, HOLDER_IS_NULL);
        StateHelper.run(
                state,
                holder::getCurrentValue,
                holder::setCurrentValue,
                holder.getValueLevel() == DefaultsHolder.ValueLevel.CURRENT ? holder::setCurrentValue : z -> holder.clearCurrentValue(),
                runnable
        );
    }

}
