/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.concurrent;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static com.github.sftwnd.crayfish.common.concurrent.LockUtils.runWithLock;
import static com.github.sftwnd.crayfish.common.exception.ExceptionUtils.wrapUncheckedExceptions;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RevocableReentantLockHelperTest {

    private static ExecutorService executorService;

    @BeforeAll
    public static void startUp() {
        executorService = Executors.newCachedThreadPool();
    }

    @AfterAll
    public static void tearDown() {
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void testStatusMonitor() {
        Lock lock = mock(Lock.class);
        Supplier<Instant> statusMonitor = mock(Supplier.class);
        RevocableReentantLockHelper helper = new RevocableReentantLockHelper(lock, statusMonitor);
        when(lock.tryLock()).thenReturn(true);
        when(statusMonitor.get()).thenReturn(null);
        assertTrue(helper.tryLock(), "Lock has to by acquired successfully if statusMonitor returns null");
        when(statusMonitor.get()).thenReturn(Instant.now().plus(1, ChronoUnit.MINUTES));
        assertTrue(helper.tryLock(), "Lock has to by acquired successfully if statusMonitor returns instant in the future");
        when(statusMonitor.get()).thenReturn(Instant.now().minus(1, ChronoUnit.SECONDS));
        assertTrue(helper.tryLock(), "Lock has to by acquired successfully if statusMonitor returns instant in the future");
    }

    @Test
    @SneakyThrows
    void tesRevoke() {
        final Runnable processor = mock(Runnable.class);
        doAnswer(invoke -> null).when(processor).run();
        final RevocableReentantLockHelper revocableLock = new RevocableReentantLockHelper(new ReentrantLock());
        assertDoesNotThrow(() -> runWithLock(revocableLock, processor::run));
        verify(processor, times(1)).run();
        revocableLock.revokeUntil(Instant.now().minusSeconds(10));
        revocableLock.lock();
        revocableLock.revokeUntil(Instant.now().minusSeconds(10));
        assertDoesNotThrow(() -> runWithLock(revocableLock, processor::run));
        verify(processor, times(2)).run();
        revocableLock.revokeUntil(Instant.now().plusSeconds(10));
        assertDoesNotThrow(() -> runWithLock(revocableLock, processor::run));
        verify(processor, times(3)).run();
        revocableLock.revokeUntil(getInitAt(revocableLock).plusNanos(1));
        assertFalse(() -> revocableLock.tryLock());
        assertEquals(0, getAcquires(revocableLock).get());
        revocableLock.lock();
        revocableLock.revokeUntil(getInitAt(revocableLock));
        assertFalse(() -> wrapUncheckedExceptions(() -> revocableLock.tryLock(1, TimeUnit.MILLISECONDS)));
    }

    @Test
    void testUnlockAndReleased() {
        Lock lock = new ReentrantLock();
        RevocableReentantLockHelper helper = new RevocableReentantLockHelper(lock);
        assertThrows(IllegalMonitorStateException.class, () -> helper.unlock(), "Unlock of non-locked helper has to throw IllegalMonitorStateException");
        assertEquals(0, getAcquires(helper).get(), "Acquires of lock on non-locked helper after unsuccess unlock has to be 0");
        helper.lock();
        helper.unlock();
        assertEquals(0, getAcquires(helper).get(), "Acquires of lock on unlocked helper after success unlock has to be 0");
    }

    @Test
    @SneakyThrows
    @SuppressWarnings({
            // "Thread.sleep" should not be used in tests
            "squid:S2925"
    })
    void testRevokeUntil() {
        Lock lock = mock(Lock.class);
        RevocableReentantLockHelper helper = new RevocableReentantLockHelper(lock);
        Instant revokeAt = getRevokedAt(helper);
        helper.revokeUntil(Instant.now());
        assertEquals(revokeAt, getRevokedAt(helper), "You have unable to set revokeAt value if lock is not acuired");
        helper.lock();
        Thread.sleep(10);
        try {
            Instant instant = getInitAt(helper).minus(1, ChronoUnit.SECONDS);
            helper.revokeUntil(instant);
            assertNotEquals(instant, getRevokedAt(helper), "You have unable to set revokeAt value if lock acuired before value");
            instant = getInitAt(helper).plus(8, ChronoUnit.MILLIS);
            helper.revokeUntil(instant);
            assertEquals(instant, getRevokedAt(helper), "You have to be able to set revokeAt value more than init value if revokeUntil was not call");
            instant = getInitAt(helper).plus(7, ChronoUnit.MILLIS);
            helper.revokeUntil(instant);
            assertEquals(instant, getRevokedAt(helper), "You have to be able to set revokeAt value more than init value if current revokeAt is after the limit");
            instant = getInitAt(helper).plus(10, ChronoUnit.MILLIS);
            helper.revokeUntil(instant);
            assertNotEquals(instant, getRevokedAt(helper), "You have unable to set revokeAt value more than revokeAt value if lock acuired before value");
        } finally {
            assertThrows(
                    RevokedException.class,
                    () -> helper.unlock(),
                    "Unable to unlock on helper which was reviked by revokeAt call"
            );
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void testLock() {
        Lock lock = mock(Lock.class);
        RevocableReentantLockHelper helper = new RevocableReentantLockHelper(lock);
        AtomicInteger acquires = getAcquires(helper);
        helper.lock();
        assertEquals(1, acquires.get(), "If lock.lock is completed then the acquires count = 1");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testLockInterruptibly() throws InterruptedException {
        Lock lock = mock(Lock.class);
        RevocableReentantLockHelper helper = new RevocableReentantLockHelper(lock);
        AtomicInteger acquires = getAcquires(helper);
        helper.lockInterruptibly();
        assertEquals(1, acquires.get(), "If lock.lock is completed then the acquires count = 1");
        doAnswer(invocation -> {
            throw new InterruptedException();
        }).when(lock).lockInterruptibly();
        assertThrows(
                InterruptedException.class,
                () -> helper.lockInterruptibly(),
                "Lock interruption has to throws testLockInterruptibly with Interrupted exception"
        );
        assertTrue(
                () -> {
                    try {
                        helper.lockInterruptibly();
                    } catch (InterruptedException itrex) {
                        return Thread.currentThread().isInterrupted();
                    }
                    return false;
                },
                "After testLockInterruptibly interruption the current thread have to be in interrupted state"
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void testTryLock() {
        Lock lock = mock(Lock.class);
        RevocableReentantLockHelper helper = new RevocableReentantLockHelper(lock);
        AtomicInteger acquires = getAcquires(helper);
        when(lock.tryLock()).thenReturn(false);
        assertFalse(helper.tryLock(), "If lock.tryLock return false then result has to be true");
        assertEquals(0, acquires.get(), "If lock.tryLock return false then the acquires count = 0");
        when(lock.tryLock()).thenReturn(true);
        assertTrue(helper.tryLock(), "If lock.tryLock return true then result has to be true");
        assertEquals(1, acquires.get(), "If lock.tryLock return true then the acquires count = 1");
        helper.tryLock();
        assertEquals(2, acquires.get(), "If lock.tryLock return true twice then the acquires count = 2");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testTryLockWithTimeout() throws InterruptedException {
        Lock lock = mock(Lock.class);
        RevocableReentantLockHelper helper = new RevocableReentantLockHelper(lock);
        AtomicInteger acquires = getAcquires(helper);
        when(lock.tryLock(anyLong(), any())).thenReturn(false);
        assertFalse(helper.tryLock(1, TimeUnit.MILLISECONDS), "If lock.tryLock(..timeout) return false then result has to be true");
        assertEquals(0, acquires.get(), "If lock.tryLock return false then the acquires count = 0");
        when(lock.tryLock(anyLong(), any())).thenReturn(true);
        assertTrue(helper.tryLock(1, TimeUnit.MILLISECONDS), "If lock.tryLock(..timeout) return true then result has to be true");
        assertEquals(1, acquires.get(), "If lock.tryLock(..timeout) return true then the acquires count = 1");
        helper.tryLock(1, TimeUnit.MILLISECONDS);
        assertEquals(2, acquires.get(), "If lock.tryLock(..timeout) return true twice then the acquires count = 2");
        when(lock.tryLock(anyLong(), any())).thenThrow(new InterruptedException());
        assertThrows(
                InterruptedException.class,
                () -> helper.tryLock(1, TimeUnit.MILLISECONDS),
                "Lock interruption has to throws tryLock with Interrupted exception"
        );
        assertTrue(
                () -> {
                    try {
                        helper.tryLock(1, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException itrex) {
                        return Thread.currentThread().isInterrupted();
                    }
                    return false;
                },
                "After tryLock interruption the current thread have to be in interrupted state"
        );
    }

    @Test
    void testNewCondition() {
        Condition condition = mock(Condition.class);
        Lock lock = mock(Lock.class);
        when(lock.newCondition()).thenReturn(condition);
        RevocableReentantLockHelper helper = new RevocableReentantLockHelper(lock);
        assertSame(condition, helper.newCondition(), "RevocableReentantLockHelper.newCondition() has return defined result");
        verify(lock, times(1)).newCondition();
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private AtomicInteger getAcquires(RevocableReentantLockHelper helper) {
        Field acquiresField = RevocableReentantLockHelper.class.getDeclaredField("acquires");
        acquiresField.setAccessible(true);
        return ((ThreadLocal<AtomicInteger>) acquiresField.get(helper)).get();
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private Instant getInitAt(RevocableReentantLockHelper helper) {
        Field initAtField = RevocableReentantLockHelper.class.getDeclaredField("initAt");
        initAtField.setAccessible(true);
        return ((ThreadLocal<Instant>) initAtField.get(helper)).get();
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private Instant getRevokedAt(RevocableReentantLockHelper helper) {
        Field revokedAtField = RevocableReentantLockHelper.class.getDeclaredField("revokedAt");
        revokedAtField.setAccessible(true);
        return ((AtomicReference<Instant>) revokedAtField.get(helper)).get();
    }

}