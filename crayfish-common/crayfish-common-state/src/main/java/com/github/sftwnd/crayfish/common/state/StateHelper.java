/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.state;

import com.github.sftwnd.crayfish.common.exception.Processor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.sftwnd.crayfish.common.exception.ExceptionUtils.wrapUncheckedExceptions;
import static java.lang.Boolean.FALSE;

/**
 * Class helper to temporaty change state of object
 *
 * @author Andrey D. Shindarev (ashindarev@gmail.com)
 * @param <S> changed state class or holder
 * @version 1.1.1
 * @since 1.1.1
 *
 */
public final class StateHelper<S> implements AutoCloseable {

    private Runnable stateRestorer;

    public StateHelper(@Nullable S state, @Nonnull Supplier<S> stateGetter, @Nonnull Consumer<S> stateSetter) {
        this(state, stateGetter, stateSetter, stateSetter);
    }

    public StateHelper(@Nullable S state, @Nonnull Supplier<S> stateGetter, @Nonnull Consumer<S> stateSetter, @Nonnull Consumer<S> stateResotorer) {
        final S currentState = Objects.requireNonNull(stateGetter, "StateHelper::new - stateGetter is null").get();
        if (FALSE.equals(Optional.ofNullable(currentState).map(cs -> cs.equals(state)).orElseGet(() -> state == null))) {
            this.stateRestorer = () -> Objects.requireNonNull(stateResotorer, "StateHelper::new - stateResotorer is null").accept(currentState);
            Objects.requireNonNull(stateSetter, "StateHelper::new - stateSetter is null").accept(state);
        }
    }

    /**
     * Restore the saved state at the moment of current object creation if needed
     * P.S. works only once
     */
    public void close() {
        Optional.ofNullable(this.stateRestorer).ifPresent(restorer -> {
            this.stateRestorer = null;
            restorer.run();
        });
    }

    /**
     * Create AutoClosable implementation of State cass usage
     */
    public static <S> AutoCloseable construct(@Nullable S state, @Nonnull Supplier<S> stateGetter, @Nonnull Consumer<S> stateSetter) {
        return construct(state, stateGetter, stateSetter, stateSetter);
    }

    public static <S> AutoCloseable construct(@Nullable S state, @Nonnull Supplier<S> stateGetter, @Nonnull Consumer<S> stateSetter, @Nonnull Consumer<S> stateRestorer) {
        return new StateHelper<>(state, stateGetter, stateSetter, stateRestorer)::close;
    }

    @SuppressWarnings("try")
    public static <S, R> R call(@Nullable S state, @Nonnull Supplier<S> stateGetter, @Nonnull Consumer<S> stateSetter, @Nonnull Consumer<S> stateRestorer, @Nonnull Callable<R> callable) throws Exception {
        try (AutoCloseable x = construct(state, stateGetter, stateSetter, stateRestorer)) {
            return callable.call();
        }
    }

    public static <S, R> R call(@Nullable S state, @Nonnull Supplier<S> stateGetter, @Nonnull Consumer<S> stateSetter, @Nonnull Callable<R> callable) throws Exception {
        return call(state, stateGetter, stateSetter, stateSetter, callable);
    }

    public static <S, R> R supply(@Nullable S state, @Nonnull Supplier<S> stateGetter, @Nonnull Consumer<S> stateSetter, @Nonnull Consumer<S> stateRestorer, @Nonnull Supplier<R> supplier) {
        return wrapUncheckedExceptions(() -> call(state, stateGetter, stateSetter, stateRestorer, supplier::get));
    }

    public static <S, R> R supply(@Nullable S state, @Nonnull Supplier<S> stateGetter, @Nonnull Consumer<S> stateSetter, Supplier<R> supplier) {
        return supply(state, stateGetter, stateSetter, stateSetter, supplier);
    }

    @SuppressWarnings({
            /*
                Try-with-resources should be used
                AutoClosable variant is not very usefull because we need to catch more Exceptions
             */
            "squid:S2093"
    })
    public static <S, E extends Exception> void process(@Nullable S state, @Nonnull Supplier<S> stateGetter, @Nonnull Consumer<S> stateSetter, @Nonnull Consumer<S> stateRestorer, @Nonnull Processor<E> processor) throws E {
        StateHelper<S> stt = new StateHelper<>(state, stateGetter, stateSetter, stateRestorer);
        try {
            processor.process();
        } finally {
            stt.close();
        }
    }

    public static <S, E extends Exception> void process(@Nullable S state, @Nonnull Supplier<S> stateGetter, @Nonnull Consumer<S> stateSetter, @Nonnull Processor<E> processor) throws E {
        process(state, stateGetter, stateSetter, stateSetter, processor);
    }

    public static <S> void run(@Nullable S state, @Nonnull Supplier<S> stateGetter, @Nonnull Consumer<S> stateSetter, @Nonnull Consumer<S> stateRestorer, @Nonnull Runnable runnable) {
        wrapUncheckedExceptions(() -> process(state, stateGetter, stateSetter, stateRestorer, runnable::run));
    }

    public static <S> void run(@Nullable S state, @Nonnull Supplier<S> stateGetter, @Nonnull Consumer<S> stateSetter, @Nonnull Runnable runnable) {
       run(state, stateGetter, stateSetter, stateSetter, runnable);
    }

}