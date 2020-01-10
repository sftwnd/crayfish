package com.github.sftwnd.crayfish.zookeeper;

import com.github.sftwnd.crayfish.common.info.NamedInfo;

@FunctionalInterface
public interface NamedInfoOnThrowListener<I, X, T extends Throwable> {

    NamedInfo<I> get(NamedInfo<X> data, T throwable);

}
