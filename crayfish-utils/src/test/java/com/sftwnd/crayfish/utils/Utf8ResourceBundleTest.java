package com.sftwnd.crayfish.utils;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Enumeration;
import java.util.ResourceBundle;

import static org.junit.Assert.*;

/**
 * Created by ashindarev on 16.01.16.
 */
public class Utf8ResourceBundleTest {

    private static ResourceBundle bundle;

    @BeforeClass
    public static void init() {
        bundle = Utf8ResourceBundle.getBundle("Utf8ResourceBundle");
    }

    @Test
    public void getBundleTest() {
        assertNotNull("Bundle has been loaded", bundle);
    }

    @Test
    public void getPropertyTest() {
        assertEquals("Check key value message.1", "Сообщение №1", bundle.getString("message.1"));
    }

    @Test
    public void getPropertyKeysTest() {
        int size = 0;
        for(Enumeration<String> keys = bundle.getKeys(); keys.hasMoreElements(); keys.nextElement()) {
            size++;
        }
        assertTrue("Check keys() method", size == 1);
    }

}
