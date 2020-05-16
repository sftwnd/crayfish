/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.exception;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.sftwnd.crayfish.common.exception.ExceptionUtils.uncheckExceptions;
import static com.github.sftwnd.crayfish.common.exception.ExceptionUtils.wrapUncheckedExceptions;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionUtilsTest {

    private static void runUNSOPEX() throws Exception {
        callUNSOPEX();
    }

    private static Long callUNSOPEX() throws Exception {
        return uncheckExceptions(new UnsupportedOperationException());
    }

    private static void runInterrupt() throws InterruptedException {
        callInterrupt();
    }

    private static Object callInterrupt() throws InterruptedException {
        throw new InterruptedException();
    }

    @Test
    void testUncheckedExceptions() {
        assertThrows(
                UnsupportedOperationException.class
                , () -> callUNSOPEX()
                , "UnsupportedOperationException has to be throwed in uncheckExceptions"
        );
    }

    @Test
    void testWrapUncheckedProcessExceptions() {
        assertThrows(
                UnsupportedOperationException.class
                , () -> wrapUncheckedExceptions(ExceptionUtilsTest::callUNSOPEX)
                , "UnsupportedOperationException has to be rethrowed in wrapUncheckedExceptions(Process)"
        );
        assertThrows(
                UnsupportedOperationException.class
                , () -> wrapUncheckedExceptions(ExceptionUtilsTest::runUNSOPEX, ExceptionUtilsTest::callUNSOPEX)
                , "UnsupportedOperationException has to be rethrowed in wrapUncheckedExceptions(Process,Process)"
        );
        final AtomicInteger cnt = new AtomicInteger(0);
        assertDoesNotThrow(
                () -> wrapUncheckedExceptions(ExceptionUtilsTest::runUNSOPEX, () -> { cnt.incrementAndGet(); })
                , "UnsupportedOperationException has to be completle finished without throws in wrapUncheckedExceptions(Process,Process)"
        );
        assertTrue(cnt.get() == 1,"wrapUncheckedExceptions(processor,processor) - second processor has to be invoked when firs one throws exception");
        cnt.set(0);
        assertDoesNotThrow(
                () -> wrapUncheckedExceptions(() -> { cnt.incrementAndGet(); })
                , "UnsupportedOperationException has to be completle finished without throws in wrapUncheckedExceptions(Process)"
        );
        assertTrue(cnt.get() == 1,"wrapUncheckedExceptions(processor) - processor has to be invoked");
        cnt.set(0);
        assertDoesNotThrow(
                () -> wrapUncheckedExceptions(() -> { cnt.incrementAndGet(); }, () -> { cnt.incrementAndGet(); })
                , "UnsupportedOperationException has to be completle finished without throws in wrapUncheckedExceptions(Process)"
        );
        assertTrue(cnt.get() == 1,"wrapUncheckedExceptions(processor,processor) - just first success processors has to be invoked");

    }

    @Test
    void testWrapUncheckedCallExceptions() {
        assertThrows(
                UnsupportedOperationException.class
                , () -> wrapUncheckedExceptions(ExceptionUtilsTest::callUNSOPEX)
                , "UnsupportedOperationException has to be rethrowed in wrapUncheckedExceptions(Callable)"
        );
        assertThrows(
                UnsupportedOperationException.class
                , () -> wrapUncheckedExceptions(ExceptionUtilsTest::callUNSOPEX, ExceptionUtilsTest::callUNSOPEX)
                , "UnsupportedOperationException has to be rethrowed in wrapUncheckedExceptions(Callable,Callable)"
        );
        final AtomicInteger cnt = new AtomicInteger(0);
        assertDoesNotThrow(
                () -> wrapUncheckedExceptions(ExceptionUtilsTest::callUNSOPEX, cnt::incrementAndGet)
                , "UnsupportedOperationException has to be completle finished without throws in wrapUncheckedExceptions(Callable,Callable)"
        );
        assertTrue(cnt.get() == 1,"wrapUncheckedExceptions(callable,callable) - second callable has to be invoked when firs one throws exception");
        cnt.set(0);
        assertDoesNotThrow(
                () -> wrapUncheckedExceptions(cnt::incrementAndGet)
                , "UnsupportedOperationException has to be completle finished without throws in wrapUncheckedExceptions(Callable)"
        );
        assertTrue(cnt.get() == 1,"wrapUncheckedExceptions(callable) - callable has to be invoked");
        cnt.set(0);
        assertDoesNotThrow(
                () -> wrapUncheckedExceptions(() -> { cnt.set(1); return cnt.get(); }, () -> { cnt.set(2); return cnt.get(); })
                , "UnsupportedOperationException has to be completle finished without throws in wrapUncheckedExceptions(Callable,Callable)"
        );
        assertTrue(cnt.get() == 1,"wrapUncheckedExceptions(callable,callable) - just first callable has to be invoked");
    }

    boolean testInterruption(Runnable runnable) throws InterruptedException {
        AtomicBoolean result = new AtomicBoolean(false);
        synchronized (ExceptionUtilsTest.class) {
            new Thread(
                    () -> {
                        synchronized (ExceptionUtilsTest.class) {
                            try {
                                runnable.run();
                                result.set(!Thread.currentThread().isInterrupted());
                            } catch (Exception ex) {
                                result.set(ex instanceof InterruptedException);
                                assertTrue(Thread.currentThread().isInterrupted(), "ExceptionUtils has to interrupt current thread on interrupt exception");
                            } finally {
                                ExceptionUtilsTest.class.notify();
                            }
                        }
                    }
            ).start();
            ExceptionUtilsTest.class.wait();
        }
        return result.get();
    }

    @Test
    void testRunInterruption() throws InterruptedException {
        assertTrue(testInterruption(() -> wrapUncheckedExceptions(ExceptionUtilsTest::runInterrupt)), "ExceptionUtils.wrapUncheckedExceptions(Runnable) does not Interrupt thread on interrupted exception");
        assertTrue(testInterruption(() -> wrapUncheckedExceptions(ExceptionUtilsTest::runInterrupt, ExceptionUtilsTest::runInterrupt)), "ExceptionUtils.wrapUncheckedExceptions(Runnable) does not Interrupt thread on interrupted exception");
        assertTrue(testInterruption(() -> {}), "ExceptionUtils.wrapUncheckedExceptions(Success Runnable) has not got to Interrupt thread without interrupted exception");
        final AtomicBoolean value = new AtomicBoolean(true);
        assertTrue(testInterruption(() -> wrapUncheckedExceptions(ExceptionUtilsTest::runInterrupt, () -> value.set(false))), "ExceptionUtils.wrapUncheckedExceptions(Runnable) does not Interrupt thread on interrupted exception");
        assertTrue(value.get(), "wrapUncheckedExceptions(Runnable, Runnable) - the secont call doesn't have to be invoked if first call was interrupted");
    }

    @Test
    void testCallInterruption() throws InterruptedException {
        assertTrue(testInterruption(() -> wrapUncheckedExceptions(ExceptionUtilsTest::callInterrupt)), "ExceptionUtils.wrapUncheckedExceptions(Callable) does not Interrupt thread on interrupted exception");
        assertTrue(testInterruption(() -> wrapUncheckedExceptions(ExceptionUtilsTest::callInterrupt, ExceptionUtilsTest::callInterrupt)), "ExceptionUtils.wrapUncheckedExceptions(Callable) does not Interrupt thread on interrupted exception");
        assertTrue(testInterruption(() -> {}), "ExceptionUtils.wrapUncheckedExceptions(Success Callable) has not got to Interrupt thread without interrupted exception");
        final AtomicBoolean value = new AtomicBoolean(true);
        assertTrue(testInterruption(() -> wrapUncheckedExceptions(ExceptionUtilsTest::callInterrupt, () -> value.set(false))), "ExceptionUtils.wrapUncheckedExceptions(Callable) does not Interrupt thread on interrupted exception");
        assertTrue(value.get(), "wrapUncheckedExceptions(Callable, Callable) - the secont call doesn't have to be invoked if first call was interrupted");
    }

}