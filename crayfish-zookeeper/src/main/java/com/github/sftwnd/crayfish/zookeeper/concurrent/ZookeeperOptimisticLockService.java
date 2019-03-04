package com.github.sftwnd.crayfish.zookeeper.concurrent;

import com.github.sftwnd.crayfish.zookeeper.ZookeeperService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 *
 *
 */
@Slf4j
public class ZookeeperOptimisticLockService extends ZookeeperLockService {

    private static final Duration CHECK_WEAK_DURATION = Duration.ofSeconds(10);

    private String prefix;
    private CuratorFramework curatorFramework;
    private ConcurrentHashMap<String, WeakReference<ZookeeperReentantLock>> locks = new ConcurrentHashMap<>();
    private ConnectionStateListener connectionStateListener;
    private volatile boolean closed = false;
    private volatile Instant checkWeakInstant = Instant.MIN;
    private volatile Instant stateInstant = Instant.MIN;

    public ZookeeperOptimisticLockService(@Nonnull ZookeeperService zookeeperService) {
        this(zookeeperService, null);
    }

    public ZookeeperOptimisticLockService(@Nonnull ZookeeperService zookeeperService, String prefix) {
        super(zookeeperService, prefix);
    }

    @Override
    @SuppressWarnings("Duplicates")
    protected void stateChanged(final CuratorFramework curatorFramework, final ConnectionState newState) {
        try {
            synchronized (this.locks) {
                switch (newState) {
                    case LOST:
                        stateInstant = Instant.MAX;
                        break;
                    case READ_ONLY:
                    case CONNECTED:
                        stateInstant = Instant.now();
                    default:
                        return;
                }
                final List<ZookeeperReentantLock> revokedLocks = this.locks.entrySet().stream()
                        .map(e -> e.getValue().get())
                        .filter(lock -> lock != null)
                        .peek(lock -> lock.revokeUntil(stateInstant))
                        .collect(Collectors.toList());
                if (revokedLocks.size() > 0) {
                    logger.trace("{}::stateChanged[{}], revoked locks: {}", this.getClass().getSimpleName(), newState, revokedLocks);
                    return;
                }
            }
        } finally {
            logger.trace("{}::stateChanged[{}]", this.getClass().getSimpleName(), newState);
        }
    }

}