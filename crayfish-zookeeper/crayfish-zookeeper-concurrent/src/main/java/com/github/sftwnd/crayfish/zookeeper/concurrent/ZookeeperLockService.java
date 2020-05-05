package com.github.sftwnd.crayfish.zookeeper.concurrent;

import com.github.sftwnd.crayfish.common.concurrent.LockService;
import com.github.sftwnd.crayfish.zookeeper.ZookeeperService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

/**
 *
 *
 */
@Slf4j
public class ZookeeperLockService implements LockService, Closeable {

    private static final Duration CHECK_WEAK_DURATION = Duration.ofSeconds(10);

    private String prefix;
    private CuratorFramework curatorFramework;
    private ConcurrentHashMap<String, WeakReference<ZookeeperReentantLock>> locks = new ConcurrentHashMap<>();
    private ConnectionStateListener connectionStateListener;
    private volatile boolean closed = false;
    private Instant checkWeakInstant = Instant.MIN;
    private Instant stateInstant = Instant.MIN;

    public ZookeeperLockService(@Nonnull ZookeeperService zookeeperService) {
        this(zookeeperService, null);
    }

    public ZookeeperLockService(@Nonnull ZookeeperService zookeeperService, String prefix) {
        Objects.requireNonNull(zookeeperService);
        this.curatorFramework = zookeeperService.getCuratorFramework();
        this.prefix = prefix == null ? "" : prefix.trim();
        this.connectionStateListener = this::stateChanged;
        curatorFramework.getConnectionStateListenable().addListener(connectionStateListener);
    }

    @SuppressWarnings("Duplicates")
    protected void stateChanged(final CuratorFramework curatorFramework, final ConnectionState newState) {
        synchronized (this.locks) {
            switch (newState) {
                case LOST:
                case SUSPENDED:
                    stateInstant = Instant.MAX;
                    break;
                default:
                    stateInstant = Instant.now();
            }
            // Поскольку мы имеем финальный шаг в stream, то peek сработает успешно
            @SuppressWarnings("squid:S3864")
            final List<ZookeeperReentantLock> revokedLocks = this.locks.entrySet().stream()
                    .map(e -> e.getValue().get())
                    .filter(Objects::nonNull)
                    .peek(lock -> lock.revokeUntil(stateInstant))
                    .collect(Collectors.toList());
            if (!revokedLocks.isEmpty()) {
                logger.trace("{}::stateChanged[{}], revoked locks: {}", this.getClass().getSimpleName(), newState, revokedLocks);
                return;
            }
        }
        logger.trace("{}::stateChanged[{}]", this.getClass().getSimpleName(), newState);
    }

    public Instant getStateInstant() {
        return stateInstant;
    }

    @Override
    public synchronized void close() throws IOException {
        if (closed) {
            return;
        }
        curatorFramework.getConnectionStateListenable().removeListener(connectionStateListener);
        closed = true;
        curatorFramework = null;
        prefix = null;
        connectionStateListener = null;
    }

    @Override
    public Lock getNamedLock(@Nonnull String name) {
        Objects.requireNonNull(name);
        synchronized (locks) {
            clearWeak();
            if (!locks.containsKey(name)) {
                ZookeeperReentantLock result = new ZookeeperReentantLock(new InterProcessMutex(curatorFramework, constructPath(name)), this::getStateInstant);
                locks.put(name, new WeakReference<>(result));
                return result;
            } else {
                WeakReference<ZookeeperReentantLock> ref = locks.get(name);
                ZookeeperReentantLock lock = ref.get();
                if (lock == null) {
                    locks.remove(name);
                    return getNamedLock(name);
                } else {
                    // Ну, типа, если между get(name) и get() GC почистил weak ссылку...
                    if (ref.get() == null) {
                        locks.put(name, new WeakReference<>(lock));
                    }
                    return lock;
                }
            }
        }
    }

    private final void clearWeak() {
        // Должно вызываться в синхронизационной секции по locks
        if (checkWeakInstant.plus(CHECK_WEAK_DURATION).isBefore(Instant.now())) {
            locks.entrySet().removeIf(e -> e.getValue().get() == null);
            checkWeakInstant = Instant.now();
        }
    }

    private String constructPath(String name) {
        return new StringBuilder(prefix).append(name).toString();
    }

}