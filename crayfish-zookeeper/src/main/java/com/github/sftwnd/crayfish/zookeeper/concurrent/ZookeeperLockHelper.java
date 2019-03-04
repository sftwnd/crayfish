package com.github.sftwnd.crayfish.zookeeper.concurrent;

import com.github.sftwnd.crayfish.common.exception.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

@Slf4j
public class ZookeeperLockHelper implements Lock {

    private static final Duration DEFAULT_TRY_DURATION = Duration.ofMillis (10);
    private final Duration tryDuration;
    private final InterProcessMutex interProcessMutex;

    ZookeeperLockHelper(Duration tryDuration, @Nonnull InterProcessMutex interProcessMutex) {
        Objects.requireNonNull(interProcessMutex);
        this.tryDuration = tryDuration == null ? DEFAULT_TRY_DURATION : tryDuration;
        this.interProcessMutex = interProcessMutex;
    }

    ZookeeperLockHelper(InterProcessMutex interProcessMutex) {
        this(null, interProcessMutex);
    }


    @Override
    public void lock() {
        try {
            logger.trace("::lock()");
            interProcessMutex.acquire();
            logger.trace("::lock() acquired");
        } catch (Throwable throwable) {
            logger.trace("::lock() unable to acquire with Exception: {}", throwable.getLocalizedMessage());
            ExceptionUtils.wrapUncheckedExceptions(() -> interProcessMutex.acquire());
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        try {
            logger.trace("::lockInterruptibly()");
            interProcessMutex.acquire();
            logger.trace("::lockInterruptibly() acquired");
        } catch (InterruptedException itex) {
            logger.trace("::lockInterruptibly() unable to acquire with InterruptedException");
            Thread.currentThread().interrupt();
            throw itex;
        } catch (Exception ex) {
            logger.trace("::lockInterruptibly() unable to acquire with Exceotion: {}", ex.getLocalizedMessage());
            ExceptionUtils.uncheckExceptions(ex);
        }
    }

    @Override
    public boolean tryLock() {
        logger.trace("::tryLock()");
        return ExceptionUtils.wrapUncheckedExceptions(() -> tryLock(tryDuration.toMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        logger.trace("::tryLock({}, {})", time, unit);
        try {
            if(time <= 0 || unit == null ? interProcessMutex.acquire(Long.MAX_VALUE, TimeUnit.MILLISECONDS) : interProcessMutex.acquire(time, unit)) {
                logger.trace("::tryLock({}, {}) acquired", time, unit);
                return true;
            }
        } catch (InterruptedException itex) {
            logger.trace("::tryLock({}, {}) unable to acquire with InterruptedException", time, unit);
            Thread.currentThread().interrupt();
            throw itex;
        } catch (Exception ex) {
            logger.trace("::tryLock({}, {}) unable to acquire with Exceotion: {}", time, unit, ex.getLocalizedMessage());
            return ExceptionUtils.uncheckExceptions(ex);
        }
        logger.trace("::tryLock({}, {}) unable to acquire", time, unit);
        return false;
    }

    @Override
    public void unlock() {
        logger.trace("::unlock()");
        try {
            interProcessMutex.release();
            logger.trace("::unlock() complete");
        } catch (Exception ex) {
            logger.trace("::unlock() throw the exception: {}", ex.getLocalizedMessage());
            ExceptionUtils.uncheckExceptions(ex);
        }
    }

    @Override
    public Condition newCondition() {
        logger.trace("::newCondition()");
        return ExceptionUtils.uncheckExceptions(new UnsupportedOperationException("UnsupportedOperationException ZookeeperLockHelper::newCondition"));
    }

}
