package com.github.sftwnd.crayfish.common.info;

import com.github.sftwnd.crayfish.common.json.JsonMapper;
import com.github.sftwnd.crayfish.common.utl.CRCUtl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class NamedInfo<I extends Cloneable> extends Named implements Informed<I> {

    private I info;

    public NamedInfo() {
        this(CRCUtl.getCrc32(UUID.randomUUID().toString()), null);
        this.name = new StringBuilder(this.getClass().getSimpleName()).append('$').append(name).toString();
    }

    public NamedInfo(@Nonnull String name) {
        this(name, null);
    }

    public NamedInfo(@Nonnull String name,  @Nullable I info) {
        super(name);
        this.info = info;
    }

    public I getInfo() {
        return this.info;
    }

    public void setInfo(@Nullable I info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return JsonMapper.snakySerializeObject(this);
    }

}
