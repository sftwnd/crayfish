package com.github.sftwnd.crayfish.utils;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by ashindarev on 04.02.16.
 */
public class ResourceBundleConfigUtilityTest {

    private static final String[] keys   = new String[] {"prefix1.key1", "prefix1.key2", "prefix2.key3", "prefix2.key4"};
    private static final String[] values = new String[] {"value of key1", "value of key2:  key1={{key1}}", "value of key 3", null};

    private ConfigUtility configUtility;

    @Before
    public void constructResourceBundle() {
        final Map<String, String> resources = new HashMap<>();
        for (int i=0; i<keys.length; i++) {
            resources.put(keys[i], values[i]);
        }
        configUtility = new ResourceBundleConfigUtility(
                                new ResourceBundle() {
                                        @Override
                                        protected Object handleGetObject(String key) {
                                            return resources.get(key) == null
                                                 ? null
                                                 : String.valueOf(resources.get(key));
                                        }

                                        @Override
                                        public Enumeration<String> getKeys() {
                                            return Collections.enumeration(resources.keySet());
                                        }
                                    }
                            );
    }

    @Test
    public void testLoadValue() throws Exception {
        for (int i=0; i<keys.length; i++) {
            String key = keys[i];
            String value = values[i];
            if (value == null) {
                assertNull("Check that value["+i+"] is null", configUtility.loadValue(key) );
            } else {
                assertEquals("Check value["+i+"] equality", value, configUtility.loadValue(key) );
            }
        }
    }

    @Test
    public void testUtf8ResourceBundleValue() throws Exception {
        String attributeName = "message.1";
        String bundleName = "Utf8ResourceBundle";
        ResourceBundle utf8ResourceBundle = Utf8ResourceBundle.getBundle(bundleName);
        ConfigUtility cfguByRb = new ResourceBundleConfigUtility(utf8ResourceBundle);
        ConfigUtility cfguByName = new ResourceBundleConfigUtility(bundleName);
        assertEquals("Attribute values from same resource file have to be equal.", cfguByName.getValue(attributeName), cfguByRb.getValue(attributeName));
    }

}