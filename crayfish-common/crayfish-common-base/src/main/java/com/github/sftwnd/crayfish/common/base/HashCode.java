package com.github.sftwnd.crayfish.common.base;

import com.github.sftwnd.crayfish.common.crc.CRC;
import com.github.sftwnd.crayfish.common.crc.CrcModel;

import java.nio.ByteBuffer;

/**
 * Created by ashindarev on 28.01.16.
 */
public class HashCode {

    private static final byte[] splitter = new byte[] {(byte)':'};
    private static final int BYTE_BUFFER_SIZE = Math.max(Integer.BYTES, Math.max(Double.BYTES, Float.BYTES));

    private CRC crc;
    private ByteBuffer buffer = ByteBuffer.allocate(BYTE_BUFFER_SIZE);

    @SuppressWarnings({"unchecked"})
    public HashCode(Class<?> clazz) {
        this(clazz != null ? clazz.getSimpleName() : null);
    }

    public HashCode(String name) {
        crc = CrcModel.CRC32.getCRC();
        if (name != null) {
            crc.update(name.getBytes());
        }
    }

    private void split() {
        crc.update(splitter);
    }

    public HashCode update(Byte val) {
        split();
        if (val != null) {
            buffer.clear();
            buffer.put(val);
            crc.update(buffer.array(), 0, Byte.BYTES);
        }
        return this;
    }


    public HashCode update(Integer val) {
        split();
        if (val != null) {
            buffer.clear();
            buffer.putInt(val);
            crc.update(buffer.array(), 0, Integer.BYTES);
        }
        return this;
    }


    public HashCode update(Long val) {
        split();
        if (val != null) {
            buffer.clear();
            buffer.putLong(val);
            crc.update(buffer.array(), 0, Long.BYTES);
        }
        return this;
    }

    public HashCode update(Float val) {
        split();
        if (val != null) {
            buffer.clear();
            buffer.putFloat(val);
            crc.update(buffer.array(), 0, Float.BYTES);
        }
        return this;
    }

    public HashCode update(Double val) {
        split();
        if (val != null) {
            buffer.clear();
            buffer.putDouble(val);
            crc.update(buffer.array(), 0, Double.BYTES);
        }
        return this;
    }

    public HashCode update(String val) {
        split();
        if (val != null) {
            crc.update(val.getBytes());
        }
        return this;
    }

    public HashCode update(Object obj) {
        if (obj == null) {
            split();
        } else if (obj instanceof Integer) {
            update((Integer)obj);
        } else if (obj instanceof Long) {
            update((Long)obj);
        } else if (obj instanceof Double) {
            update((Double)obj);
        } else if (obj instanceof Byte) {
            update((Byte)obj);
        } else if (obj instanceof Float) {
            update((Float)obj);
        } else {
            update(obj.toString());
        }
        return this;
    }

    public HashCode update(Object[] objects) {
        for (Object obj:objects) {
            update(obj);
        }
        return this;
    }

    @Override
    public int hashCode() {
        return (int) (crc.getCrc() & 0xFFFFFFFF);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        } else if (object == this) {
            return true;
        } else {
            return this.getClass().equals(object.getClass())
                && hashCode() == object.hashCode();
        }
    }

    public int hashCode(Object[] attributes) {
        for (Object obj:attributes) {
            update(obj);
        }
        return hashCode();
    }

    public static int hashCode(String name, Object[] attributes) {
        return new HashCode(name).hashCode(attributes);
    }

    @SuppressWarnings({"unchecked"})
    public static int hashCode(Class<?> clazz, Object[] attributes) {
        return hashCode(clazz != null ? clazz.getSimpleName() : null, attributes);
    }

}
