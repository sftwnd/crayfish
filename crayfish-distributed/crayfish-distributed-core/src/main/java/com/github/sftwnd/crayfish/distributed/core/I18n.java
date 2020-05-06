package com.github.sftwnd.crayfish.distributed.core;

import com.github.sftwnd.crayfish.common.i18n.MessageSource;

import java.lang.invoke.MethodHandles;

public class I18n {

    public static final MessageSource messageSource = com.github.sftwnd.crayfish.common.i18n.I18n.getMessageSource(MethodHandles.lookup().lookupClass().getPackage(), "messages");

    public static final MessageSource getMessageSource() {
        return messageSource;
    }

}
