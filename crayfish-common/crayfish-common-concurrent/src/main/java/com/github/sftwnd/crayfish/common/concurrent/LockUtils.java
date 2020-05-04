package com.github.sftwnd.crayfish.common.concurrent;

import com.github.sftwnd.crayfish.common.base.Holder;
import com.github.sftwnd.crayfish.common.exception.ExceptionUtils;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Функции упрощающие работу с блокировками.
 */

@SuppressWarnings({
        // Throws declarations should not be superfluous
        //
        "squid:RedundantThrowsDeclarationCheck",
        // Locks should be released
        // Сонар ошибается. Все установленные блокировки снимаются.
        "squid:S2222"
})
public final class LockUtils {

    private LockUtils() {}

    /**
     * Константа задающая таймаут означающий, что нужно использовать функцию {@link Lock#tryLock()} вместо
     * {@link Lock#tryLock(long, TimeUnit)}.
     */
    public static final long TRY_LOCK_WITHOUT_TIMEOUT_TIMEOUT = -1;

    /**
     * Исключение выбрасываемое при таймауте получения блокировки.
     */
    @SuppressWarnings("serial")
    public static class LockAquireTimeoutException extends RuntimeException {}

    /**
     * Выполняет указанное действие внутри блокировки:
     * <ol>
     * <li>Захватывает указанную в параметре <code>lock</code> блокировку.</li>
     * <li>После этого выполняет действие заданное параметром <code>callable</code>.</li>
     * <li>Затем гарантированно освобождает полученную блокировку, даже если на этапе выполнения действия
     *     <code>callable.call()</code> произошло исключение.</li>
     * </ol>
     * <em>Ожидает получение указанной блокировки безусловно.</em>
     * @param lock Блокировка которую необходимо получить перед выполнением заданного действия <code>callable</code>.
     * @param callable Действтие, которое необходимо выполнить после установки указанной блокировки.
     * @param <T> Задаёт тип значения возвращаемого выполняемым действием.
     * @return Значение возвращаённое выполняемым действием.
     * @throws Exception Если при выполнении действия <code>callable.call()</code> возникает исключение,
     *                   то оно свободно выпускается наружу.
     */
    public static <T> T callWithLock(Lock lock, Callable<T> callable) throws Exception {
        return callWithLock(lock, 0, null, callable);
    }

    /**
     * Выполняет указанное действие внутри блокировки:
     * <ol>
     * <li>Захватывает указанную в параметре <code>lock</code> блокировку.</li>
     * <li>После этого выполняет действие заданное параметром <code>callable</code>.</li>
     * <li>Затем гарантированно освобождает полученную блокировку, даже если на этапе выполнения действия
     *     <code>callable.call()</code> произошло исключение.</li>
     * </ol>
     * <em>Ожидает получение блокировки в течение указанного таймаута.</em>
     * @param lock Блокировка которую необходимо получить перед выполнением заданного действия <code>callable</code>.
     * @param timeout Таймаут задающий время в течение кторого ожидатся получение блокировки. Если значение этого
     *                параметра равно {@link #TRY_LOCK_WITHOUT_TIMEOUT_TIMEOUT}, то будет сделана пропытка моментального
     *                получения блокировки ({@link Lock#tryLock()}).
     * @param timeUnit Задаёт единицы в которых задан параметр <code>timeout</code>. Если не задан (<code>null</code>),
     *                 то будет использована безусловная блокировка без таймаута ({@link Lock#lock()}).
     * @param callable Действтие, которое необходимо выполнить после установки указанной блокировки.
     * @param <T> Задаёт тип значения возвращаемого выполняемым действием.
     * @return Значение возвращаённое выполняемым действием.
     * @throws LockAquireTimeoutException Будет выброшено в случае если не удалось получить блокировку в течение
     *                                    указанного периода времени.
     * @throws Exception Если при выполнении действия <code>callable.call()</code> возникает исключение,
     *                   то оно свободно выпускается наружу.
     */
    public static <T> T callWithLock(Lock lock,
                                     long timeout,
                                     @Nullable TimeUnit timeUnit,
                                     Callable<T> callable) throws LockAquireTimeoutException, Exception {
        return callWithLock(lock, timeout, timeUnit, null, callable);
    }

