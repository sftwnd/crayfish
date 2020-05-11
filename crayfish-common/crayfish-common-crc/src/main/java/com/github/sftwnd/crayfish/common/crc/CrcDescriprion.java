package com.github.sftwnd.crayfish.common.crc;

import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.Objects;

@RequiredArgsConstructor
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

    @SuppressWarnings({
            // squil:S116 Field names should comply with a naming convention
            // We have save the names of original C algorithm
            "squid:S116",
            // squid:S3077 Non-primitive fields should not be "volatile"
            // table items are not mutable and use of table is controlled in CrcModel carefully
            "squid:S3077"
    })
    protected volatile long[] table_byte = null;

}
