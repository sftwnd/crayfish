package com.github.sftwnd.crayfish.utils;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ResourceBundle;

import static org.junit.Assert.*;

/**
 * Created by ashindarev on 16.01.16.
 */
public final class HashCodeTest {

    private static ResourceBundle bundle;

    private static final Object v0 = null;
    private static final byte   v1 = 1;
    private static final int    v2 = 2;
    private static final long   v3 = 3;
    private static final float  v4 = 4.5f;
    private static final double v5 = 5.6d;
    private static final String v6 = "v6";
    private static final Long   v7 = null;
    private              Object[] objects = constructObjects();
    private static       int      baseHashCodeValue;
    private static HashCode baseHashCode;

    private static final Object[] constructObjects() {
        return new Object[] {v0, v1, v2, v3, v4, v5, v6, v7};
    }

    @BeforeClass
    public static void initClass() {
        baseHashCode = new HashCode(HashCodeTest.class);
        baseHashCode.update(constructObjects());
        baseHashCodeValue = baseHashCode.hashCode();
    }

    @Before
    public void reloadObjects() {
        objects = constructObjects();
    }


    @Test
    public void checkHashCodeOfArray() {
        HashCode hashCode = new HashCode(HashCodeTest.class);
        assertEquals("Check hashCode.hashCode(objects[])", baseHashCodeValue, hashCode.hashCode(objects));
    }

    @Test
    public void checkUpdateObjects() {
        HashCode hashCode = new HashCode(HashCodeTest.class);
        hashCode.update(objects);
        assertEquals("Check hashCode.update(objects[])", baseHashCodeValue, hashCode.hashCode());
    }

    @Test
    public void checkStaticClassHashCodeOfArray() {
        assertEquals("Check hashCode.hashCode(objects[])", baseHashCodeValue, HashCode.hashCode(HashCodeTest.class, objects));
    }

    @Test
    public void checkStaticStringHashCodeOfArray() {
        assertEquals("Check hashCode.hashCode(objects[])", baseHashCodeValue, HashCode.hashCode(HashCodeTest.class.getSimpleName(), objects));
    }

    @Test
    public void checkSimpleUpdate() {
        HashCode hashCode = new HashCode(HashCodeTest.class);
        hashCode.update(v0);
        hashCode.update(v1);
        hashCode.update(v2);
        hashCode.update(v3);
        hashCode.update(v4);
        hashCode.update(v5);
        hashCode.update(v6);
        hashCode.update(v7);
        assertEquals("Check hashCode.update(object...)", baseHashCodeValue, hashCode.hashCode());
    }

    @Test
    public void equalsTest() {
        HashCode hashCode = new HashCode(HashCodeTest.class);
        hashCode.update(constructObjects());
        assertEquals("Check hashCode.equals(other HashCode)", baseHashCode, hashCode);
        assertFalse("Check hashCode.equals(null)", hashCode.equals(null));
        assertTrue("Check hashCode.equals(this)", hashCode.equals(hashCode));
    }

}
