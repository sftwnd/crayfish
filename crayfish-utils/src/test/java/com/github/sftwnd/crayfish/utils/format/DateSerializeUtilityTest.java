package com.github.sftwnd.crayfish.utils.format;

import org.junit.After;
import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by ashindarev on 08.02.16.
 */
public class DateSerializeUtilityTest {

    @After
    public void teardown() {
        DateSerializeUtility.clear();
    }

    @Test
    public void testSerializeDeserialize() throws Exception {
        DateSerializeUtility dateSerializeUtility = new DateSerializeUtility();
        Date currentDate = new Date();
        String currentDateStr = dateSerializeUtility.serialize(currentDate);
        Date restoredCurrentDate = dateSerializeUtility.deserialize(currentDateStr);
        assertEquals("Check date and date serialize/deserialize result", currentDate, restoredCurrentDate);
    }

    @Test
    public void testDefaultSerializeDeserialize() throws Exception {
        Date currentDate = new Date();
        String currentDateStr = DateSerializeUtility.defaultSerialize(currentDate);
        Date restoredCurrentDate = DateSerializeUtility.defaultDeserialize(currentDateStr);
        assertEquals("Check date and date defaultSerialize/Deserialize result", currentDate, restoredCurrentDate);
    }

    @Test
    public void testDefaultDeserializeSerialize() throws Exception {
        String currentDateStr = "2016-02-29T12:13:14.015+04:00";
        Date currentDate = DateSerializeUtility.getDateSerializeUtility((TimeZone)null,null).deserialize(currentDateStr);
        String restoredCurrentDateStr = DateSerializeUtility.getDateSerializeUtility("GMT+04:00",null).serialize(currentDate);
        assertEquals("Check date and date defaultSerialize/Deserialize result", currentDateStr, restoredCurrentDateStr);
    }

    @Test
    public void testGetDateSerializeUtilityDefaultTZ() {
        TimeZone tz1 = TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getID());
        TimeZone tz2 = TimeZone.getDefault();
        String dateFormat = DateSerializeUtility.defaultDateFormatStr;
        DateSerializeUtility dateSerializeUtility1 = DateSerializeUtility.getDateSerializeUtility(tz1, dateFormat);
        DateSerializeUtility dateSerializeUtility2 = DateSerializeUtility.getDateSerializeUtility(tz2, dateFormat);
        assertTrue("Check getDateSerializeUtility(same timeZone, same dateFormat) result.", dateSerializeUtility1 == dateSerializeUtility2);
    }

    @Test
    public void testGetDateSerializeUtility() {
        TimeZone tz1 = TimeZone.getTimeZone("Asia/Novosibirsk");
        TimeZone tz2 = TimeZone.getTimeZone("Asia/Novosibirsk");
        String dateFormat = DateSerializeUtility.defaultDateFormatStr;
        DateSerializeUtility dateSerializeUtility1 = DateSerializeUtility.getDateSerializeUtility(tz1, dateFormat);
        DateSerializeUtility dateSerializeUtility2 = DateSerializeUtility.getDateSerializeUtility(tz2, dateFormat);
        assertTrue("Check getDateSerializeUtility(same timeZone, same dateFormat) result.", dateSerializeUtility1 == dateSerializeUtility2);
    }

    @Test
    public void testDateSerializeUtilityConstructorStringNoparam() {
        DateSerializeUtility defaultDateSerializeUtility = new DateSerializeUtility();
        DateSerializeUtility dateSerializeUtility = new DateSerializeUtility(Calendar.getInstance().getTimeZone().getID());
        Date checkDate = new Date();
        assertEquals( "Check DateSerializeUtility(timeZoneId) constructor"
                      ,defaultDateSerializeUtility.serialize(checkDate)
                      ,dateSerializeUtility.serialize(checkDate)
                    );
    }

    @Test(expected=ParseException.class)
    public void testDeserializeWrongDateText() throws ParseException {
        DateSerializeUtility.defaultDeserialize("adjhfalskdfjhalskjdf");
    }

}