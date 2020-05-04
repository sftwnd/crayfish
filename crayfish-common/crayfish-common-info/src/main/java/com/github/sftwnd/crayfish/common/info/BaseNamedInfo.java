package com.github.sftwnd.crayfish.common.info;

import com.github.sftwnd.crayfish.common.crc.CrcModel;
import com.github.sftwnd.crayfish.common.json.JsonMapper;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.util.UUID;

public class BaseNamedInfo<I> extends BaseNamed implements NamedInfo<I> {

    @Getter @Setter private I info;

    public BaseNamedInfo() {
        this(new StringBuilder(MethodHandles.lookup().lookupClass().getSimpleName())
                     .append('$').append(Long.toHexString(CrcModel.CRC64_XZ.getCRC(UUID.randomUUID().toString().getBytes()).getCrc()).toUpperCase())
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
    public String toString() {
        return JsonMapper.snakySerializeObject(this);
    }

}
