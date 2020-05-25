/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.exception;

/**
 * This interface is designed to provide a common protocol for objects that
 * wish to execute code while they are active and are able to throws defined
 * Exception.
 *
 * @since 0.0.1
 * @author Andrey D. Shindarev
 * @param <E> the type of throwed exception
 */
public interface Processor<E extends Exception> {

    /**
     * A task that process operation and may throw an exception.
     * Implementors define a single method with no arguments called
     * {@code process}.
     *
     * <p>The {@code Processor} interface is similar to {@link
     * java.lang.Runnable}, however, it able to throw a checked exception.
     *
     * @throws E throwed Exception
     */
    void process() throws E;

}