    /**
     * Выполняет указанное действие внутри блокировки:
     * <ol>
     * <li>Захватывает указанную в параметре <code>lock</code> блокировку.</li>
     * <li>После этого выполняет действие заданное параметром <code>callable</code>.</li>
     * <li>Затем гарантированно освобождает полученную блокировку, даже если на этапе выполнения действия
     *     <code>callable.call()</code> произошло исключение.</li>
     * </ol>
     * <em>Ожидает получение блокировки в течение указанного таймаута.</em>
     * @param lock Блокировка которую необходимо получить перед выполнением заданного действия <code>callable</code>.
     * @param timeout Таймаут задающий время в течение кторого ожидатся получение блокировки. Если значение этого
     *                параметра равно {@link #TRY_LOCK_WITHOUT_TIMEOUT_TIMEOUT}, то будет сделана пропытка моментального
     *                получения блокировки ({@link Lock#tryLock()}).
     * @param timeUnit Задаёт единицы в которых задан параметр <code>timeout</code>. Если не задан (<code>null</code>),
     *                 то будет использована безусловная блокировка без таймаута ({@link Lock#lock()}).
     * @param timeoutError Экземпляр исключения, которое будет выброшено в случае если блокировку не удалось получить
     *                     в течение указанного таймаута. Если этот параметр не задан, то будет сгенерировано исключение
     *                     типа {@link LockAquireTimeoutException}.
     * @param callable Действтие, которое необходимо выполнить после установки указанной блокировки.
     * @param <T> Задаёт тип значения возвращаемого выполняемым действием.
     * @return Значение возвращаённое выполняемым действием.
     * @throws LockAquireTimeoutException Будет выброшено в случае если не удалось получить блокировку в течение
     *                                    указанного периода времени и не задан параметр <code>timeoutError</code>.
     * @throws Exception Если при выполнении действия <code>callable.call()</code> возникает исключение,
     *                   то оно свободно выпускается наружу.
     */
    @SuppressWarnings({
            // Sonar лажается: lock.unlock вызывается всегда
            "squid:S2222"
            // Вряд ли в данном случае кто-то будет долго искать где используется неиспользуемая переменная "x",
            // а написить более эффективный код для finally-действия без потери информации обо всех ошибках
            // без использования try-with-resources наверное нельзя. Читаемость и понятность кода не страдают.
            , "squid:S1481", "squid:S1854"
            // Generic exceptions should never be thrown
            // Sonar ошибается: мы выполняем произвольный Callable.call, а он может сегнерить Exception.
            , "squid:S00112"
            , "try"
    })
    public static <T> T callWithLock(final Lock lock,
                                     long timeout,
                                     @Nullable TimeUnit timeUnit,
                                     @Nullable RuntimeException timeoutError,
                                     Callable<T> callable) throws LockAquireTimeoutException, Exception {
        if (timeUnit == null) {
            lock.lock();
        } else {
            if (!(timeout == TRY_LOCK_WITHOUT_TIMEOUT_TIMEOUT ? lock.tryLock() : lock.tryLock(timeout, timeUnit)))
            {
                throw timeoutError == null ? new LockAquireTimeoutException() : timeoutError;
            }
        }

        try (AutoCloseable x = lock::unlock) {
            return callable.call();
        }
    }

    /**
     * Обёртка вокруг {@link #callWithLock(Lock, Callable)}, оборачивающая
     * возникающие при выполнении <code>callable.call()</code> checked исключения в {@link RuntimeException}.
     */
    public static <T> T callWithLockAndHideCheckedExceptions(Lock lock, Callable<T> callable) {
        return callWithLockAndHideCheckedExceptions(lock, 0, null, callable);
    }

    /**
     * Обёртка вокруг {@link #callWithLock(Lock, long, TimeUnit, Callable)}, оборачивающая
     * возникающие при выполнении <code>callable.call()</code> checked исключения в {@link RuntimeException}.
     */
    public static <T> T callWithLockAndHideCheckedExceptions(
            Lock lock,
            long timeout,
            @Nullable TimeUnit timeUnit,
            Callable<T> callable) throws LockAquireTimeoutException {
        return callWithLockAndHideCheckedExceptions(lock, timeout, timeUnit, null, callable);
    }

