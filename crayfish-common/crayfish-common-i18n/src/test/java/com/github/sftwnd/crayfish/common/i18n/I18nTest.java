package com.github.sftwnd.crayfish.common.i18n;

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;

import static org.junit.jupiter.api.Assertions.assertEquals;

class I18nTest {

    private static final String testMessage = "Тестовое сообщение";

    private static void assertMessageSourceValue(MessageSource messageSource) {
        assertEquals(testMessage, messageSource.message("test.message"));

    }

    @Test
    void getMessageSourcePackage() {
        MessageSource messageSource = I18n.getMessageSource(MethodHandles.lookup().lookupClass().getPackage());
        assertMessageSourceValue(messageSource);
    }

    @Test
    void getMessageSourceClass() {
        MessageSource messageSource = I18n.getMessageSource(MethodHandles.lookup().lookupClass());
        assertMessageSourceValue(messageSource);
    }

    @Test
    void getMessageSourcePackageBundle() {
        MessageSource messageSource = I18n.getMessageSource(MethodHandles.lookup().lookupClass().getPackage(), "messages");
        assertMessageSourceValue(messageSource);
    }

    @Test
    void getMessageSourceClassBundle() {
        MessageSource messageSource = I18n.getMessageSource(MethodHandles.lookup().lookupClass(), "messages");
        assertMessageSourceValue(messageSource);
    }

}