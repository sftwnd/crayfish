package com.github.sftwnd.crayfish.distributed.core.resource;

import com.github.sftwnd.crayfish.common.info.BaseNamedInfo;
import com.github.sftwnd.crayfish.common.info.NamedInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Resource<I extends ResourceInfo> extends BaseNamedInfo<I> implements Cloneable {

    public Resource(@Nonnull String name) {
        this(name, null);
    }

    public Resource(@Nonnull String name, @Nullable I info) {
        super(name, info);
    }

    public Resource(@Nonnull NamedInfo<I> named) {
        super(named.getName(), named.getInfo());
    }

    @Override
    public Object clone() {
        return new Resource<>(this);
    }

}

