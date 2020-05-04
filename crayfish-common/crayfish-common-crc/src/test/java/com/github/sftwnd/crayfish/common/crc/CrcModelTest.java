package com.github.sftwnd.crayfish.common.crc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.github.sftwnd.crayfish.common.crc.CrcModel.*;
import static  com.github.sftwnd.crayfish.common.crc.CrcModel.getModels;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrcModelTest {

    private static final byte[] buff = "123456789".getBytes();

    @Test
    void testPoly() {
        getModels().forEach(
                m -> assertEquals(m.isRefin() ? reflect(m.getPoly(), m.getWidth()) : m.getPoly(), m.poly, "Wrong poly field initialization in constructor")
        );
    }

    @Test
    void testInit() {
        getModels().forEach(
                m -> assertEquals((m.isRefot() ? CrcModel.reflect(m.getInit(), m.getWidth()) : m.getInit()) ^ m.getXorot(), m.init, "Wrong init field initialization in constructor")
        );
    }

    @Test
    void testRefot() {
        getModels().forEach(
                m -> assertEquals(m.isRefot() ^ m.isRefin(), m.refot, "Wrong refot field initialization in constructor")
        );
    }

    @Test
    void testGetModels() {
        assertTrue(getModels().count() >= 101, "Wrong getModels size");
    }

    @Test
    void testGetModelsByName() {
        getModels().forEach(
                m -> {
                    CrcModel model = getModels(mdl -> mdl.getName().equals(m.getName())).findFirst().orElse(null);
                    assertSame(m, model, "getModels(byName) result is wrong");
                }
        );
    }

    @Test
    void testCrcBytewise() {
        getModels().filter(m -> m.getCheck() != null).forEach(m -> {
            m._init();
            long crc = m.crcBytewise(m.init, buff, 0, 9);
            assertEquals(m.getCheck().longValue(), crc, "Update crc by '123456789' buffer has to be equals check value");
            crc = m.crcBytewise(m.init, null, 0, 9);
            assertEquals(m.init, crc, "Update crc by null buffer has got to return init value");
            crc = m.crcBytewise(m.init, null, 0, -1);
            assertEquals(m.init, crc, "Update crc by buffer with -1 length has got to return init value");
        });
    }

    @Test
    void testGetModelsByDescription() {
        getModels().forEach(
                m -> {
                    CrcModel model = getModels(mdl -> mdl.getCrcDescriprion().equals(m.getCrcDescriprion())).findFirst().orElse(null);
                    assertSame(m, model, "getModels(byName) result is wrong");
                }
        );
    }

    @Test
    void testCRCLengthArg() {
        Assertions.assertThrows(
                IllegalArgumentException.class
               ,() -> CRC64_XZ.getCRC(0, -1)
               ,"getCrc(length < 0) hhad to throw IllegalArgumentException"
        );
    }

    @Test
    void testGetCRC() {
        getModels().forEach(
                m -> {
                    CRC crc = m.getCRC();
                    assertSame(m, Optional.ofNullable(crc).map(CRC::getModel).orElse(null), "Wrong model in the CRC, created by getCRC");
                }
        );
    }

    @Test
    void testGetCRCCrcVal() {
        getModels().filter(m -> m.getCheck()!= null).forEach(
                m -> {
                    long crcVal = 0x17;
                    int  lenVal = 47;
                    CRC crc = m.getCRC(crcVal, lenVal);
                    assertSame(m, Optional.ofNullable(crc).map(CRC::getModel).orElse(null), "Wrong model in the CRC, created by getCRC(crc, len)");
                    assertEquals(crcVal, Optional.ofNullable(crc).map(CRC::getCrc).orElse(null), "Wrong crc in the CRC, created by getCRC(crc, len)");
                    assertEquals(lenVal, Optional.ofNullable(crc).map(CRC::getLength).orElse(null), "Wrong length in the CRC, created by getCRC(crc, len)");
                }
        );
    }

    @Test
    void testGetCRCBuff() {
        getModels().filter(m -> m.getCheck()!= null).forEach(
                m -> {
                    CRC crc = m.getCRC(buff);
                    assertSame(m, Optional.ofNullable(crc).map(CRC::getModel).orElse(null), "Wrong model in the CRC, created by getCRC(crc, len)");
                    assertEquals(m.getCheck(), Optional.ofNullable(crc).map(CRC::getCrc).orElse(null), "Wrong crc in the CRC, created by getCRC('123456789')");
                }
        );
    }

    @Test
    void testGetCRCBuffLen() {
        getModels().forEach(
                m -> {
                    CRC crc = m.getCRC(buff, buff.length);
                    assertSame(m, Optional.ofNullable(crc).map(CRC::getModel).orElse(null), "Wrong model in the CRC, created by getCRC(crc, len)");
                    assertEquals(m.getCheck(), Optional.ofNullable(crc).map(CRC::getCrc).orElse(null), "Wrong crc in the CRC, created by getCRC('123456789', 9)");
                }
        );
    }

    @Test
    void testGetCRCBuffOffsetLen() {
        getModels().filter(m -> m.getCheck() != null).forEach(
                m -> {
                    CRC crc = m.getCRC(buff, 0, buff.length);
                    assertSame(m, Optional.ofNullable(crc).map(CRC::getModel).orElse(null), "Wrong model in the CRC, created by getCRC(crc, len)");
                    assertEquals(m.getCheck(), Optional.ofNullable(crc).map(CRC::getCrc).orElse(null), "Wrong crc in the CRC, created by getCRC('123456789', 0, 9)");
                }
        );
    }

    @Test
    void testFind() {
        assertNull(find("$$$"), "Result for unknown model has to be null");
        assertSame(CRC64_XZ, find(CRC64_XZ.getName()), "Result for find(known model) has to return known model");
    }

    @Test
    void testConstruct() {
        assertSame(CRC64_XZ, construct("$$$", CRC64_XZ, 0L), "Result for construct(name, known model, length) has to return known model");
        assertSame(CRC64_XZ, construct(CRC64_XZ, 0L), "Result for construct(known model, length) has to return known model");
        assertSame(CRC64_XZ, construct(CRC64_XZ), "Result for construct(known model, length) has to return known model");
        CrcDescriprion descriprion = new CrcDescriprion(27, 0x800069, 0x0, true, true, 0x0);
        CrcModel model = CrcModel.construct(null, descriprion, null);
        assertEquals(descriprion, construct("###", descriprion, null).getCrcDescriprion(), "Result for construct(name, model, length) has to return model");
    }

    @Test
    void testCombine() {
        getModels().filter(m -> m.getCheck() != null).forEach( m -> {
            for (int i=0; i<buff.length; i++) {
                long crc = crc_general_combine(
                               m.getCRC(buff, 0, i).crc,
                               m.getCRC(buff, i, buff.length -i).crc,
                          buff.length -i,
                               m.getWidth(), m.getInit(), m.getPoly(), m.getXorot(), m.isRefot());
                assertEquals(m.getCheck().longValue(), crc, "Combine crc of byte sequence '123456789' splitted in position "+i+" has to be equals check value");
            }
            assertEquals(
                    m.getCheck().longValue(),
                    crc_general_combine(
                            m.getCRC(buff).crc, -1, -1,
                            m.getWidth(), m.getInit(), m.getPoly(), m.getXorot(), m.isRefot()),
                         "Combine crc to crc with negative length has to return nonchanged crc value");
        });
    }


}