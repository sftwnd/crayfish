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

    @SneakyThrows
    public static void wrapUncheckedExceptions(@Nonnull Processor<? extends Exception> processor) {
        try {
            processor.process();
        } catch (Exception ex) {
            throw checkInterruption(ex);
        }
    }

    @SneakyThrows
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
                    .ifPresentOrElse(
                            ExceptionUtils::uncheckExceptions,
                            () -> wrapUncheckedExceptions(onThrow::process)
                    );
        }
    }

    @SneakyThrows
    public static final <R>R uncheckExceptions(Throwable throwable) {
        throw checkInterruption(throwable);
    }

    public static final Throwable checkInterruption(Throwable throwable){
        if (throwable instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        return throwable;
    }

}
