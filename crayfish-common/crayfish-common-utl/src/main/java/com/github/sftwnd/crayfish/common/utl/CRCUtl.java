package com.github.sftwnd.crayfish.common.utl;

import java.util.zip.CRC32;

public class CRCUtl {

    public static String getCRC(long crc) {
        return Long.toHexString(crc).toUpperCase();
    }

    public static CRC32 buildCrc32(String str) {
        return str == null ? buildCrc32((byte[])null) : buildCrc32(str.getBytes());
    }

    public static CRC32 buildCrc32(byte[] buff) {
        CRC32 crc32 = new CRC32();
        if (buff == null || buff.length == 0) {
            crc32.update(0);
        } else {
            crc32.update(buff, 0, buff.length);
        }
        return crc32;
    }

    public static long getCrc32Value(String str) {
        return buildCrc32(str).getValue();
    }

    public static long getCrc32Value(byte[] buff) {
        return buildCrc32(buff).getValue();
    }

    public static String getCrc32(String str) {
        return getCRC(getCrc32Value(str));
    }

    public static String getCrc32(byte[] buff) {
        return getCRC(getCrc32Value(buff));
    }

}
