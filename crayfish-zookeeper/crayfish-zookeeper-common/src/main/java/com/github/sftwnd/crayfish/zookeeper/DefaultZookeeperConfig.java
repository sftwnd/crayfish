package com.github.sftwnd.crayfish.zookeeper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *
 */

@Getter @Setter @AllArgsConstructor
public class DefaultZookeeperConfig implements ZookeeperConfig {

    private String connectString;
    private int connectionTimeoutMs;
    private int sessionTimeoutMs;
    private int initialRetryIntervalMs;
    private int maxRetryIntervalMs;
    private int maxRetryCount;

    public DefaultZookeeperConfig(String connectString) {
        this(connectString, 1000, 5000, 5, 750, 10);
    }

}
