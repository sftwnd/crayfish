package com.github.sftwnd.crayfish.zookeeper.service;

import com.github.sftwnd.crayfish.common.resource.IResourceProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.retry.ExponentialBackoffRetry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Realize CuratorFramework support for native work with ZooKeeper.
 */
public interface ZookeeperService extends IResourceProvider<CuratorFramework> {

    /**
     * Close current connection if is defined
     */
    void close();

    /**
     * Create default Builder to construct CuratorFramework on request
     * @return Builder to build connection
     */
    static @Nonnull Builder builder() {
        return CuratorFrameworkFactory.builder()
                .connectionTimeoutMs(1000)
                .sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(5, 10, 750))
                .defaultData(new byte[]{});
    }

    /**
     * Create default Builder with defined connectString to construct CuratorFramework on request
     * @param connectString Connect string to Zookeeper
     * @return Builder to build connection
     */
    static @Nonnull Builder builder(@Nullable String connectString) {
        return Optional.ofNullable(connectString)
                .map(c -> builder().connectString(c))
                .orElseGet(ZookeeperService::builder);
    }

}
