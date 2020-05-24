/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.exception;

import lombok.Generated;
import lombok.SneakyThrows;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Утилиты для работы с исключениями
 */
@SuppressWarnings({
        /*
            Exception types should not be tested using "instanceof" in catch blocks

            We use case with Exception and InterruptedException in catch. The  Java
            compiler says that catch section of InterruptedException  is  never used
            when the catch of Exception section is present, but we needed in it.
         */
        "squid:S1193"
})
public final class ExceptionUtils {

    @Generated
    private ExceptionUtils() {
        super();
    }

    /**
     * Call the callable, in the case of Exception rethrow it sneaky
     * If callable throws InterruptedException - rethrow it without onThrow call
     * @param callable base processor
     * @param <T> the type of the result
     * @return result of call operation
     */
    @SneakyThrows
    @SuppressWarnings({
            // "InterruptedException" should not be ignored
            // we have reinterrupt InterruptionException, but in the hidden form
            "squid:S2142"
    })
    public static <T> T wrapUncheckedExceptions(Callable<T> callable) {
        try {
            return callable.call();
        } catch (InterruptedException ex) {
            throw checkInterruption(ex);
        }
    }

    /**
     * Call the callable, in the case of Exception try to call onThrow
     * If callable throws InterruptedException - rethrow it without onThrow call
     *
     * @param callable base processor
     * @param onThrow processor for the cas of throw on first call
     * @param <T> the type of the result
     * @return result of call operation
     */
    @SneakyThrows
    @SuppressWarnings({
            // "InterruptedException" should not be ignored
            // we have reinterrupt InterruptionException, but in the hidden form
            "squid:S2142",
            //Throwable and Error should not be caught
            "squid:S1181"
    })
    public static <T> T wrapUncheckedExceptions(@Nonnull Callable<T> callable, @Nonnull Callable<T> onThrow) {
        try {
            return callable.call();
        } catch (InterruptedException ex) {
            throw checkInterruption(ex);
        } catch (Throwable throwable) {
            return onThrow.call();
        }
    }

    /**
     * Process the processor, in the case of Exception rethrow it sneaky
     * @param processor base processor
     */
    @SneakyThrows
    public static void wrapUncheckedExceptions(@Nonnull Processor<? extends Exception> processor) {
        try {
            processor.process();
        } catch (Exception ex) {
            throw checkInterruption(ex);
        }
    }

    /**
     * Process the processor, in the case of Exception try to process onThrow
     * If processor throws InterruptedException - rethrow it without onThrow call
     * @param processor base processor
     * @param onThrow processor for the cas of throw on first call
     */
    @SuppressWarnings({
            //Throwable and Error should not be caught
            "squid:S1181"
    })
    public static void wrapUncheckedExceptions(@Nonnull Processor<? extends Exception> processor, @Nonnull Processor<? extends Exception> onThrow) {
        try {
            processor.process();
        } catch (Throwable throwable) {
            Optional.of(throwable)
                    .filter(InterruptedException.class::isInstance)
                    // In Java 11 change to ifPresentOrElse
                    .map(ExceptionUtils::uncheckExceptions)
                    .orElseGet(() -> {
                                wrapUncheckedExceptions(onThrow::process);
                                return null;
                            }
                    );
        }
    }

    /**
     * Throw exception without definition in throw section (just sneak it)
     * @param throwable Throwable
     * @param <R> the type of the result
     * @return nothing - just throw defined Throwable
     */
    @SneakyThrows
    public static final <R> R uncheckExceptions(Throwable throwable) {
        throw checkInterruption(throwable);
    }

    /**
     * If Throwable is InterruptedException then interrup current thread
     *
     * @param throwable Throwable
     * @return same as throwable parameter
     */
    public static final Throwable checkInterruption(Throwable throwable){
        if (throwable instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        return throwable;
    }

}
