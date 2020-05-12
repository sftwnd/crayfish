/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.state;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <p>Class helper to temporaty change state of object</p>
 *
 * @author <ul><li>Andrey D. Shindarev (ashindarev@gmail.com)</li><li>...</li>...</ul>
 * @param <S> changed state class or holder
 * @version 1.1.1
 * @since 1.1.1
 *
 */
public final class State<S> implements AutoCloseable {

    private AtomicReference<S> state;
    private Consumer<S> stateRestorer;

    protected State(@Nullable S state, @Nonnull Supplier<S> stateGetter, @Nonnull Consumer<S> stateSetter) {
        S currentState = Objects.requireNonNull(stateGetter, "State::new - stateGetter is null").get();
        this.state = (state == currentState) || (state != null && state.equals(currentState)) ? null : new AtomicReference<>(currentState);
        this.stateRestorer = Objects.requireNonNull(stateSetter, "State::new - stateSetter is null");
        Optional.ofNullable(this.state).ifPresent(
                val -> stateSetter.accept(state)
        );
    }

    /**
     * Restore the saved state at the moment of current object creation if needed
     * P.S.> works only once
     */
    public void close() {
        if (this.state != null) {
            try {
                stateRestorer.accept(this.state.get());
            } finally {
                this.state = null;
            }
        }
    }

    /**
     * Create AutoClosable implementation of State cass usage
     */
    public static <S> AutoCloseable construct(@Nullable S state, @Nonnull Supplier<S> stateGetter, @Nonnull Consumer<S> stateSetter) {
        return new State<>(state, stateGetter, stateSetter)::close;
    }

}