    /**
     * Обёртка вокруг {@link #callWithLock(Lock, long, TimeUnit, RuntimeException, Callable)}, оборачивающая
     * возникающие при выполнении <code>callable.call()</code> checked исключения в {@link RuntimeException}.
     */
    public static <T> T callWithLockAndHideCheckedExceptions(
            Lock lock,
            long timeout,
            @Nullable TimeUnit timeUnit,
            @Nullable RuntimeException timeoutError,
            Callable<T> callable) throws LockAquireTimeoutException {
        try {
            return callWithLock(lock, timeout, timeUnit, timeoutError, callable);
        } catch (Exception e) {
            return ExceptionUtils.uncheckExceptions(e);
        }
    }

    /**
     * Аналог {@link #callWithLock(Lock, Callable)} в котором действие задаётся через {@link Runnable} вместо
     * {@link Callable}.
     */
    public static void runWithLock(Lock lock, final Runnable runnable) {
        runWithLock(lock, 0, null, runnable);
    }

    /**
     * Аналог {@link #callWithLock(Lock, long, TimeUnit, Callable)} в котором действие задаётся через {@link Runnable} вместо
     * {@link Callable}.
     */
    public static void runWithLock(Lock lock,
                                   long timeout,
                                   @Nullable TimeUnit timeUnit,
                                   final Runnable runnable) throws LockAquireTimeoutException {
        runWithLock(lock, timeout, timeUnit, null, runnable);
    }

    /**
     * Аналог {@link #callWithLock(Lock, long, TimeUnit, RuntimeException, Callable)} в котором действие задаётся через {@link Runnable} вместо
     * {@link Callable}.
     */
    public static void runWithLock(Lock lock,
                                   long timeout,
                                   @Nullable TimeUnit timeUnit,
                                   @Nullable RuntimeException timeoutError,
                                   final Runnable runnable) throws LockAquireTimeoutException {
        callWithLockAndHideCheckedExceptions(lock, timeout, timeUnit, timeoutError, () -> {
            runnable.run();
            return null;
        });
    }


    //////////////////////////////////
    // ReentrantReadWriteLock
    //
    /**
     * Аналог {@link #callWithLock(Lock, Callable)} в котором в качестве блокировки используется
     * {@link ReentrantReadWriteLock} и получается блокировка на запись.
     */
    public static <T> T callWithWriteLock(ReentrantReadWriteLock lock, Callable<T> callable) throws Exception {
        return callWithWriteLock(lock, 0, null, callable);
    }

    /**
     * Аналог {@link #callWithLock(Lock, long, TimeUnit, Callable)} в котором в качестве блокировки используется
     * {@link ReentrantReadWriteLock} и получается блокировка на запись.
     * <p>См. примечание к {@link #callWithWriteLock(ReentrantReadWriteLock, long, TimeUnit, RuntimeException, Callable)}.</p>
     */
    public static <T> T callWithWriteLock(ReentrantReadWriteLock lock,
                                          long timeout,
                                          @Nullable TimeUnit timeUnit,
                                          Callable<T> callable) throws LockAquireTimeoutException, Exception {
        return callWithWriteLock(lock, timeout, timeUnit, null, callable);
    }

    @SuppressWarnings("serial")
    private static class StolenReadLockException extends LockAquireTimeoutException {}

