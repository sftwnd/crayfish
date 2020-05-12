/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.concurrent;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

@Slf4j
public class RevocableReentantLockHelper implements Lock, Revocable {

    @SuppressWarnings("squid:S5164")
    private final ThreadLocal<AtomicInteger> acquires = ThreadLocal.withInitial(() -> new AtomicInteger(0));

    private final String logName;
    private final Lock lockHelper;
    @Getter
    private final Supplier<Instant>        statusMonitor;
    @SuppressWarnings("squid:S5164")
    private final ThreadLocal<Instant>     initAt    = ThreadLocal.withInitial(() -> Instant.MIN);
    private final AtomicReference<Instant> revokedAt = new AtomicReference<>(Instant.MIN);
    @SuppressWarnings("squid:S5164")
    private final ThreadLocal<Long>        checkedAt = ThreadLocal.withInitial(() -> 0L);

    public RevocableReentantLockHelper(@Nonnull Lock lockHelper) {
        this(lockHelper, null);
    }

    public RevocableReentantLockHelper(@Nonnull Lock lockHelper, @Nullable Supplier<Instant> statusMonitor) {
        Objects.requireNonNull(lockHelper);
        logName = String.format("%s@%s", this.getClass().getSimpleName(), Integer.toHexString(this.hashCode()));
        this.lockHelper = lockHelper;
        this.statusMonitor = statusMonitor == null ? () -> Instant.MIN : () -> Objects.requireNonNullElse(statusMonitor.get(), Instant.MIN);
    }

    private void preacquire() {
        synchronized (revokedAt) {
            if (acquires.get().intValue() == 0) {
                initAt.set(Instant.now());
                if (!revokedAt.get().equals(statusMonitor.get())) {
                    revokedAt.set(statusMonitor.get());
                    logger.trace("{} revokedAt flag changed to: {}", logName, revokedAt.get());
                }
                return;
            }
            checkRevoked("::preacquire", false);
        }
    }

    private void acquired() {
        checkRevoked("::acquired", true);
        synchronized (revokedAt) {
            // Подразумевается, что есть маааленький slice временя между первым успешным взятием блокировки и очисткой флага revoked,
            // когда флаг может быть установлен триггером, и после этого без проверки опять сброшен. Посредством определения метода в
            // clearFlag можно задать правильное значение
            logger.trace("{} acquires: {}", logName, acquires.get().incrementAndGet());
        }
    }

    private void released() {
        synchronized (revokedAt) {
            if (acquires.get().intValue() > 0) {
                @SuppressWarnings("squid:HiddenFieldCheck")
                final int _acquires = acquires.get().decrementAndGet();
                try {
                    logger.trace("{} has been released {} to: {} acquires", logName, _acquires == 0 ? "completely" : "", _acquires);
                    checkRevoked("::released", false);
                } finally {
                    if (_acquires == 0) {
                        this.initAt.set(Instant.MIN);
                    }
                }
            } else {
                logger.trace("{} is not acquired", logName);
            }
        }
    }

    @Override
    public void revokeUntil(@Nonnull Instant limit) {
        Objects.requireNonNull(limit);
        synchronized (revokedAt) {
            try {
                if (acquires.get().intValue() > 0) {
                    if (this.revokedAt.get().isBefore(initAt.get())) {
                        logger.trace("{}::revokeUntil(): lock has been revoked and unable until {}", logName, limit);
                    } else {
                        logger.trace("{}::revokeUntil(): lock is already revoked. Lock is unable until {}", logName, limit);
                    }
                } else {
                    logger.trace("{}::revokeUntil(): lock is not acquired. Lock is unable until {}", logName, limit);
                }
            } finally {
                if (!revokedAt.get().equals(limit)) {
                    revokedAt.set(limit);
                    revokedAt.notifyAll();
                }
            }
        }
    }

    @SuppressWarnings("squid:S1181")
    private void checkRevoked(final String callerName, final boolean acquired) {
        synchronized (revokedAt) {
            if (revokedAt.get().isAfter(initAt.get())) {
                if (acquires.get().intValue() == 0) {
                    logger.trace("{} is checked as revoked by {} at {}. Lock is not possible", this.logName, callerName, revokedAt.get());
                } else {
                    logger.trace("{} is checked as revoked by {} at {}", this.logName, callerName, revokedAt.get());
                }
                throw new RevokedException(String.format("Lock [%s] has been revoked at %s", logName, revokedAt.get()));
            }
            checkedAt.set(System.currentTimeMillis());
        }
    }

    @Override
    @SuppressWarnings("squid:S1181")
    public void lock() {
        preacquire();
        lockHelper.lock();
        acquired();
    }

    @Override
    @SuppressWarnings("squid:S1181")
    public void lockInterruptibly() throws InterruptedException {
        try {
            preacquire();
            lockHelper.lockInterruptibly();
            acquired();
        } catch (InterruptedException itex) {
            Thread.currentThread().interrupt();
            throw itex;
        }
    }

    @Override
    @SuppressWarnings("squid:S1181")
    public boolean tryLock() {
        preacquire();
        if (lockHelper.tryLock()) {
            acquired();
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("squid:S1181")
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        try {
            preacquire();
            if (lockHelper.tryLock(time, unit)) {
                acquired();
                return true;
            }
        } catch (InterruptedException itex) {
            Thread.currentThread().interrupt();
            throw itex;
        }
        return false;
    }

    @Override
    public void unlock() {
        try {
            lockHelper.unlock();
        } finally {
            released();
        }
    }

    @Override
    public Condition newCondition() {
        return lockHelper.newCondition();
    }

}
