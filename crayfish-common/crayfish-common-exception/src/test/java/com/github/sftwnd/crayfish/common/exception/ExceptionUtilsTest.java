package com.github.sftwnd.crayfish.common.exception;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static com.github.sftwnd.crayfish.common.exception.ExceptionUtils.uncheckExceptions;
import static com.github.sftwnd.crayfish.common.exception.ExceptionUtils.wrapUncheckedExceptions;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionUtilsTest {

    private static Long callNPE() throws UnsupportedOperationException {
        return uncheckExceptions(new UnsupportedOperationException());
    }

    private static void callInterrupt() throws InterruptedException {
        throw new InterruptedException();
    }

    @Test
    void testUncheckedExceptions() {
        assertThrows(
                UnsupportedOperationException.class
                ,() -> ExceptionUtilsTest.callNPE()
                ,"UnsupportedOperationException has to be throwed in uncheckExceptions"
        );
    }

    @Test
    void testWrapUncheckedProcessExceptions() {
        assertThrows(
                UnsupportedOperationException.class
                ,() -> wrapUncheckedExceptions(() -> { ExceptionUtilsTest.callNPE(); })
                ,"UnsupportedOperationException has to be rethrowed in wrapUncheckedExceptions(Process)"
        );
        assertDoesNotThrow(
                 () -> wrapUncheckedExceptions(() -> {})
                ,"UnsupportedOperationException has to be completle finished without throws in wrapUncheckedExceptions(Process)"
        );
    }

    @Test
    void testWrapUncheckedCallExceptions() {
        assertThrows(
                UnsupportedOperationException.class
                ,() -> wrapUncheckedExceptions(() -> ExceptionUtilsTest.callNPE())
                ,"UnsupportedOperationException has to be rethrowed in wrapUncheckedExceptions(Callable)"
        );
        assertDoesNotThrow(
                () -> wrapUncheckedExceptions(() -> true)
                ,"UnsupportedOperationException has to be completle finished without throws in wrapUncheckedExceptions(Callable)"
        );
    }

    @Test
    void testInterruption() throws InterruptedException {
        synchronized (ExceptionUtilsTest.class) {
            new Thread(
                    () -> {
                        synchronized (ExceptionUtilsTest.class) {
                            try {
                                ExceptionUtils.wrapUncheckedExceptions(() -> ExceptionUtilsTest.callInterrupt());
                            } catch(Exception ex) {
                                assertTrue(Thread.currentThread().isInterrupted(), "ExceptionUtils has to interrupt current thread on interrupt exception");
                            } finally {
                                ExceptionUtilsTest.class.notify();
                            }
                        }
                    }
            ).start();
            ExceptionUtilsTest.class.wait();
        }
    }

}