package com.github.sftwnd.crayfish.zookeeper.concurrent;

import com.github.sftwnd.crayfish.common.concurrent.RevocableReentantLockHelper;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;

public class ZookeeperReentantLock extends RevocableReentantLockHelper {

    public ZookeeperReentantLock(@Nonnull InterProcessMutex interProcessMutex, @Nonnull Supplier<Instant> statusMonitor) {
        super(new ZookeeperLockHelper(interProcessMutex), Objects.requireNonNull(statusMonitor));
    }

}
