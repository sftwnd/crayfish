package com.github.sftwnd.crayfish.utils;

import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import static org.junit.Assert.*;

/**
 *
 * Test of class ToStringer
 *
 * @author Andrey D. Shindarev
 * @author CJSC PETER-SERVICE
 *
 */
public final class ToStringerTest {

    private static ResourceBundle bundle;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private String strDateIn      = "2015-01-02T12:01:02.000+03:00";
    private String strDateOut;
    private long   timestamp;
    private String strResultNamed;
    private String strResultUnNamed;

    @Before
    public void init() throws ParseException {
        timestamp  = dateFormat.parse(strDateIn).getTime();
        strDateOut = dateFormat.format(new Date(timestamp));
        strResultNamed   = "{\"OOPS\":{\"раз\":1,\"два\":2,\"три\":3.0,\"четыре\":\"Четыре\",\"пять\":\""+strDateOut+"\",\"шесть\":\""+strDateOut+"\"}}";
        strResultUnNamed = "{\"раз\":1,\"два\":2,\"три\":3.0,\"четыре\":\"Четыре\",\"пять\":\""+strDateOut+"\",\"шесть\":\""+strDateOut+"\"}";
    }

    @Test
    public void checkNamedTest() {
        ToStringer ts = new ToStringer("OOPS");
        ts.append("раз", 1); ts.append("два", 2L); ts.append("три", 3d); ts.append("четыре", "Четыре");
        ts.append("пять", new Date(timestamp)); ts.append("шесть", new Timestamp(timestamp));
        assertEquals("Check named ToStringer", strResultNamed, ts.toString());
    }

    @Test
    public void checkUnNamedTest() {
        ToStringer ts = new ToStringer();
        ts.append("раз", 1); ts.append("два", 2L); ts.append("три", 3d); ts.append("четыре", "Четыре");
        ts.append("пять", new Date(timestamp)); ts.append("шесть", new Timestamp(timestamp));
        assertEquals("Check unNamed ToStringer", strResultUnNamed, ts.toString());
    }

    @Test
    public void checkToStringNamedPushNamedTest() {
        ToStringer ts = new ToStringer("OOPS");
        assertEquals("Check ToStringer(Named) push(Named)", "{\"OOPS\":{\"Name\":БЛА}}", ts.push("Name", "БЛА").toString());
    }

    @Test
    public void checkToStringNamedPushUnNamedTest() {
        ToStringer ts = new ToStringer("OOPS");
        assertEquals("Check ToStringer(Named) push(UnNamed)", "{\"OOPS\":{БЛА}}", ts.push("БЛА").toString());
    }

    @Test
    public void checkToStringUnNamedPushNamedTest() {
        ToStringer ts = new ToStringer();
        assertEquals("Check ToStringer(UnNamed) push(Named)", "{\"Name\":БЛА}", ts.push("Name", "БЛА").toString());
    }

    @Test
    public void checkToStringUnNamedPushUnNamedTest() {
        ToStringer ts = new ToStringer();
        assertEquals("Check ToStringer(UnNamed) push(UnNamed)", "{БЛА}", ts.push("БЛА").toString());
    }

    @Test
    public void checkToStringObjectTest() {
        ToStringer ts = new ToStringer();
        Object obj = new String("Object");
        assertEquals("Check ToStringer(Object)", "{\"Object\":\"Object\"}", ts.append("Object",obj).toString());
    }

    @Test
    public void checkClazzConstructor() {
        ToStringer ts1 = new ToStringer(this.getClass());
        ToStringer ts2 = new ToStringer(this.getClass());
        ts1.append("name",(String)null);
        ts2.append("name",(String)null);
        assertEquals("Check ToStringer.clazz.update(null String) equality", ts1.toString(), ts2.toString());
    }

    @Test
    public void checkWithNullsTest() {
        ToStringer tsWithNulls = new ToStringer("OOPS", true);
        ToStringer tsWithoutNulls = new ToStringer("OOPS", false);
        tsWithNulls.append("раз", 1).append("два", (String)null).append("три", 3d);
        tsWithoutNulls.append("раз", 1).append("два", (String)null).append("три", 3d);
        assertTrue("Check ToStringer(withNulls) contains 'null'", tsWithNulls.toString().contains("null"));
        assertFalse("Check ToStringer(withoutNulls) does not contains 'null'", tsWithoutNulls.toString().contains("null"));
        assertNotEquals("Check ToStringer(withNulls) != ToStringer(withoutNulls)", tsWithNulls.toString(), tsWithoutNulls.toString());
    }


}
