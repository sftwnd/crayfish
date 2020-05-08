package com.github.sftwnd.crayfish.common.exception;

import lombok.SneakyThrows;
import java.util.concurrent.Callable;

/**
 * Утилиты для работы с исключениями
 */
public final class ExceptionUtils {

    private ExceptionUtils() {
        super();
    }

    @SneakyThrows
    public static <T> T wrapUncheckedExceptions(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception ex) {
            // Next line is covered by the JUnit. JaCoCo result is wrong
            return uncheckExceptions(ex);
        }
    }

    @SneakyThrows
    public static void wrapUncheckedExceptions(Process<? extends Exception> process) {
        try {
            process.work();
        } catch (Exception ex) {
            // Next line is covered by the JUnit. JaCoCo result is wrong
            uncheckExceptions(ex);
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
