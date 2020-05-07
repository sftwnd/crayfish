package com.github.sftwnd.crayfish.common.format;

import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.sftwnd.crayfish.common.format.DateSerializeUtility.DEFAULT_DATE_FORMAT_STR;
import static com.github.sftwnd.crayfish.common.format.DateSerializeUtility.DEFAULT_TIME_ZONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateSerializeUtilityTest {

    void testDateSerializeUtility(String txt, DateSerializeUtility dsu, String format, TimeZone tz) {
        DateFormat df = new SimpleDateFormat(format);
        df.setTimeZone(tz);
        testDateSerializeUtility(txt, dsu, df);
    }

    void testDateSerializeUtility(String txt, DateSerializeUtility dsu, DateFormat df) {
        Instant instant = Instant.now();
        assertEquals(df.format(Date.from(instant)), dsu.serialize(instant), txt+" has to use format '"+DEFAULT_DATE_FORMAT_STR+"' in StimeZone: '"+df.getTimeZone().getDisplayName()+"'");
    }

    @Test
    void testNewDateSerializeUtility() {
        testDateSerializeUtility("DateSerializeUtility()",new DateSerializeUtility(), DEFAULT_DATE_FORMAT_STR, DEFAULT_TIME_ZONE);
    }

    @Test
    void testNewDateSerializeUtilityTZ() {
        TimeZone tz = TimeZone.getTimeZone("Australia/ACT");
        testDateSerializeUtility("DateSerializeUtility(TimeZone)",new DateSerializeUtility(tz), DEFAULT_DATE_FORMAT_STR, tz);
    }

    @Test
    void testNewDateSerializeUtilityZoneID() {
        TimeZone tz = TimeZone.getTimeZone("Australia/ACT");
        testDateSerializeUtility("DateSerializeUtility(Zone.ID)",new DateSerializeUtility(tz.getID()), DEFAULT_DATE_FORMAT_STR, tz);
        tz = DEFAULT_TIME_ZONE;
        testDateSerializeUtility("DateSerializeUtility(Zone.ID=null)",new DateSerializeUtility((String)null), DEFAULT_DATE_FORMAT_STR, tz);
    }

    @Test
    void testNewDateSerializeUtilityTZFmt() {
        TimeZone tz = TimeZone.getTimeZone("Australia/NFT");
        String dateFormat = "dd.MM.yyyy HH:mm:ss.SSSXXX";
        testDateSerializeUtility("DateSerializeUtility(TimeZone, Format)",new DateSerializeUtility(tz, dateFormat), dateFormat, tz);
    }

    @Test
    void testSerializeNullability() {
        assertNull(new DateSerializeUtility().serialize((Instant)null), "serialize(Instant=null) has to be null");
        assertNull(new DateSerializeUtility().serialize((Date)null), "serialize(Date=null) has to be null");
        assertNotNull(new DateSerializeUtility().serialize(Instant.now()), "serialize(Instant.now) has to be not null");
        assertNotNull(new DateSerializeUtility().serialize(new Date()), "serialize(Date=null) has to be not null");
    }

    @Test
    void testDeserializeNullability() throws ParseException {
        assertNull(new DateSerializeUtility().deserialize(null), "deserialize(null) has to be null");
        assertNull(new DateSerializeUtility().deserialize(""), "deserialize(\"\":emptyString) has to be null");
        assertThrows(ParseException.class, () -> new DateSerializeUtility().deserialize("$#@%^#"),
                "Deserialization of wrong string has to throws ParseException");
    }

    @Test
    void testDeSerialize() throws ParseException {
        Date date = new Date();
        assertEquals(date, DateSerializeUtility.defaultDeserialize(DateSerializeUtility.defaultSerialize(date)),"Serialize/Deserialize of date has to return the same result");
    }

    @Test
    void testGetDateSerializeUtility() throws InterruptedException {
        DateSerializeUtility dsu = DateSerializeUtility.getDateSerializeUtility(DEFAULT_TIME_ZONE, DEFAULT_DATE_FORMAT_STR);
        AtomicReference<DateSerializeUtility> dsur = new AtomicReference<>(DateSerializeUtility.getDateSerializeUtility(DEFAULT_TIME_ZONE, DEFAULT_DATE_FORMAT_STR));
        assertSame(DateSerializeUtility.getDateSerializeUtility(DEFAULT_TIME_ZONE, DEFAULT_DATE_FORMAT_STR),
                   DateSerializeUtility.getDateSerializeUtility(DEFAULT_TIME_ZONE, DEFAULT_DATE_FORMAT_STR),
                   "getDateSerializeUtility(TimeZone, String) from equls parameters has to return same value");
        assertSame(DateSerializeUtility.getDateSerializeUtility(DEFAULT_TIME_ZONE, DEFAULT_DATE_FORMAT_STR),
                DateSerializeUtility.getDateSerializeUtility(DEFAULT_TIME_ZONE.toZoneId(), DEFAULT_DATE_FORMAT_STR),
                "getDateSerializeUtility(ZoneID, String) from equls parameters has to return same value");
        assertSame(DateSerializeUtility.getDateSerializeUtility(DEFAULT_TIME_ZONE.getID(), DEFAULT_DATE_FORMAT_STR),
                DateSerializeUtility.getDateSerializeUtility(DEFAULT_TIME_ZONE.getID(), DEFAULT_DATE_FORMAT_STR),
                "getDateSerializeUtility(String, String) from the same thread and on equls parameters has to return same value");
        assertNotNull(DateSerializeUtility.getDateSerializeUtility(DEFAULT_TIME_ZONE, DEFAULT_DATE_FORMAT_STR),
                "getDateSerializeUtility(TimeZone, String) from non null valid parameters has to return non null value");
        assertNotNull(DateSerializeUtility.getDateSerializeUtility(DEFAULT_TIME_ZONE.toZoneId(), DEFAULT_DATE_FORMAT_STR),
                "getDateSerializeUtility(ZoneID, String) from non null valid parameters has to return non null value");
        assertNotNull(DateSerializeUtility.getDateSerializeUtility(DEFAULT_TIME_ZONE.getID(), DEFAULT_DATE_FORMAT_STR),
                "getDateSerializeUtility(String, String) from non null valid parameters has to return non null value");
        Instant now = Instant.now();
        String str = dsu.serialize(now);
        assertEquals(str,
                DateSerializeUtility.getDateSerializeUtility((TimeZone)null, DEFAULT_DATE_FORMAT_STR).serialize(now),
                "getDateSerializeUtility(TimeZone=null, String) with TimeZone=null has to format like default serialize utility");
        assertEquals(str,
                DateSerializeUtility.getDateSerializeUtility((ZoneId)null, DEFAULT_DATE_FORMAT_STR).serialize(now),
               "getDateSerializeUtility(ZoneID=null, String) with ZoneID=null has to format like default serialize utility");
        for (String timeZoneId: new String[] {null, DEFAULT_TIME_ZONE.getID()}) {
            for (String format: new String[] {null, DEFAULT_DATE_FORMAT_STR}) {
                assertEquals(str,
                        DateSerializeUtility.getDateSerializeUtility(timeZoneId, format).serialize(now),
                        "getDateSerializeUtility(\""+timeZoneId+"\", \""+format+"\") has to format like default serialize utility");

            }
        }
    }

    @Test
    void testClear() {
        DateSerializeUtility dsu = DateSerializeUtility.getDateSerializeUtility(DEFAULT_TIME_ZONE, DEFAULT_DATE_FORMAT_STR);
        DateSerializeUtility.clear();
        assertNotSame(dsu, DateSerializeUtility.getDateSerializeUtility(DEFAULT_TIME_ZONE, DEFAULT_DATE_FORMAT_STR),
                "getDateSerializeUtility(...) from equls parameters has to change result after clear() method call");

    }

}