    /**
     * Аналог {@link #callWithLock(Lock, long, TimeUnit, RuntimeException, Callable)} в котором в качестве блокировки используется
     * {@link ReentrantReadWriteLock} и получается блокировка на запись.
     * <p><b>Примечание!</b><br/>Особенности реализации {@link ReentrantReadWriteLock} не позволяют получив блокировку на чтение, получить
     * затем блокировку на запись. Для того что-бы получить блокировку на запись надо что-бы были сняты все блокировки
     * на чтение, в том числе и собственные (установленные в том-же треде). Поэтому собственную блокировку на чтение
     * (если она была) необходимо отпустить. Т.к. блокировка на запись получается после отпускания блокировки на чтение,
     * то есть вероятность, что другой тред успеет вклиниться и получить нашу блокировку на запись.
     * Поэтому при получении блокировки на запись всегда нужно проверять изменились ли данные,
     * которые защищала блокировка на чтение. После отпускания блокировки на запись, блокировка на чтение (если была
     * установлена) восстанавливается в изначальное состояние без каких либо накладок и пауз: блокировка на чтение
     * устанавливается до снятия блокировки на запись (устанавливать блокировку на чтение после установки блокировки на
     * запись {@link ReentrantReadWriteLock} позволяет).
     * </p>
     */
    @SuppressWarnings({
            // Вряд ли в данном случае кто-то будет долго искать где используется неиспользуемая переменная "x",
            // а написить более эффективный код для finally-действия без потери информации обо всех ошибках
            // без использования try-with-resources наверное нельзя. Читаемость и понятность кода не страдают.
            "squid:S1481", "squid:S1854"
            // Здесь (e != markException) нужна именно reference equality
            , "squid:S1698"
            , "try"
    })
    public static <T> T callWithWriteLock(final ReentrantReadWriteLock lock,
                                          long timeout,
                                          @Nullable TimeUnit timeUnit,
                                          @Nullable RuntimeException timeoutError,
                                          final Callable<T> callable) throws LockAquireTimeoutException, Exception {
        final Holder<Integer> readLockCount = new Holder<>(0);
        while (lock.getReadHoldCount() > 0) {
            lock.readLock().unlock();
            readLockCount.value++;
        }

        try (AutoCloseable x = () -> {
            while (readLockCount.value > 0) {
                // Восстанавливаем количество блокировок на чтение
                lock.readLock().lock();
                readLockCount.value--;
            }
        }) {
            Callable<T> doIt = () -> {
                try (AutoCloseable x1 = () -> {
                    if (readLockCount.value > 0) {
                        // Downgrade by acquiring read lock before releasing write lock
                        lock.readLock().lock();
                        readLockCount.value--;
                    }
                }) {
                    return callable.call();
                }
            };


            if (readLockCount.value > 0) {
                StolenReadLockException markException = new StolenReadLockException();
                try {
                    // Сразу пробуем прорваться вперёд всех: см. доку к ReentrantReadWriteLock.WriteLock.tryLock() (этот метод нарушает соглашения об очерёдности)
                    return callWithLock(lock.writeLock(), TRY_LOCK_WITHOUT_TIMEOUT_TIMEOUT, timeUnit, markException, doIt);
                } catch (StolenReadLockException e) {
                    if (e != markException) {
                        throw e;
                    }
                }
            }

            // Если не получилось получить блокировку вперёд всех, то пробуем получить её штатным образом.
            return callWithLock(lock.writeLock(), timeout, timeUnit, timeoutError, doIt);
        }
    }


    /**
     * Обёртка вокруг {@link #callWithWriteLock(ReentrantReadWriteLock, Callable)}, оборачивающая
     * возникающие при выполнении <code>callable.call()</code> checked исключения в {@link RuntimeException}.
     * <p>
     * {@link #callWithWriteLock(ReentrantReadWriteLock, long, TimeUnit, RuntimeException, Callable) Здесь}
     * смотри важное примечание касающееся особенностей {@link ReentrantReadWriteLock}.
     * </p>
     */
    public static <T> T callWithWriteLockAndHideCheckedExceptions(ReentrantReadWriteLock lock, Callable<T> callable) {
        return callWithWriteLockAndHideCheckedExceptions(lock, 0, null, callable);
    }

    /**
     * Обёртка вокруг {@link #callWithWriteLock(ReentrantReadWriteLock, long, TimeUnit, Callable)}, оборачивающая
     * возникающие при выполнении <code>callable.call()</code> checked исключения в {@link RuntimeException}.
     * <p>
     * {@link #callWithWriteLock(ReentrantReadWriteLock, long, TimeUnit, RuntimeException, Callable) Здесь}
     * смотри важное примечание касающееся особенностей {@link ReentrantReadWriteLock}.
     * </p>
     */
    public static <T> T callWithWriteLockAndHideCheckedExceptions(
            ReentrantReadWriteLock lock,
            long timeout,
            @Nullable TimeUnit timeUnit,
            Callable<T> callable) throws LockAquireTimeoutException {
        return callWithWriteLockAndHideCheckedExceptions(lock, timeout, timeUnit, null, callable);
    }

