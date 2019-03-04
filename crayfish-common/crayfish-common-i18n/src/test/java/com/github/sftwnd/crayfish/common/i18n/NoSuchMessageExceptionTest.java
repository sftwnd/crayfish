package com.github.sftwnd.crayfish.common.i18n;

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;

import static org.junit.jupiter.api.Assertions.assertThrows;

class NoSuchMessageExceptionTest {

    @Test
    public void testExceptionThrows() {
        assertThrows(
                NoSuchMessageException.class
               ,() -> {
                    MessageSource messageSource = I18n.getMessageSource(MethodHandles.lookup().lookupClass());
                    messageSource.message("unknown message");
                }
        );
    }

}