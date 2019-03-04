package com.github.sftwnd.crayfish.distributed.core.instance;


import com.github.sftwnd.crayfish.common.info.NamedInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class Instance<I extends InstanceInfo> extends NamedInfo<I> {

    public Instance(@Nonnull String name) {
        this(name, null);
    }

    public Instance(@Nullable I info) {
        super(UUID.randomUUID().toString(), info);
    }

    public Instance(@Nonnull String name, @Nullable I info) {
        super(name, info);
    }

    @SuppressWarnings("unchecked")
    public Instance(@Nonnull Instance<I> instance) {
        super(instance.getName(), instance.getInfo() == null ? null : (I)instance.getInfo().clone());
    }

    @Override
    public Object clone() {
        return new Instance<>(this);
    }

}

