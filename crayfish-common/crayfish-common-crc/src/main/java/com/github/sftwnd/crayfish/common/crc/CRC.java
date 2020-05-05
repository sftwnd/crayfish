package com.github.sftwnd.crayfish.common.crc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

import static com.github.sftwnd.crayfish.common.crc.CrcModel.crc_general_combine;

@EqualsAndHashCode
public final class CRC implements Cloneable {

    @Getter private CrcModel model;
    @SuppressWarnings("squid:S1700")
    protected long crc;
    @Getter private int length = 0;

    public CRC(@Nonnull final CRC crc) {
        Objects.requireNonNull(crc, "CRC::new - crc is null");
        this.model = crc.model;
        this.crc = crc.crc;
        this.length = crc.length;
    }

    protected CRC(@Nonnull final CrcModel model, long crc, int length) {
        if (length < 0) {
            throw new IllegalArgumentException("CRC::new - length less than 0");
        }
        this.model = Objects.requireNonNull(model, "CRC::new - model is null");
        this.crc = crc;
        this.length = length;
    }

    protected CRC(@Nonnull final CrcModel model) {
        this(model, model.init, 0);
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("squid:S2975")
    public CRC clone() {
        return CRC.class.cast(super.clone());
    }

    public long getCrc() {
        return this.length == 0 ? model.getInit() : this.crc;
    }

    public CRC update(byte[] buff, int offset, int len) {
        if (buff != null && len > 0) {
            if (this.length == 0) {
                this.crc = model.init;
            }
            this.crc = model.crcBytewise(this.crc, buff, offset, len);
            this.length += len;
        }
        return this;
    }

    public CRC update(byte[] buff, int len) {
        return update(buff, 0, len);
    }

    public CRC update(byte[] buff) {
        return update(buff, buff.length);
    }

    public long combine(long crc2, int len2) {
        return crc_general_combine(
                  length == 0 ? model.init : getCrc(), crc2, len2,
                  model.getWidth(), model.getInit(), model.getPoly(), model.getXorot(), model.isRefot()
               );
    }

    public CRC combine(CRC crc) {
        Optional.ofNullable(crc)
                .filter(c -> c.length > 0)
                .ifPresent(c -> {
                         this.crc = combine(c.getCrc(), c.length);
                         this.length += c.length;
                 });
        return this;
    }

    @Override
    public String toString() {
        return this.getModel().toString()+"[crc:"+getCrc()+",len:"+getLength()+"]";
    }

}
