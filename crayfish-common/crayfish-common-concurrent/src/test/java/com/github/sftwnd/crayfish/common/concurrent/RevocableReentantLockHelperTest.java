/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.concurrent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void revokeTest() {
        final Revoker revoker = new Revoker();
        final RevocableReentantLockHelper revocableLock = constructLock(revoker::get);
        final AtomicInteger value = new AtomicInteger(0);
        assertThrows( RevokedException.class,
                      () -> LockUtils.runWithLock(
                              revocableLock
                              , () -> {
                                  value.incrementAndGet(); // value = 1
                                  LockUtils.sneakyRunWithLock(revocableLock, value::incrementAndGet); // value = 2
                                  revocableLock.revokeUntil(Instant.now().minusSeconds(10));
                                  LockUtils.sneakyRunWithLock(revocableLock, value::incrementAndGet); // value = 3
                                  revocableLock.revokeUntil(Instant.now());
                                  LockUtils.sneakyRunWithLock(revocableLock, value::incrementAndGet); // unaccessible
                              }
                           )
                    );
        assertEquals(3, value.get());

    }

    private static RevocableReentantLockHelper constructLock(Supplier<Instant> revoker) {
        return new RevocableReentantLockHelper(new ReentrantLock(), revoker);
    }

    @NoArgsConstructor
    class Revoker {
        @Getter @Setter
        private volatile Instant instant = Instant.MIN;
        public Instant get() {
            return this.instant;
        }
    }

}