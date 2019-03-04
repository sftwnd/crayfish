package com.github.sftwnd.crayfish.distributed.core.resource;

public interface ResourceManagerMBean {

    long getReloadCount();
    long getReloadTime();
    long getReloadErrorCount();

}
