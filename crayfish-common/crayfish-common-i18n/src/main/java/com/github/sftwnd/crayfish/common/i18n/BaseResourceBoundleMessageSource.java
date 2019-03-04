package com.github.sftwnd.crayfish.common.i18n;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Источник сообщений пользователя на основе ResourceBundle
 */

public abstract class BaseResourceBoundleMessageSource extends BaseMessageSource {

    private final String baseName;
    private final Map<Locale, ResourceBundle> byLocaleMap = new HashMap<>();

    public BaseResourceBoundleMessageSource(@Nonnull String baseName) {
        this.baseName = baseName;
    }

    @Override
    public String message(@Nonnull Locale locale, @Nonnull String code, Object... args) throws NoSuchMessageException {
        ResourceBundle resourceBundle;
        if (byLocaleMap.containsKey(locale)) {
            resourceBundle = byLocaleMap.get(locale);
        } else {
            resourceBundle = constructResourceBundle(baseName, locale);
            byLocaleMap.put(locale, resourceBundle);
        }

        String value = null;
        try {
            value = resourceBundle.getString(code);
        } catch (MissingResourceException e) {
            throw new NoSuchMessageException(e);
        }
        if (value == null) {
            throw new NoSuchMessageException(code, locale);
        }

        return format(locale, value, args);
    }

    protected abstract ResourceBundle constructResourceBundle(@Nonnull String baseName, @Nonnull Locale locale);

}
