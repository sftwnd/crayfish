package com.github.sftwnd.crayfish.common.i18n;

import java.util.Locale;

/**
 *  Exception thrown when a message can't be resolved.
 */

public class NoSuchMessageException extends RuntimeException {

    private static final long serialVersionUID = 2191573760253673452L;

    /**
     * Create a new exception.
     * @param code code that could not be resolved for given locale
     * @param locale locale that was used to search for the code within
     */
    public NoSuchMessageException(String code, Locale locale) {
        super(String.format("No message found under code '%s' for locale '%s'.", code, locale));
    }

    /**
     * Create a new exception.
     * @param code code that could not be resolved for given locale
     */
    public NoSuchMessageException(String code) {
        this(code, Locale.getDefault());
    }

    public NoSuchMessageException(Throwable error) {
        super(error);
    }

}
