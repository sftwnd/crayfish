package com.github.sftwnd.crayfish.common.concurrent;

import com.github.sftwnd.crayfish.common.concurrent.LockUtils.LockAquireTimeoutException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;

import static com.github.sftwnd.crayfish.common.concurrent.LockUtils.TRY_LOCK_WITHOUT_TIMEOUT_TIMEOUT;
import static com.github.sftwnd.crayfish.common.concurrent.LockUtils.acquireLock;
import static com.github.sftwnd.crayfish.common.concurrent.LockUtils.callWithLock;
import static com.github.sftwnd.crayfish.common.concurrent.LockUtils.runWithLock;
import static com.github.sftwnd.crayfish.common.concurrent.LockUtils.sneakyCallWithLock;
import static com.github.sftwnd.crayfish.common.concurrent.LockUtils.sneakyRunWithLock;
import static com.github.sftwnd.crayfish.common.exception.ExceptionUtils.wrapUncheckedExceptions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class LockUtilsTest {

    private static Object call() {
        return null;
    }

    @Test
    void testAcquireLock() {
        Lock lock = Mockito.mock(Lock.class);
        when(lock.tryLock()).thenReturn(false).thenReturn(true);
        assertThrows(
                NullPointerException.class
               ,() -> acquireLock(lock, NullPointerException::new)
               ,"acquireLock has to return generated exception if unable to acquire the lock"
        );
        assertNotNull(acquireLock(lock),"acquireLock has to return not null AutoClosable in the case of success");
    }

    @Test
    void testAcquireTryLock() throws InterruptedException {
        Lock lock = Mockito.mock(Lock.class);
        when(lock.tryLock(any(Long.class),any(TimeUnit.class))).thenReturn(false);
        assertThrows(
                LockAquireTimeoutException.class
                ,() -> acquireLock(lock, 1, TimeUnit.SECONDS, null)
                ,"acquireLock has to return generated exception if unable to acquire the lock"
        );
        when(lock.tryLock()).thenReturn(true);
        assertNotNull(acquireLock(lock, TRY_LOCK_WITHOUT_TIMEOUT_TIMEOUT, TimeUnit.SECONDS),"acquireLock has to return not null AutoClosable in the case of tryLock():success");
    }

    @Test
    void testCallWithLock() throws Exception {
        Lock lock = Mockito.mock(Lock.class);
        when(lock.tryLock()).thenReturn(false).thenReturn(true);
        assertThrows(
                NullPointerException.class
                ,() -> callWithLock(lock, NullPointerException::new, Function::identity)
                ,"callWithLock has to return generated exception if unable to acquire the lock"
        );
        long val = new Random().nextLong();
        assertEquals(  Long.valueOf(val),
                       wrapUncheckedExceptions(() -> callWithLock(lock, () -> Long.valueOf(val))),
                "callWithLock has to return defined value in the case of success"
        );
    }

    @Test
    void testCallWithTryLock() throws Exception {
        Lock lock = Mockito.mock(Lock.class);
        when(lock.tryLock(any(Long.class),any(TimeUnit.class))).thenReturn(false);
        when(lock.tryLock()).thenReturn(true).thenReturn(false);
        assertThrows(
                LockAquireTimeoutException.class
                ,() -> callWithLock(lock, 1, TimeUnit.SECONDS, null, Function::identity)
                ,"callWithLock has to return generated exception if unable to acquire the lock"
        );
        long val = new Random().nextLong();
        assertEquals(val,
                wrapUncheckedExceptions(() -> callWithLock(lock, TRY_LOCK_WITHOUT_TIMEOUT_TIMEOUT, TimeUnit.SECONDS, () -> Long.valueOf(val))),
                "callWithLock has to return defined value in the case of success");
    }

    @Test
    void testSneakyCallWithLock() throws Exception {
        Lock lock = Mockito.mock(Lock.class);
        when(lock.tryLock()).thenReturn(false).thenReturn(true);
        assertThrows(
                NullPointerException.class
                ,() -> sneakyCallWithLock(lock, NullPointerException::new, Function::identity)
                ,"sneakyCallWithLock has to return generated exception if unable to acquire the lock"
        );
        long val = new Random().nextLong();
        assertEquals(  Long.valueOf(val),
                wrapUncheckedExceptions(() -> sneakyCallWithLock(lock, () -> Long.valueOf(val))),
                "sneakyCallWithLock has to return defined value in the case of success"
        );
    }

    @Test
    void testSneakyCallWithTryLock() throws Exception {
        Lock lock = Mockito.mock(Lock.class);
        when(lock.tryLock(any(Long.class),any(TimeUnit.class))).thenReturn(false);
        when(lock.tryLock()).thenReturn(true).thenReturn(false);
        assertThrows(
                LockAquireTimeoutException.class
                ,() -> sneakyCallWithLock(lock, 1, TimeUnit.SECONDS, null, Function::identity)
                ,"sneakyCallWithLock has to return generated exception if unable to acquire the lock"
        );
        long val = new Random().nextLong();
        assertEquals(val,
                wrapUncheckedExceptions(() -> sneakyCallWithLock(lock, TRY_LOCK_WITHOUT_TIMEOUT_TIMEOUT, TimeUnit.SECONDS, () -> Long.valueOf(val))),
                "sneakyCallWithLock has to return defined value in the case of success");
    }

    @Test
    void testRunWithLock() throws Exception {
        Lock lock = Mockito.mock(Lock.class);
        when(lock.tryLock()).thenReturn(false).thenReturn(true);
        assertThrows(
                NullPointerException.class
                ,() -> runWithLock(lock, NullPointerException::new, Function::identity)
                ,"runWithLock has to return generated exception if unable to acquire the lock"
        );
        AtomicBoolean value = new AtomicBoolean(false);
        runWithLock(lock, () -> { value.set(true);});
        assertTrue(value.get(), "runWithLock has to complete work in the case of success");
    }

    @Test
    void testRunWithTryLock() throws Exception {
        Lock lock = Mockito.mock(Lock.class);
        when(lock.tryLock(any(Long.class),any(TimeUnit.class))).thenReturn(false);
        when(lock.tryLock()).thenReturn(true).thenReturn(false);
        assertThrows(
                LockAquireTimeoutException.class
                ,() -> runWithLock(lock, 1, TimeUnit.SECONDS, null, Function::identity)
                ,"runWithLock has to return generated exception if unable to acquire the lock"
        );
        AtomicBoolean value = new AtomicBoolean(false);
        runWithLock(lock, TRY_LOCK_WITHOUT_TIMEOUT_TIMEOUT, TimeUnit.SECONDS, () -> { value.set(true); });
        assertTrue(value.get(), "runWithLock has to complete work in the case of success");
    }



    @Test
    void testSneakyRunWithLock() throws Exception {
        Lock lock = Mockito.mock(Lock.class);
        when(lock.tryLock()).thenReturn(false).thenReturn(true);
        assertThrows(
                NullPointerException.class
                ,() -> sneakyRunWithLock(lock, NullPointerException::new, Function::identity)
                ,"sneakyRunWithLock has to return generated exception if unable to acquire the lock"
        );
        AtomicBoolean value = new AtomicBoolean(false);
        sneakyRunWithLock(lock, () -> { value.set(true);});
        assertTrue(value.get(), "sneakyRunWithLock has to complete work in the case of success");
    }

    @Test
    void testSneakyRunWithTryLock() throws Exception {
        Lock lock = Mockito.mock(Lock.class);
        when(lock.tryLock(any(Long.class),any(TimeUnit.class))).thenReturn(false);
        when(lock.tryLock()).thenReturn(true).thenReturn(false);
        assertThrows(
                LockAquireTimeoutException.class
                ,() -> sneakyRunWithLock(lock, 1, TimeUnit.SECONDS, null, Function::identity)
                ,"sneakyRunWithLock has to return generated exception if unable to acquire the lock"
        );
        AtomicBoolean value = new AtomicBoolean(false);
        sneakyRunWithLock(lock, TRY_LOCK_WITHOUT_TIMEOUT_TIMEOUT, TimeUnit.SECONDS, () -> { value.set(true); });
        assertTrue(value.get(), "sneakyRunWithLock has to complete work in the case of success");
    }

}