    /**
     * Обёртка вокруг {@link #callWithWriteLock(ReentrantReadWriteLock, long, TimeUnit, RuntimeException, Callable)}, оборачивающая
     * возникающие при выполнении <code>callable.call()</code> checked исключения в {@link RuntimeException}.
     * <p>
     * {@link #callWithWriteLock(ReentrantReadWriteLock, long, TimeUnit, RuntimeException, Callable) Здесь}
     * смотри важное примечание касающееся особенностей {@link ReentrantReadWriteLock}.
     * </p>
     */
    public static <T> T callWithWriteLockAndHideCheckedExceptions(
            ReentrantReadWriteLock lock,
            long timeout,
            @Nullable TimeUnit timeUnit,
            @Nullable RuntimeException timeoutError,
            Callable<T> callable) throws LockAquireTimeoutException {
        try {
            return callWithWriteLock(lock, timeout, timeUnit, timeoutError, callable);
        } catch (Exception e) {
            return ExceptionUtils.uncheckExceptions(e);
        }
    }


    /**
     * Аналог {@link #callWithWriteLock(ReentrantReadWriteLock, Callable)} в котором действие задаётся через {@link Runnable} вместо
     * {@link Callable}.
     * <p>
     * {@link #callWithWriteLock(ReentrantReadWriteLock, long, TimeUnit, RuntimeException, Callable) Здесь}
     * смотри важное примечание касающееся особенностей {@link ReentrantReadWriteLock}.
     * </p>
     */
    public static void runWithWriteLock(ReentrantReadWriteLock lock, final Runnable runnable) {
        runWithWriteLock(lock, 0, null, runnable);
    }

    /**
     * Аналог {@link #callWithWriteLock(ReentrantReadWriteLock, long, TimeUnit, Callable)} в котором действие задаётся через {@link Runnable} вместо
     * {@link Callable}.
     * <p>
     * {@link #callWithWriteLock(ReentrantReadWriteLock, long, TimeUnit, RuntimeException, Callable) Здесь}
     * смотри важное примечание касающееся особенностей {@link ReentrantReadWriteLock}.
     * </p>
     */
    public static void runWithWriteLock(ReentrantReadWriteLock lock,
                                        long timeout,
                                        @Nullable TimeUnit timeUnit,
                                        final Runnable runnable) throws LockAquireTimeoutException {
        runWithWriteLock(lock, timeout, timeUnit, null, runnable);
    }

    /**
     * Аналог {@link #callWithWriteLock(ReentrantReadWriteLock, long, TimeUnit, RuntimeException, Callable)} в котором действие задаётся через {@link Runnable} вместо
     * {@link Callable}.
     * <p>
     * {@link #callWithWriteLock(ReentrantReadWriteLock, long, TimeUnit, RuntimeException, Callable) Здесь}
     * смотри важное примечание касающееся особенностей {@link ReentrantReadWriteLock}.
     * </p>
     */
    public static void runWithWriteLock(ReentrantReadWriteLock lock,
                                        long timeout,
                                        @Nullable TimeUnit timeUnit,
                                        @Nullable RuntimeException timeoutError,
                                        final Runnable runnable) throws LockAquireTimeoutException {
        callWithWriteLockAndHideCheckedExceptions(lock, timeout, timeUnit, timeoutError, () -> {
            runnable.run();
            return null;
        });
    }




    /**
     * Аналог {@link #callWithLock(Lock, Callable)} в котором в качестве блокировки используется
     * {@link ReentrantReadWriteLock} и получается блокировка на чтение.
     */
    public static <T> T callWithReadLock(ReentrantReadWriteLock lock, Callable<T> callable) throws Exception {
        return callWithLock(lock.readLock(), 0, null, callable);
    }

