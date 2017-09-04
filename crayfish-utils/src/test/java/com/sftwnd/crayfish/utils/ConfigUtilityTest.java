package com.sftwnd.crayfish.utils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


/**
 * Created by ashindarev on 04.02.16.
 */
public class ConfigUtilityTest {

    private static final String[] keys   = new String[] {"ConfigUtilityTest.key1", "ConfigUtilityTest.key2", "ConfigUtilityTest.pfx1.key3", "ConfigUtilityTest.pfx1.key4", "ConfigUtilityTest.pfx2.key5", "ConfigUtilityTest.pfx2.key6", "ConfigUtilityTest.pfx3.elements", "ConfigUtilityTest.pfx3.currentTimestamp"};
    private static final String[] values = new String[] {"value of key1", "{{key1}}", "value of key 3", "{{key3}}", "value of key 5", "{{pfx2.key5}}", "1;2;3;4;5", "{{currentTimestamp}} --> {{currentTimestamp+86400}}"};

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
    public void getValueClassNameNoIdTest() {
        assertEquals("Check ConfigUtility.getValue(Class<?> clazz, String name)", values[0], configUtility.getValue(ConfigUtilityTest.class, "key2"));
    }

    @Test
    public void getValueClassStepNameNoIdTest() {
        assertEquals("Check ConfigUtility.getValue(Class<?> clazz, String step, String name, int id)", values[2], configUtility.getValue(ConfigUtilityTest.class, "pfx1", "key4", -1 ));
    }

    @Test
    public void getValueStringNameNoIdTest() {
        assertEquals("Check ConfigUtility.getValue(Class<?> clazz, String step, String name, int id)", values[4], configUtility.getValue(ConfigUtilityTest.class, "pfx2.key6", -1 ));
    }

    @Test
    public void getKeyClassStepNameTest() {
        assertEquals("Check ConfigUtility.getKey(Class<?> clazz, String step, String name)", keys[4], configUtility.getKey(ConfigUtilityTest.class, "pfx2", "key5"));
    }

    @Test
    public void getKeyPrefixStepNameTest() {
        assertEquals("Check ConfigUtility.getKey(Class<?> clazz, String step, String name)", keys[4], configUtility.getKey("ConfigUtilityTest", "pfx2", "key5"));
    }

    @Test
    public void constructListTest() {
        List<String> list = new ArrayList<>();
        for (String val:new String[] {"1", "2", "3", "4", "5"}) {
            list.add(val);
        }
        assertEquals("Check ConfigUtility.constructList(...)", list, ConfigUtility.constructList(values[6]));
    }

    @Test
    public void constructListWithNullTest() {
        List<String> list = new ArrayList<>();
        for (String val:new String[] {null, "1", "2", null}) {
            list.add(val);
        }
        assertEquals("Check ConfigUtility.constructList(...)", list, ConfigUtility.constructList(";1;2;"));
    }

    @Test
    public void getLongValuesClassNoStepTest() {
        List<Long> values = Arrays.asList(new Long[] {1l, 2l, 3l, 4l, 5l});
        assertEquals("Check getLongValues(Class<?> clazz, String name)", values, Arrays.asList(configUtility.getLongValues(ConfigUtilityTest.class, "pfx3.elements")));
    }

    @Test
    public void getLongValuesPrefixNoStepTest() {
        List<Long> values = Arrays.asList(new Long[] {1l, 2l, 3l, 4l, 5l});
        assertEquals("Check getLongValues(String prefix, String name)", values, Arrays.asList(configUtility.getLongValues("ConfigUtilityTest", "pfx3.elements")));
    }

    @Test
    public void getLongValuesClassNameTest() {
        List<Long> values = Arrays.asList(new Long[] {1l, 2l, 3l, 4l, 5l});
        assertEquals("Check getLongValues(Class<?> clazz, String step, String name)", values, Arrays.asList(configUtility.getLongValues(ConfigUtilityTest.class, "pfx3.elements")));
    }

    @Test
    public void getLongValuesClassStepNameTest() {
        List<Long> values = Arrays.asList(new Long[] {1l, 2l, 3l, 4l, 5l});
        assertEquals("Check getLongValues(Class<?> clazz, String step, String name)", values, Arrays.asList(configUtility.getLongValues(ConfigUtilityTest.class, "pfx3" ,"elements")));
    }

    @Test
    public void getValuesClassStepNameTest() {
        List<String> values = Arrays.asList(new String[] {"1", "2", "3", "4", "5"});
        assertEquals("Check getValues(Class<?> clazz, String step, String name)", values, Arrays.asList(configUtility.getValues(ConfigUtilityTest.class, "pfx3", "elements")));
    }

    @Test
    public void getValuesClassNameTest() {
        List<String> values = Arrays.asList(new String[] {"1", "2", "3", "4", "5"});
        assertEquals("Check getValues(Class<?> clazz, String step, String name)", values, Arrays.asList(configUtility.getValues(ConfigUtilityTest.class, "pfx3.elements")));
    }

    @Test
    public void getValuesPrefixStepNameTest() {
        List<String> values = Arrays.asList(new String[] {"1", "2", "3", "4", "5"});
        assertEquals("Check getValues(Class<?> clazz, String step, String name)", values, Arrays.asList(configUtility.getValues("ConfigUtilityTest", "pfx3", "elements")));
    }

    @Test
    public void getValuesPrefixNameTest() {
        List<String> values = Arrays.asList(new String[] {"1", "2", "3", "4", "5"});
        assertEquals("Check getValues(Class<?> clazz, String step, String name)", values, Arrays.asList(configUtility.getValues("ConfigUtilityTest.pfx3", "elements")));
    }

    @Test
    public void getCalculatedCurrentTimestampValueTest() {
        String text = configUtility.loadValue(ConfigUtilityTest.class, "pfx3.currentTimestamp");
        String value = configUtility.getValue(ConfigUtilityTest.class, "pfx3", "currentTimestamp", -1);
        assertNotEquals("Check getValue(Class<?> clazz, String prefix, String name)", text, value);
    }

    @Test
    public void getValueNoPrefixTest() {
        String text = configUtility.loadValue(ConfigUtilityTest.class, "pfx3.currentTimestamp");
        String value = configUtility.getValue(ConfigUtilityTest.class, "pfx3.currentTimestamp");
        assertNotEquals("Check getValue(Class<?> clazz, String name)", text, value);
    }

    @Test
    public void getValueNoClassTest() {
        String text = configUtility.loadValue(ConfigUtilityTest.class, "pfx3.currentTimestamp");
        String value = configUtility.getValue("ConfigUtilityTest.pfx3", "currentTimestamp");
        assertNotEquals("Check getValue(Class<?> clazz, String name)", text, value);
    }

}