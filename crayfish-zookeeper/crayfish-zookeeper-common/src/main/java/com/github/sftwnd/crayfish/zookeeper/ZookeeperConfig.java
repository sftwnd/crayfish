package com.github.sftwnd.crayfish.zookeeper;

/**
 * Настроечные параметры соединения с ZooKeeper
 */

public interface ZookeeperConfig {
    /**
     * Строка соединения в формате host:port[,host:port[...]]
     */
    String getConnectString();

    int getConnectionTimeoutMs();
    int getSessionTimeoutMs();

    int getInitialRetryIntervalMs();
    int getMaxRetryIntervalMs();
    int getMaxRetryCount();
}