    /**
     * Аналог {@link #callWithLock(Lock, long, TimeUnit, Callable)} в котором в качестве блокировки используется
     * {@link ReentrantReadWriteLock} и получается блокировка на чтение.
     */
    public static <T> T callWithReadLock(ReentrantReadWriteLock lock,
                                         long timeout,
                                         @Nullable TimeUnit timeUnit,
                                         Callable<T> callable) throws LockAquireTimeoutException, Exception {
        return callWithLock(lock.readLock(), timeout, timeUnit, callable);
    }

    /**
     * Аналог {@link #callWithLock(Lock, long, TimeUnit, RuntimeException, Callable)} в котором в качестве блокировки используется
     * {@link ReentrantReadWriteLock} и получается блокировка на чтение.
     */
    public static <T> T callWithReadLock(ReentrantReadWriteLock lock,
                                         long timeout,
                                         @Nullable TimeUnit timeUnit,
                                         @Nullable RuntimeException timeoutError,
                                         Callable<T> callable) throws LockAquireTimeoutException, Exception {
        return callWithLock(lock.readLock(), timeout, timeUnit, timeoutError, callable);
    }


    /**
     * Обёртка вокруг {@link #callWithReadLock(ReentrantReadWriteLock, Callable)}, оборачивающая
     * возникающие при выполнении <code>callable.call()</code> checked исключения в {@link RuntimeException}.
     */
    public static <T> T callWithReadLockAndHideCheckedExceptions(ReentrantReadWriteLock lock, Callable<T> callable) {
        return callWithLockAndHideCheckedExceptions(lock.readLock(), callable);
    }

    /**
     * Обёртка вокруг {@link #callWithReadLock(ReentrantReadWriteLock, long, TimeUnit, Callable)}, оборачивающая
     * возникающие при выполнении <code>callable.call()</code> checked исключения в {@link RuntimeException}.
     */
    public static <T> T callWithReadLockAndHideCheckedExceptions(
            ReentrantReadWriteLock lock,
            long timeout,
            @Nullable TimeUnit timeUnit,
            Callable<T> callable) throws LockAquireTimeoutException {
        return callWithLockAndHideCheckedExceptions(lock.readLock(), timeout, timeUnit, callable);
    }

    /**
     * Обёртка вокруг {@link #callWithReadLock(ReentrantReadWriteLock, long, TimeUnit, RuntimeException, Callable)}, оборачивающая
     * возникающие при выполнении <code>callable.call()</code> checked исключения в {@link RuntimeException}.
     */
    public static <T> T callWithReadLockAndHideCheckedExceptions(
            ReentrantReadWriteLock lock,
            long timeout,
            @Nullable TimeUnit timeUnit,
            @Nullable RuntimeException timeoutError,
            Callable<T> callable) throws LockAquireTimeoutException {
        return callWithLockAndHideCheckedExceptions(lock.readLock(), timeout, timeUnit, timeoutError, callable);
    }


    /**
     * Аналог {@link #callWithReadLock(ReentrantReadWriteLock, Callable)} в котором действие задаётся через {@link Runnable} вместо
     * {@link Callable}.
     */
    public static void runWithReadLock(ReentrantReadWriteLock lock, final Runnable runnable) {
        runWithLock(lock.readLock(), runnable);
    }

    /**
     * Аналог {@link #callWithReadLock(ReentrantReadWriteLock, long, TimeUnit, Callable)} в котором действие задаётся через {@link Runnable} вместо
     * {@link Callable}.
     */
    public static void runWithReadLock(ReentrantReadWriteLock lock,
                                       long timeout,
                                       @Nullable TimeUnit timeUnit,
                                       final Runnable runnable) throws LockAquireTimeoutException {
        runWithLock(lock.readLock(), timeout, timeUnit, runnable);
    }

    /**
     * Аналог {@link #callWithReadLock(ReentrantReadWriteLock, long, TimeUnit, RuntimeException, Callable)} в котором действие задаётся через {@link Runnable} вместо
     * {@link Callable}.
     */
    public static void runWithReadLock(ReentrantReadWriteLock lock,
                                       long timeout,
                                       @Nullable TimeUnit timeUnit,
                                       @Nullable RuntimeException timeoutError,
                                       final Runnable runnable) throws LockAquireTimeoutException {
        runWithLock(lock.readLock(), timeout, timeUnit, timeoutError, runnable);
    }

}
