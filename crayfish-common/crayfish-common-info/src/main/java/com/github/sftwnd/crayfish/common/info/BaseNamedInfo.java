package com.github.sftwnd.crayfish.common.info;

import com.github.sftwnd.crayfish.common.base.CRC64;
import com.github.sftwnd.crayfish.common.json.JsonMapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.util.UUID;

public class BaseNamedInfo<I> extends BaseNamed implements NamedInfo<I> {

    private I info;

    public BaseNamedInfo() {
        this(new StringBuilder(MethodHandles.lookup().lookupClass().getSimpleName())
                     .append('$').append(Long.toHexString(CRC64.hash(UUID.randomUUID().toString().getBytes())).toUpperCase())
                     .toString(), null);
    }

    public BaseNamedInfo(@Nonnull String name) {
        this(name, null);
    }

    public BaseNamedInfo(@Nonnull String name, @Nullable I info) {
        super(name);
        this.info = info;
    }

    @Override
    public I getInfo() {
        return this.info;
    }

    @Override
    public void setInfo(@Nullable I info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return JsonMapper.snakySerializeObject(this);
    }

}
