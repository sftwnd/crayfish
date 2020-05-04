package com.github.sftwnd.crayfish.common.crc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

@EqualsAndHashCode(callSuper = true)
public final class CrcModel extends CrcDescriprion {

    @Getter public  final String name;
    @Getter public  final Long check;
    @Getter private final CrcDescriprion crcDescriprion;

    private CrcModel(@Nullable final String name, final int width, final long poly, final long init, final boolean refin, final boolean refot, final long xorot, @Nullable final Long check) {
        this(name, new CrcDescriprion(width, poly, init, refin, refot, xorot), check);
    }

    private CrcModel(@Nonnull final String name, @Nonnull final CrcDescriprion model, @Nullable final Long check) {
        super(Objects.requireNonNull(_config(model), "CRC_model_t::new - model is null"));
        this.name = name == null ? super.toString() : name;
        this.check = check;
        this.crcDescriprion = model;
    }

    public long getPoly() {
        return crcDescriprion.getPoly();
    }

    public long getInit() {
        return crcDescriprion.getInit();
    }

    public boolean isRefot() {
        return crcDescriprion.isRefot();
    }

    public CRC getCRC() {
        CRC result = new CRC(this);
        _init();
        return result;
    }

    public CRC getCRC(byte[] buff, int offset, int len) {
        return getCRC().update(buff, offset, len);
    }

    public CRC getCRC(byte[] buff, int len) {
        return getCRC(buff, 0, len);
    }

    public CRC getCRC(byte[] buff) {
        return getCRC(buff, 0, buff == null ? 0 : buff.length);
    }

    public CRC getCRC(long crc, int len) {
        CRC result = new CRC(this, crc, len);
        _init();
        return result;
    }

    @Override
    public String toString() {
        return name;
    }

    protected void _init() {
        if (table_byte == null) {
            synchronized (this) {
                if (table_byte == null) {
                    table_byte = _createBytewiseTable();
                }
            }
        }
    }

    /**
     * Process the model data for th calculation.
     * constants will be eguals of originl values, but variables - changed
     */
    private static CrcDescriprion _config(@Nullable final CrcDescriprion model) {
        return model == null ? null
             : new CrcDescriprion(
                       model.width,
                       model.refin ? CrcModel.reflect(model.poly, model.width) : model.poly,
                       (model.refot ? CrcModel.reflect(model.init, model.width) : model.init) ^ model.xorot,
                       model.refin,
                       model.refot ^ model.refin,
                       model.xorot
                   );
    }

    private long[] table_byte = null;

    private long[] _createBytewiseTable() {
        long[] table = new long[256];
        long crc;
        for (int k = 0; k < table.length; k++) {
            crc = createBitwiseValue(k);
            if (refot) {
                crc = reflect(crc);
            }
            if (width < 8 && !refin) {
                crc <<= 8 - width;
            }
            table[k] = crc;
        }
        return table;
    }

    private long createBitwiseValue(int k) {
        /* if requested, return the initial CRC: if (k < 0) return init; */
        long poly = this.poly;
        /* pre-process the CRC */
        long crc = xorot;
        if (refot) {
            crc = reflect(crc);
        }
        /* process the input data a bit at a time */
        if (refin) {
            crc &= widmask(width);
            {
                crc ^= k;
                for (int i = 0; i < 8; i++) {
                    crc = ((crc & 1) != 0) ? (crc >>> 1) ^ poly : crc >>> 1;
                }

            }
        } else if (width <= 8) {
            int shift = 8 - width;           /* 0..7 */
            poly <<= shift;
            crc <<= shift;
            {
                crc ^= k;
                for (int i = 0; i < 8; i++) {
                    crc = (crc & 0x80) != 0 ? (crc << 1) ^ poly : crc << 1;
                }
            }
            crc >>= shift;
            crc &= widmask(width);
        } else {
            long mask = 1L << (width - 1);
            long shift = width - 8;           /* 1..WORDBITS-8 */
            {
                crc ^= ((long) k) << shift;
                for (int i = 0; i < 8; i++) {
                    crc = ((crc & mask) != 0) ? (crc << 1) ^ poly : crc << 1;
                }
            }
            crc &= widmask(width);
        }
        /* post-process and return the CRC */
        if (refot) {
            crc = reflect(crc);
        }
        return crc ^ xorot;
    }

