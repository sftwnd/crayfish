package com.github.sftwnd.crayfish.zookeeper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.Objects;

public class DefaultZookeeperService implements ZookeeperService {

    private static final byte[] EMPTY_VALUE = new byte[0];
    private ZookeeperConfig zookeeperConfig;
    @Getter @Setter(AccessLevel.PRIVATE)
    private CuratorFramework curatorFramework;

    public DefaultZookeeperService(final ZookeeperConfig zookeeperConfig) {
        Objects.requireNonNull(zookeeperConfig);
        this.zookeeperConfig = zookeeperConfig;
        setCuratorFramework(createCuratorFramework());
        getCuratorFramework().start();
    }

    @Override
    public CuratorFramework getCuratorFramework() {
        return this.curatorFramework;
    }

    @Override
    public  CuratorFramework createCuratorFramework() {
        return CuratorFrameworkFactory.builder()
                                      .connectString(zookeeperConfig.getConnectString())
                                      .connectionTimeoutMs(zookeeperConfig.getConnectionTimeoutMs())
                                      .sessionTimeoutMs(zookeeperConfig.getSessionTimeoutMs())
                                      .retryPolicy(
                                              new ExponentialBackoffRetry(
                                                      zookeeperConfig.getInitialRetryIntervalMs(),
                                                      zookeeperConfig.getMaxRetryCount(),
                                                      zookeeperConfig.getMaxRetryIntervalMs()))
                                      .defaultData(EMPTY_VALUE)
                                      .build();
    }

}