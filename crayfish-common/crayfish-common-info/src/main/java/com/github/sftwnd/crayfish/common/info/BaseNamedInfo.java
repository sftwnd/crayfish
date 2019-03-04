package com.github.sftwnd.crayfish.common.info;

import com.github.sftwnd.crayfish.common.json.JsonMapper;
import com.github.sftwnd.crayfish.common.utl.CRCUtl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class BaseNamedInfo<I extends Cloneable> extends BaseNamed implements NamedInfo<I> {

    private I info;

    public BaseNamedInfo() {
        this(CRCUtl.getCrc32(UUID.randomUUID().toString()), null);
        this.name = new StringBuilder(this.getClass().getSimpleName()).append('$').append(name).toString();
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