    protected long crcBytewise(long crc, byte[] buf, int offset, int len) {
        /* if requested, return the initial CRC */
        if (buf == null)
            return init;

        /* pre-process the CRC */
        if (refot) {
            crc = reflect(crc);
        }

        /* process the input data a byte at a time */
        if (refin) {
            crc &= widmask(width);
            while (len-- > 0) {
                crc = (crc >>> 8) ^ table_byte[(int) ((crc ^ buf[offset++]) & 0xff)];
            }
        } else if (width <= 8) {
            int shift = 8 - width;           /* 0..7 */
            crc <<= shift;
            while (len-- > 0) {
                crc = table_byte[(int) (crc ^ buf[offset++])];
            }
            crc >>>= shift;
        } else {
            int shift = width - 8;           /* 1..WORDBITS-8 */
            while (len-- > 0) {
                crc = (crc << 8) ^
                        table_byte[(int) (((crc >>> shift) ^ buf[offset++]) & 0xff)];
            }
            crc &= widmask(width);
        }
        /* post-process and return the CRC */
        if (refot) {
            crc = reflect(crc);
        }
        return crc;
    }

    private static final List<CrcModel> models = new ArrayList<>();

    public static @Nonnull Stream<CrcModel> getModels() {
        return getModels(model -> true);
    }

