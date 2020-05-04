package com.github.sftwnd.crayfish.common.crc;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static com.github.sftwnd.crayfish.common.crc.CrcModel.getModels;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CRCTest {

    private static final byte[] buff = "123456789".getBytes();

    @Test
    void testUpdate() {
        getModels(m -> m.getCheck() != null).forEach( m -> {
            CRC crc = m.getCRC();
            long crcValue = crc.update(buff).getCrc();
            assertEquals(m.getCheck().longValue(), crc.getCrc(), "CRC value for byte sequence of 123456789 has to be equals check value");
            assertEquals(m.getCheck().longValue(), crcValue, "Crc update result for byte sequence of 123456789 has to be equals check value");
            assertEquals(m.getCheck().longValue(), crc.update(buff, 1, -1).getCrc(), "Update with negative length has not got to change result");
            assertEquals(m.getCheck().longValue(), crc.update(null, 1, 1).getCrc(), "Update with null reference to buff has not got to change result");
        });
    }

    @Test
    void combineUpdate() {
        getModels(m -> m.getCheck() != null).forEach( m -> {
            CRC crc = m.getCRC();
            crc.update(buff, 0, 4).combine(m.getCRC().update(buff, 4, 5));
            assertEquals(m.getCheck().longValue(), crc.getCrc(), "CRC combine value of crcs oomr byte sequence of 1234 & 56789 has to be equals check value");
        });
    }

    @Test
    void testClone() {
        getModels().forEach(
                m -> {
                    CRC crc = m.getCRC(buff);
                    CRC crc1 = CRC.class.cast(crc.clone());
                    assertEquals(crc, crc1, "CRC and CRC.clone() have to be equals");
                }
        );
    }

    @Test
    void testHashCode() {
        getModels().forEach(
                m -> {
                    CRC crc = m.getCRC(buff);
                    CRC crc1 = CRC.class.cast(crc.clone());
                    assertEquals(crc.hashCode(), crc1.hashCode(), "CRC.hashCode() on cloned CRCs have to be equals");
                }
        );
    }

    @Test
    void testToString() {
        Set<String> strings = new HashSet<>();
        assertEquals(
            getModels().peek( m -> strings.add(m.getCRC(buff).toString())).count()
           ,strings.stream().distinct().count()
           ,"All toString value for different CRCs has to be different");
    }

}