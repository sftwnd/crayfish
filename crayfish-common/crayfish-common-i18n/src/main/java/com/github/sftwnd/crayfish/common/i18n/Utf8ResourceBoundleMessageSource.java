package com.github.sftwnd.crayfish.common.i18n;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Источник сообщений пользователя на основе ResourceBundle
 */

public class Utf8ResourceBoundleMessageSource extends BaseResourceBoundleMessageSource {

    public Utf8ResourceBoundleMessageSource(@Nonnull String baseName) {
        super(baseName);
    }

    public Utf8ResourceBoundleMessageSource(@Nonnull Package pkg, @Nonnull String bundleName) {
        this(pkg.getName() + "." + bundleName);
    }

    public Utf8ResourceBoundleMessageSource(@Nonnull Class<?> clazz, @Nonnull String bundleName) {
        this(clazz.getPackage(), bundleName);
    }

    @Override
    protected ResourceBundle constructResourceBundle(@Nonnull String baseName, @Nonnull Locale locale) {
        return Utf8ResourceBundle.getBundle(baseName, locale);
    }

}
