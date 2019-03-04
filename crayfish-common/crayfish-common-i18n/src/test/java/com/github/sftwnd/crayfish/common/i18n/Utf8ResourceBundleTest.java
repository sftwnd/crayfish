package com.github.sftwnd.crayfish.common.i18n;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Utf8ResourceBundleTest {

    private static String packageName = MethodHandles.lookup().lookupClass().getPackage().getName();
    private static String bundleNmae = "messages";
    private static String testMessage;

    @BeforeAll
    public static void config() throws IOException {
        final Class<?> clazz = MethodHandles.lookup().lookupClass();
        String filePath = packageName.replace(".","/")+"/"+bundleNmae+"_ru.properties";
        BufferedReader isr = new BufferedReader(new InputStreamReader(clazz.getClassLoader().getResourceAsStream(filePath)));
        testMessage = isr.readLine().replace("test.message=","");
    }

    @Test
    public void testResourceBundleGetString() {
        ResourceBundle resourceBundle = Utf8ResourceBundle.getBundle(packageName + "." + bundleNmae);
        String str = resourceBundle.getString("test.message");
        assertEquals(testMessage, str);
    }

}