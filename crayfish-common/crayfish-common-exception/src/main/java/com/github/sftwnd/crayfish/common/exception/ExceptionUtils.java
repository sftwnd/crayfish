/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.exception;

import lombok.Generated;
import lombok.SneakyThrows;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

/**
 * Утилиты для работы с исключениями
 */
public final class ExceptionUtils {

    @Generated
    private ExceptionUtils() {
        super();
    }

    @SneakyThrows
    public static <T> T wrapUncheckedExceptions(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception ex) {
            return uncheckExceptions(ex);
        }
    }

    public static <T> T wrapUncheckedExceptions(@Nonnull Callable<T> callable, @Nonnull Callable<T> onThrow) {
        try {
            return callable.call();
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                return uncheckExceptions(ex);
            } else {
                return wrapUncheckedExceptions(onThrow);
            }
        }
    }

    @SneakyThrows
    public static void wrapUncheckedExceptions(@Nonnull Processor<? extends Exception> processor) {
        try {
            processor.process();
        } catch (Exception ex) {
            uncheckExceptions(ex);
        }
    }

    public static void wrapUncheckedExceptions(@Nonnull Processor<? extends Exception> processor, @Nonnull Processor<? extends Exception> onThrow) {
        try {
            processor.process();
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                uncheckExceptions(ex);
            } else {
                wrapUncheckedExceptions(onThrow);
            }
        }
    }

    @SneakyThrows
    public static final <R>R uncheckExceptions(Throwable throwable) {
        if (throwable instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        throw throwable;
    }

}
