package com.sftwnd.crayfish.utils;

import com.sftwnd.crayfish.utils.format.DateSerializeUtility;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by ashindarev on 12.02.16.
 */
public class BaseConfigUtilityTest {

    private static BaseConfigUtility baseConfigUtility;

    private static List<String> values = new ArrayList<>();
    private static List<String> values1 = new ArrayList<>();
    private static List<Long>   lvalues = new ArrayList<>();
    private static List<Long>   lvalues1 = new ArrayList<>();

    @BeforeClass
    public static void setUp() throws Exception {
        baseConfigUtility = new ResourceBundleConfigUtility(
                                    Utf8ResourceBundle.getBundle (
                                        "BaseConfigUtility"
                                    )
                                ).getBaseConfigUtility("baseConfigUtility");
        Collections.addAll(values, Arrays.stream(new String[] { "1", "2", "3", "4", "5" }).toArray(String[]::new));
        Collections.addAll(values1, Arrays.stream(new String[] { "10", "20", "30", "40", "50" }).toArray(String[]::new));
        Collections.addAll(lvalues, Arrays.stream(new Long[] { 1L, 2L, 3L, 4L, 5L }).toArray(Long[]::new));
        Collections.addAll(lvalues1, Arrays.stream(new Long[] { 10L, 20L, 30L, 40L, 50L }).toArray(Long[]::new));
    }

    @Test
    public void testGetValueStepName() throws Exception {
        assertEquals("test.test: String getValue(step, val1);", "value1",baseConfigUtility.getValue("step","val1"));
    }

    @Test
    public void testGetValueName() throws Exception {
        assertEquals("test.test: String getValue(val2);", "value2",baseConfigUtility.getValue("val2"));
    }

    @Test
    public void testGetValueStepNameId() throws Exception {
        assertEquals("test.test: String getValue(step, val3, 2);", "[30]",baseConfigUtility.getValue("step","val3", 2));
    }

    @Test
    public void testGetValueNameId() throws Exception {
        assertEquals("test.test: String getValue(val4, 3);", "<4>",baseConfigUtility.getValue("val4", 3));
        assertEquals("test.test: String getValue(step.val3, 2);", "[3]",baseConfigUtility.getValue("step.val3", 2));
    }

    @Test
    public void testGetValueNoStepNameId() throws Exception {
        assertEquals("test.test: String getValue(val7, 1) with {{step.id}};", "((20))",baseConfigUtility.getValue("val7", 1));
    }

    @Test
    public void testGetValuesStepName() throws Exception {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, Arrays.stream(baseConfigUtility.getValues("step","ids")).toArray(String[]::new));
        assertEquals("test.test: getValues(step, ids)", values1, list);
    }

    @Test
    public void testGetValuesName() throws Exception {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, Arrays.stream(baseConfigUtility.getValues("ids")).toArray(String[]::new));
        assertEquals("test.test: getValues(ids)", values,list);
        list.clear();
        Collections.addAll(list, Arrays.stream(baseConfigUtility.getValues("step.ids")).toArray(String[]::new));
        assertEquals("test.test: getValues(step.ids)", values1, list);
    }

    @Test
    public void testGetValuesNoStepName() throws Exception {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, Arrays.stream(baseConfigUtility.getValues("step.ids")).toArray(String[]::new));
        assertEquals("test.test: getValues(step.ids)", values1, list);
    }

    @Test
    public void testGetLongValuesStepName() throws Exception {
        List<Long> list = new ArrayList<>();
        Collections.addAll(list, Arrays.stream(baseConfigUtility.getLongValues("step","ids")).toArray(Long[]::new));
        assertEquals("test.test: getLongValues(step, ids)", lvalues1, list);
    }

    @Test
    public void testGetLongValuesName() throws Exception {
        List<Long> list = new ArrayList<>();
        Collections.addAll(list, Arrays.stream(baseConfigUtility.getLongValues("ids")).toArray(Long[]::new));
        assertEquals("test.test: getLongValues(ids)", lvalues, list);
        list.clear();
        Collections.addAll(list, Arrays.stream(baseConfigUtility.getLongValues("step.ids")).toArray(Long[]::new));
        assertEquals("test.test: getLongValues(step.ids)", lvalues1, list);
    }

    @Test
    public void testGetPrefixNoprefixString() throws Exception {
        String value1 = baseConfigUtility.getValue("step.val5");
        String value2 = baseConfigUtility.getValue("step", "val5");
        Date date1 = DateSerializeUtility.defaultDeserialize(value1);
        Date date2 = DateSerializeUtility.defaultDeserialize(value2);
        assertNotNull("test.test: getValue(step.value5) != null", value1);
        assertTrue("test.test: getValue(step.value5) ~ getValue(step, value5)", Math.abs(date1.getTime()-date2.getTime()) < 100);
    }

    @Test
    public void testGetTimestampValue() throws Exception {
        String value = baseConfigUtility.getValue("step","val5");
        String value1 = baseConfigUtility.getValue("step","val5",-1);
        Date date = DateSerializeUtility.defaultDeserialize(value);
        assertNotNull("test.test: Date({{currentTimestamp}}) != null", date);
        assertTrue("Test: {{currentTimestamp}}", Math.abs(date.getTime() - System.currentTimeMillis()) < 750);
    }

}