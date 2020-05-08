package com.github.sftwnd.crayfish.common.concurrent;

import com.github.sftwnd.crayfish.common.exception.Process;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import static com.github.sftwnd.crayfish.common.exception.ExceptionUtils.wrapUncheckedExceptions;

/**
 * Функции упрощающие работу с блокировками.
 */
public final class LockUtils {

    private LockUtils() {
        super();
    }

    /**
     * Константа, задающая таймаут, означающий, что нужно использовать функцию {@link Lock#tryLock()} вместо
     * {@link Lock#tryLock(long, TimeUnit)}.
     */
    public static final long TRY_LOCK_WITHOUT_TIMEOUT_TIMEOUT = -1;

    /**
     * Исключение выбрасываемое при таймауте получения блокировки.
     */
    public static class LockAquireTimeoutException extends RuntimeException {
        private static final long serialVersionUID = -2859072224946060682L;
        private LockAquireTimeoutException(Lock lock) {
            super("Unable to acquire lock: "+lock);
        }
    }

    // acquireLock

    public static AutoCloseable acquireLock(@Nonnull final Lock lock) {
        return acquireLock(lock, null);
    }

    public static AutoCloseable acquireLock(@Nonnull final Lock lock, @Nullable final Supplier<RuntimeException> onErrorThrow) {
        return acquireLock(lock, TRY_LOCK_WITHOUT_TIMEOUT_TIMEOUT, null, onErrorThrow);
    }

    public static AutoCloseable acquireLock(@Nonnull final Lock lock, final long timeout, @Nullable final TimeUnit timeUnit) {
        return acquireLock(lock, timeout, timeUnit, null);
    }

    public static AutoCloseable acquireLock(@Nonnull final Lock lock, final long timeout, @Nullable final TimeUnit timeUnit, @Nullable final Supplier<RuntimeException> onErrorThrow) {
        if ( tryLock(Objects.requireNonNull(lock, "LockUtl::acquireLock - lock is null"), timeout, timeUnit)) {
            return lock::unlock;
        }
        throw Optional.ofNullable(onErrorThrow).map(Supplier::get).orElseGet(() -> new LockAquireTimeoutException(lock));
    }

    private static boolean tryLock(@Nonnull final Lock lock, final long timeout, @Nullable final TimeUnit timeUnit) {
        if (timeout == TRY_LOCK_WITHOUT_TIMEOUT_TIMEOUT || timeUnit == null) {
            return lock.tryLock();
        } else {
            return wrapUncheckedExceptions(() -> lock.tryLock(timeout, timeUnit));
        }
    }

    // callWithLock

    @SuppressWarnings("try")
    public static <T> T callWithLock(final Lock lock,
                                     long timeout,
                                     @Nullable TimeUnit timeUnit,
                                     @Nullable final Supplier<RuntimeException> onErrorThrow,
                                     Callable<T> callable) throws LockAquireTimeoutException, Exception {
        try (AutoCloseable x = acquireLock(lock, timeout, timeUnit, onErrorThrow)) {
            return callable.call();
        }
    }

    public static <T> T callWithLock(final Lock lock,
                                     long timeout,
                                     @Nullable TimeUnit timeUnit,
                                     Callable<T> callable) throws LockAquireTimeoutException, Exception {
        return callWithLock(lock, timeout, timeUnit, null, callable);
    }

    public static <T> T callWithLock(final Lock lock,
                                     @Nullable final Supplier<RuntimeException> onErrorThrow,
                                     Callable<T> callable) throws LockAquireTimeoutException, Exception {
        return callWithLock(lock, 0L, null, onErrorThrow, callable);
    }

    public static <T> T callWithLock(final Lock lock,
                                     Callable<T> callable) throws LockAquireTimeoutException, Exception {
        return callWithLock(lock, null, callable);
    }

    public static <T> T sneakyCallWithLock(final Lock lock,
                                           long timeout,
                                           @Nullable TimeUnit timeUnit,
                                           @Nullable final Supplier<RuntimeException> onErrorThrow,
                                           Callable<T> callable) throws LockAquireTimeoutException {
        return wrapUncheckedExceptions( () -> callWithLock(lock, timeout, timeUnit, onErrorThrow, callable));
    }

    public static <T> T sneakyCallWithLock(final Lock lock,
                                     long timeout,
                                     @Nullable TimeUnit timeUnit,
                                     Callable<T> callable) throws LockAquireTimeoutException {
        return sneakyCallWithLock(lock, timeout, timeUnit, null, callable);
    }

    public static <T> T sneakyCallWithLock(final Lock lock,
                                     @Nullable final Supplier<RuntimeException> onErrorThrow,
                                     Callable<T> callable) throws LockAquireTimeoutException {
        return sneakyCallWithLock(lock, 0L, null, onErrorThrow, callable);
    }

    public static <T> T sneakyCallWithLock(final Lock lock,
                                     Callable<T> callable) throws LockAquireTimeoutException {
        return sneakyCallWithLock(lock, null, callable);
    }

    // runWithLock

    @SuppressWarnings("try")
    public static void runWithLock(final Lock lock,
                                       long timeout,
                                       @Nullable TimeUnit timeUnit,
                                       @Nullable final Supplier<RuntimeException> onErrorThrow,
                                       Process<?> process) throws LockAquireTimeoutException, Exception {
        try (AutoCloseable x = acquireLock(lock, timeout, timeUnit, onErrorThrow)) {
            process.work();
        }
    }

    public static void runWithLock(final Lock lock,
                                       long timeout,
                                       @Nullable TimeUnit timeUnit,
                                       Process<?> process) throws LockAquireTimeoutException, Exception {
        runWithLock(lock, timeout, timeUnit, null, process);
    }

    public static void runWithLock(final Lock lock,
                                       @Nullable final Supplier<RuntimeException> onErrorThrow,
                                       Process<?> process) throws LockAquireTimeoutException, Exception {
        runWithLock(lock, 0L, null, onErrorThrow, process);
    }

    public static void runWithLock(final Lock lock,
                                       Process<?> process) throws LockAquireTimeoutException, Exception {
        runWithLock(lock, null, process);
    }

    public static void sneakyRunWithLock(final Lock lock,
                                             long timeout,
                                             @Nullable TimeUnit timeUnit,
                                             @Nullable final Supplier<RuntimeException> onErrorThrow,
                                             Process<?> process) throws LockAquireTimeoutException {
        wrapUncheckedExceptions(() -> {
            runWithLock(lock, timeout, timeUnit, onErrorThrow, process);
        });
    }

    public static void sneakyRunWithLock(final Lock lock,
                                       long timeout,
                                       @Nullable TimeUnit timeUnit,
                                       Process<?> process) throws LockAquireTimeoutException {
        sneakyRunWithLock(lock, timeout, timeUnit, null, process);
    }

    public static void sneakyRunWithLock(final Lock lock,
                                       @Nullable final Supplier<RuntimeException> onErrorThrow,
                                       Process<?> process) throws LockAquireTimeoutException {
        sneakyRunWithLock(lock, 0L, null, onErrorThrow, process);
    }

    public static void sneakyRunWithLock(final Lock lock,
                                       Process<?> process) throws LockAquireTimeoutException {
        sneakyRunWithLock(lock, null, process);
    }

}