    public static @Nonnull Stream<CrcModel> getModels(@Nonnull final Predicate<? super CrcModel> filter) {
        Objects.requireNonNull(filter, "CRCModel::getModels - filter is null");
        synchronized (models) {
            return Stream.concat(
                    Arrays.stream(CrcModel.class.getDeclaredFields())
                            .filter(f -> f.getType().equals(CrcModel.class))
                            .map(f -> {
                                try {
                                    return f.get(null);
                                } catch (IllegalAccessException ilex) {
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .map(CrcModel.class::cast)
                            .filter(filter)
                    , models.stream()
            );
        }
    }

    public static @Nullable
    CrcModel find(@Nullable final String name) {
        return name == null ? null
                : getModels(m -> name.equals(m.getName())).findFirst().orElse(null);
    }

    public static @Nullable CrcModel construct(@Nullable final String name, @Nullable final CrcDescriprion crcDescriprion, @Nullable final Long value) {
        return crcDescriprion == null ? null
             : getModels(m -> (crcDescriprion instanceof CrcModel
                               ? CrcModel.class.cast(crcDescriprion).getCrcDescriprion()
                               : crcDescriprion
                              ).equals(m.getCrcDescriprion()))
                .findFirst()
                .orElseGet(() -> {
                    CrcModel model = new CrcModel(name, crcDescriprion, value);
                    synchronized (models) {
                        models.add(model);
                    }
                    return model;
                });
    }

    public static @Nullable
    CrcModel construct(@Nullable final CrcDescriprion crcDescriprion, @Nullable final Long value) {
        return construct(null, crcDescriprion, value);
    }

    public static @Nullable
    CrcModel construct(@Nullable final CrcDescriprion crcDescriprion) {
        return construct(crcDescriprion, null);
    }

    /*
       Deprecated: you have to use .getCRC()
       In this case you have unable to use crc = init if init & getInit is not equals.
    */

    // public long combine(long crc1, long crc2, int len2) {
    //     return crc_general_combine(
    //             crc1 == getInit() ? init : crc1, crc2, len2, getWidth(), getInit(), getPoly(), getXorot(), isRefot()
    //     );
    // }

    protected final long reflect(long value) {
        return reflect(value, getWidth());
    }

    protected static final long reflect(long value, int bits) {
        long mask = widmask(bits);
        long rmask = mask ^ 0xFFFFFFFFFFFFFFFFL;
        long prefix = value & rmask;
        long suffix = Long.reverse(value << (Long.SIZE - bits));
        return prefix | suffix;
    }

    protected static final long widmask(int width) {
        return (((1L << (width-1)) - 1L) << 1) | 1L;
    }

    private static long gf2_matrix_times(long mat[], long vec) {
        long sum = 0L;
        for (int i=0; vec != 0; vec >>>= 1, i++) {
            if ((vec & 1) != 0) {
                sum ^= mat[i];
            }
        }
        return sum;
    }

    private static void gf2_matrix_square(long square[], long mat[], int width) {
        for (int n = 0; n < width; n++) {
            square[n] = gf2_matrix_times(mat, mat[n]);
        }
    }

    protected static long crc_general_combine(long crc1, long crc2, int len2, int width, long init, long poly, long xorot, boolean reflect) {
        int n;
        long col;
        long[] even = new long[width];          /* even-power-of-two zeros operator */
        long[] odd = new long[width];           /* odd-power-of-two zeros operator */

        if (reflect) {
            init = reflect(init, width);
            poly = reflect(poly, width);
        }
        /* degenerate case (also disallow negative lengths if type changed) */
        if (len2 <= 0) {
            return crc1;
        }

        /* exclusive-or the result with len2 zeros applied to the CRC of an empty
           sequence */
        crc1 ^= init ^ xorot;

        /* construct the operator for one zero bit and put in odd[] */
        if (reflect) {
            odd[0] = poly;                /* polynomial */
            col = 1;
            for (n = 1; n < width; n++) {
                odd[n] = col;
                col <<= 1;
            }
        } else {
            col = 2;
            for (n = 0; n < width - 1; n++) {
                odd[n] = col;
                col <<= 1;
            }
            odd[n] = poly;                /* polynomial */
        }
        /* put operator for two zero bits in even[] */
        gf2_matrix_square(even, odd, width);

        /* put operator for four zero bits in odd[] */
        gf2_matrix_square(odd, even, width);

       /* apply len2 zeros to crc1 (first square will put the operator for eight
          zero bits == one zero byte, in even[]) */
        do {
            /* apply zeros operator for this bit of len2 */
            gf2_matrix_square(even, odd, width);
            if ((len2 & 1) != 0) {
                crc1 = gf2_matrix_times(even, crc1);
            }
            len2 >>>= 1;

            /* if no more bits set, then done */
            if (len2 == 0)
                break;

            /* another iteration of the loop with odd[] and even[] swapped */
            gf2_matrix_square(odd, even, width);
            if ((len2 & 1) != 0) {
                crc1 = gf2_matrix_times(odd, crc1);
            }
            len2 >>>= 1;

            /* if no more bits set, then done */
        } while (len2 != 0);

        /* return combined crc */
        crc1 ^= crc2;
        return crc1;
    }

    // CRC-3
    public static final CrcModel CRC3_GSM          = new CrcModel("CRC-3/GSM", 3, 0x3, 0x0, false, false, 0x7, 0x4L);
    public static final CrcModel CRC3_ROHC         = new CrcModel("CRC-3/ROHC", 3, 0x3, 0x7, true, true, 0x0, 0x6L);

    // CRC-4
    public static final CrcModel CRC4_INTERLAKEN   = new CrcModel("CRC-4/INTERLAKEN", 4, 0x3, 0xf, false, false, 0xF, 0xBL);
    public static final CrcModel CRC4_ITU          = new CrcModel("CRC-4/ITU", 4, 0x3, 0x0, true, true, 0x0, 0x7L);

    // CRC-5
    public static final CrcModel CRC5_EPC          = new CrcModel("CRC-5/EPC", 5, 0x9, 0x9, false, false, 0x0, 0x0L);
    public static final CrcModel CRC5_ITU          = new CrcModel("CRC-5/ITU", 5, 0x15, 0x0, true, true, 0x0, 0x7L);
    public static final CrcModel CRC5_USB          = new CrcModel("CRC-5/USB", 5, 0x5, 0x1F, true, true, 0x1F, 0x19L);

    // CRC-6
    public static final CrcModel CRC6_CDMA2000_A   = new CrcModel("CRC-6/CDMA2000-A", 6, 0x27, 0x3F, false, false, 0x0, 0xDL);
    public static final CrcModel CRC6_CDMA2000_B   = new CrcModel("CRC-6/CDMA2000-B", 6, 0x7, 0x3F, false, false, 0x0, 0x3BL);
    public static final CrcModel CRC6_DARC         = new CrcModel("CRC-6/DARC", 6, 0x19, 0x0, true, true, 0x0, 0x26L);
    public static final CrcModel CRC6_GSM          = new CrcModel("CRC-6/GSM", 6, 0x2F, 0x0, false, false, 0x3F, 0x13L);
    public static final CrcModel CRC6_ITU          = new CrcModel("CRC-6/ITU", 6, 0x3, 0x0, true, true, 0x0, 0x6L);

    // CRC-7
    public static final CrcModel CRC7              = new CrcModel("CRC-7", 7, 0x9, 0x0, false, false, 0x0, 0x75L);
    public static final CrcModel CRC7_ROHC         = new CrcModel("CRC-7/ROHC", 7, 0x4F, 0x7F, true, true, 0x0, 0x53L);
    public static final CrcModel CRC7_UMTS         = new CrcModel("CRC-7/UMTS", 7, 0x45, 0x0, false, false, 0x0, 0x61L);

    // CRC-8
    public static final CrcModel CRC8              = new CrcModel("CRC-8", 8, 0x7, 0x0, false, false, 0x0, 0xF4L);
    public static final CrcModel CRC8_AUTOSAR      = new CrcModel("CRC-8/AUTOSAR", 8, 0x2F, 0xFF, false, false, 0xFF, 0xDFL);
    public static final CrcModel CRC8_BLUETOOTH    = new CrcModel("CRC-8/BLUETOOTH", 8, 0xA7, 0x0, true, true, 0x0, 0x26L);
    public static final CrcModel CRC8_CDMA_2000    = new CrcModel("CRC-8/CDMA2000", 8, 0x9B, 0xFF, false, false, 0x0, 0xDAL);
    public static final CrcModel CRC8_DARC         = new CrcModel("CRC-8/DARC", 8, 0x39, 0x0, true, true, 0x0, 0x15L);
    public static final CrcModel CRC8_DVB_S2       = new CrcModel("CRC-8/DVB-S2", 8, 0xD5, 0x0, false, false, 0x0, 0xBCL);
    public static final CrcModel CRC8_EBU          = new CrcModel("CRC-8/EBU", 8, 0x1D, 0xFF, true, true, 0x0, 0x97L);
    public static final CrcModel CRC8_GSM_A        = new CrcModel("CRC-8/GSM-A", 8, 0x1D, 0x0, false, false, 0x0, 0x37L);
    public static final CrcModel CRC8_GSM_B        = new CrcModel("CRC-8/GSM-B", 8, 0x49, 0x0, false, false, 0xFF, 0x94L);
    public static final CrcModel CRC8_ICODE        = new CrcModel("CRC-8/I-CODE", 8, 0x1D, 0xFD, false, false, 0x0, 0x7EL);
    public static final CrcModel CRC8_ITU          = new CrcModel("CRC-8/ITU", 8, 0x7, 0x0, false, false, 0x55, 0xA1L);
    public static final CrcModel CRC8_LTE          = new CrcModel("CRC-8/LTE", 8, 0x9B, 0x0, false, false, 0x0, 0xEAL);
    public static final CrcModel CRC8_MAXIM        = new CrcModel("CRC-8/MAXIM", 8, 0x31, 0x0, true, true, 0x0, 0xA1L);
    public static final CrcModel CRC8_OPENSAFETY   = new CrcModel("CRC-8/OPENSAFETY", 8, 0x2F, 0x0, false, false, 0x0, 0x3EL);
    public static final CrcModel CRC8_ROHC         = new CrcModel("CRC-8/ROHC", 8, 0x7, 0xFF, true, true, 0x0, 0xD0L);
    public static final CrcModel CRC8_SAE_J1850    = new CrcModel("CRC-8/SAE-J1850", 8, 0x1D, 0xFF, false, false, 0xFF, 0x4BL);
    public static final CrcModel CRC8_WCDMA        = new CrcModel("CRC-8/WCDMA", 8, 0x9B, 0x0, true, true, 0x0, 0x25L);

    // CRC-10
    public static final CrcModel CRC10             = new CrcModel("CRC-10", 10, 0x233, 0x0, false, false, 0x0, 0x199L);
    public static final CrcModel CRC10_CDMA2000    = new CrcModel("CRC-10/CDMA2000", 10, 0x3D9, 0x3FF, false, false, 0x0, 0x233L);
    public static final CrcModel CRC10_GSM         = new CrcModel("CRC-10/GSM", 10, 0x175, 0x0, false, false, 0x3FF, 0x12AL);

    // CRC-11
    public static final CrcModel CRC11             = new CrcModel("CRC-11", 11, 0x385, 0x1A, false, false, 0x0, 0x5A3L);
    public static final CrcModel CRC11_UMTS        = new CrcModel("CRC-11/UMTS", 11, 0x307, 0x0, false, false, 0x0, 0x061L);

    // CRC-12
    public static final CrcModel CRC12_3GPP        = new CrcModel("CRC-12/3GPP", 12, 0x80F, 0x0, false, true, 0x0, 0xDAFL); // CRC-12/GSM
    public static final CrcModel CRC12_CDMA2000    = new CrcModel("CRC-12/CDMA2000", 12, 0xF13, 0xFFF, false, false, 0x0, 0xD4DL);
    public static final CrcModel CRC12_DECT        = new CrcModel("CRC-12/DECT", 12, 0x80F, 0x0, false, false, 0x0, 0xF5BL);
    public static final CrcModel CRC12_GSM         = new CrcModel("CRC-12/GSM", 12, 0xD31, 0x0, false, false, 0xFFF, 0xB34L);

    // CRC-13
    public static final CrcModel CRC13_BBC         = new CrcModel("CRC-13/BBC", 13, 0x1CF5, 0x0, false, false, 0x0, 0x4FAL);

    // CRC-14
    public static final CrcModel CRC14_DARC        = new CrcModel("CRC-14/DARC", 14, 0x805, 0x0, true, true, 0x0, 0x82DL);
    public static final CrcModel CRC14_GSM         = new CrcModel("CRC-14/GSM", 14, 0x202D, 0x0, false, false, 0x3fff, 0x30AEL);

    // CRC-15
    public static final CrcModel CRC15             = new CrcModel("CRC-15", 15, 0x4599, 0x0, false, false, 0x0, 0x59EL);
    public static final CrcModel CRC15_MPT1327     = new CrcModel("CRC-15/MPT1327", 15, 0x6815, 0x0, false, false, 0x1, 0x2566L);

    // CRC-16
    public static final CrcModel CRC16_ARC         = new CrcModel("CRC-16/ARC", 16, 0x8005, 0x0, true, true, 0x0, 0xBB3DL);
    public static final CrcModel CRC16_AUG_CCITT   = new CrcModel("CRC-16/AUG-CCITT", 16, 0x1021, 0x1D0F, false, false, 0x0, 0xE5CCL);
    public static final CrcModel CRC16_BUYPASS     = new CrcModel("CRC-16/BUYPASS", 16, 0x8005, 0x0, false, false, 0x0, 0xFEE8L);
    public static final CrcModel CRC16_CCITT_FALSE = new CrcModel("CRC-16/CCITT-FALSE", 16, 0x1021, 0xFFFF, false, false, 0x0, 0x29B1L);
    public static final CrcModel CRC16_CDMA2000    = new CrcModel("CRC-16/CDMA2000", 16, 0xC867, 0xFFFF, false, false, 0x0, 0x4C06L);
    public static final CrcModel CRC16_CMS         = new CrcModel("CRC-16/CMS", 16, 0x8005, 0xFFFF, false, false, 0x0, 0xAEE7L);
    public static final CrcModel CRC16_DDS_110     = new CrcModel("CRC-16/DDS-110", 16, 0x8005, 0x800D, false, false, 0x0, 0x9ECFL);
    public static final CrcModel CRC16_DECT_R      = new CrcModel("CRC-16/DECT-R", 16, 0x589, 0x0, false, false, 0x1, 0x7EL);
    public static final CrcModel CRC16_DECT_X      = new CrcModel("CRC-16/DECT-X", 16, 0x589, 0x0, false, false, 0x0, 0x7FL);
    public static final CrcModel CRC16_DNP         = new CrcModel("CRC-16/DNP", 16, 0x3D65, 0x0, true, true, 0xFFFF, 0xEA82L);
    public static final CrcModel CRC16_EN13757     = new CrcModel("CRC-16/EN-13757", 16, 0x3D65, 0x0, false, false, 0xFFFF, 0xC2B7L);
    public static final CrcModel CRC16_GENIBUS     = new CrcModel("CRC-16/GENIBUS", 16, 0x1021, 0xFFFF, false, false, 0xFFFF, 0xD64EL);
    public static final CrcModel CRC16_GSM         = new CrcModel("CRC-16/GSM", 16, 0x1021, 0x0, false, false, 0xFFFF, 0xCE3CL);
    public static final CrcModel CRC16_KERMIT      = new CrcModel("CRC-16/KERMIT", 16, 0x1021, 0x0, true, true, 0x0, 0x2189L);
    public static final CrcModel CRC16_LJ1200      = new CrcModel("CRC-16/LJ1200", 16, 0x6F63, 0x0, false, false, 0x0, 0xBDF4L);
    public static final CrcModel CRC16_MAXIM       = new CrcModel("CRC-16/MAXIM", 16, 0x8005, 0x0, true, true, 0xFFFF, 0x44C2L);
    public static final CrcModel CRC16_MCRF4XX     = new CrcModel("CRC-16/MCRF4XX", 16, 0x1021, 0xFFFF, true, true, 0x0, 0x6F91L);
    public static final CrcModel CRC16_MODBUS      = new CrcModel("CRC-16/MODBUS", 16, 0x8005, 0xFFFF, true, true, 0x0, 0x4B37L);
    public static final CrcModel CRC16_OPENSAFETY_A= new CrcModel("CRC-16/OPENSAFETY-A", 16, 0x5935, 0x0, false, false, 0x0, 0x5D38L);
    public static final CrcModel CRC16_OPENSAFETY_B= new CrcModel("CRC-16/OPENSAFETY-B", 16, 0x755B, 0x0, false, false, 0x0, 0x20FEL);
    public static final CrcModel CRC16_PROFIBUS    = new CrcModel("CRC-16/PROFIBUS", 16, 0x1DCF, 0xFFFF, false, false, 0xFFFF, 0xA819L);
    public static final CrcModel CRC16_RIELLO      = new CrcModel("CRC-16/RIELLO", 16, 0x1021, 0xB2AA, true, true, 0x0, 0x63D0L);
    public static final CrcModel CRC16_T10_DIF     = new CrcModel("CRC-16/T10-DIF", 16, 0x8BB7, 0x0, false, false, 0x0, 0xD0DBL);
    public static final CrcModel CRC16_TELEDISK    = new CrcModel("CRC-16/TELEDISK", 16, 0xA097, 0x0, false, false, 0x0, 0xFB3L);
    public static final CrcModel CRC16_TMS37157    = new CrcModel("CRC-16/TMS37157", 16, 0x1021, 0x89EC, true, true, 0x0, 0x26B1L);
    public static final CrcModel CRC16_USB         = new CrcModel("CRC-16/USB", 16, 0x8005, 0xFFFF, true, true, 0xFFFF, 0xB4C8L);
    public static final CrcModel CRC_A             = new CrcModel("CRC-A", 16, 0x1021, 0xc6c6, true, true, 0x0, 0xBF05L);
    public static final CrcModel CRC16_XMODEM      = new CrcModel("CRC-16/XMODEM", 16, 0x1021, 0x0, false, false, 0x0, 0x31C3L);
    public static final CrcModel CRC16_X_25        = new CrcModel("CRC-16/X-25", 16, 0x1021, 0xFFFF, true, true, 0xFFFF, 0x906EL);

    // CRC-17
    public static final CrcModel CRC17_CAN_FD      = new CrcModel("CRC-17/CAN-FD", 17, 0x1685B, 0x0, false, false, 0x0, 0x04F03L);

    // CRC-21
    public static final CrcModel CRC21_CAN_FD      = new CrcModel("CRC-21/CAN-FD", 21, 0x102899, 0x0, false, false, 0x0, 0x0ED841L);

    // CRC-24
    public static final CrcModel CRC24             = new CrcModel("CRC-24", 24, 0x864CFB, 0xB704CE, false, false, 0x0, 0x21CF02L);
    public static final CrcModel CRC24_BLE         = new CrcModel("CRC-24/BLE", 24, 0x65B, 0x555555, true, true, 0x0, 0xC25A56L);
    public static final CrcModel CRC24_FLEXRAY_A   = new CrcModel("CRC-24/FLEXRAY-A", 24, 0x5D6DCB, 0xFEDCBA, false, false, 0x0, 0x7979BDL);
    public static final CrcModel CRC24_FLEXRAY_B   = new CrcModel("CRC-24/FLEXRAY-B", 24, 0x5D6DCB, 0xABCDEF, false, false, 0x0, 0x1F23B8L);
    public static final CrcModel CRC24_INTERLAKEN  = new CrcModel("CRC-24/INTERLAKEN", 24, 0x328B63, 0xFFFFFF, false, false, 0xFFFFFF, 0xB4F3E6L);
    public static final CrcModel CRC24_LTE_A       = new CrcModel("CRC-24/LTE-A", 24, 0x864CFB, 0x0, false, false, 0x0, 0xCDE703L);
    public static final CrcModel CRC24_LTE_B       = new CrcModel("CRC-24/LTE-B", 24, 0x800063, 0x0, false, false, 0x0, 0x23EF52L);

    // CRC-30
    public static final CrcModel CRC30_CDMA        = new CrcModel("CRC-30/CDMA", 30, 0x2030B9C7, 0x3FFFFFFF, false, false , 0x3FFFFFFFL, 0x04C34ABFL);

    // CRC-31
    public static final CrcModel CRC31_PHILIPS     = new CrcModel("CRC-31/PHILIPS", 31, 0x4C11DB7, 0x7FFFFFFF, false, false , 0x7FFFFFFFL, 0xCE9E46CL);

    // CRC-32
    public static final CrcModel CRC32             = new CrcModel("CRC-32", 32, 0x04C11DB7L, 0xFFFFFFFFL, true, true, 0xFFFFFFFFL, 0xCBF43926L);
    public static final CrcModel CRC32_AUTOSAR     = new CrcModel("CRC-32/AUTOSAR", 32, 0xF4ACFB13L, 0xFFFFFFFFL, true, true, 0xFFFFFFFFL, 0x1697D06AL);
    public static final CrcModel CRC32_BZIP2       = new CrcModel("CRC-32/BZIP2", 32, 0x04C11DB7L, 0xFFFFFFFFL, false, false, 0xFFFFFFFFL, 0xFC891918L);
    public static final CrcModel CRC32_C           = new CrcModel("CRC-32C", 32, 0x1EDC6F41L, 0xFFFFFFFFL, true, true, 0xFFFFFFFFL, 0xE3069283L);
    public static final CrcModel CRC32_D           = new CrcModel("CRC-32D", 32, 0xA833982BL, 0xFFFFFFFFL, true, true, 0xFFFFFFFFL, 0x87315576L);
    public static final CrcModel CRC32_JAMCRC      = new CrcModel("CRC-32/JAMCRC", 32, 0x04C11DB7L, 0xFFFFFFFFL, true, true, 0x00000000L, 0x340BC6D9L);
    public static final CrcModel CRC32_MPEG_2      = new CrcModel("CRC-32/MPEG-2", 32, 0x04C11DB7L, 0xFFFFFFFFL, false, false, 0x00000000L, 0x0376E6E7L);
    public static final CrcModel CRC32_POSIX       = new CrcModel("CRC-32/POSIX", 32, 0x04C11DB7L, 0x00000000L, false, false, 0xFFFFFFFFL, 0x765E7680L);
    public static final CrcModel CRC32_Q           = new CrcModel("CRC-32Q", 32, 0x814141ABL, 0x00000000L, false, false, 0x00000000L, 0x3010BF7FL);
    public static final CrcModel CRC32_XFER        = new CrcModel("CRC-32/XFER", 32, 0x000000AFL, 0x00000000L, false, false, 0x00000000L, 0xBD0BE338L);

    // CRC-40

    public static final CrcModel CRC40_GSM         = new CrcModel("CRC-40/GSM", 40, 0x4820009, 0x0, false, false, 0xFFFFFFFFFFL,  0xD4164FC646L);

    // CRC-64

    public static final CrcModel CRC64_GO_ISO      = new CrcModel("CRC-64/GO-ISO",64, 0x000000000000001BL, 0xFFFFFFFFFFFFFFFFL, true, true, 0xFFFFFFFFFFFFFFFFL, 0xB90956C775A41001L);
    public static final CrcModel CRC64             = new CrcModel("CRC-64",64, 0x42F0E1EBA9EA3693L, 0x00000000L, false, false, 0x00000000L, 0x6C40DF5F0B497347L);
    public static final CrcModel CRC64_WE          = new CrcModel("CRC-64/WE", 64, 0x42F0E1EBA9EA3693L, 0xFFFFFFFFFFFFFFFFL, false, false, 0xFFFFFFFFFFFFFFFFL,0x62EC59E3F1A4F00AL);
    public static final CrcModel CRC64_XZ          = new CrcModel("CRC-64/XZ", 64, 0x42F0E1EBA9EA3693L, 0xFFFFFFFFFFFFFFFFL, true, true, 0xFFFFFFFFFFFFFFFFL,0x995DC9BBDF1939FAL);

}