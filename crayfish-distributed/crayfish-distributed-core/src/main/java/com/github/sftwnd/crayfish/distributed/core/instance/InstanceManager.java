package com.github.sftwnd.crayfish.distributed.core.instance;

import com.github.sftwnd.crayfish.common.info.NamedInfoLoader;

public interface InstanceManager<I extends InstanceInfo> extends NamedInfoLoader<I> {

    Instance<I> getCurrentInstance();

}
