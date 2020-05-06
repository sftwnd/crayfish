package com.github.sftwnd.crayfish.common.crc;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.Objects;

@AllArgsConstructor
@EqualsAndHashCode
public class CrcDescriprion {

    @Getter protected final int     width;
    @Getter protected final long    poly;
    @Getter protected final long    init;
    @Getter protected final boolean refin;
    @Getter protected final boolean refot;
    @Getter protected final long    xorot;

    public CrcDescriprion(@Nonnull final CrcDescriprion model) {
        this(Objects.requireNonNull(model, "CRC_model_d::new - model is null").getWidth(),
             model.getPoly(), model.getInit(), model.isRefin(), model.isRefot(), model.getXorot());
    }

    @Generated
    @Override
    public String toString() {
        return "CRC-" + getWidth()+"/P"+Long.toHexString(poly).toUpperCase()
             + "_I" +(init == 0 ? "0" : Long.toHexString(init).toUpperCase())
             + (refin ? "_RI" : "")
             + (refot ? "_RO" : "")
             + (xorot == 0 ? "" : "_X"+Long.toHexString(xorot).toUpperCase());
    }

}
