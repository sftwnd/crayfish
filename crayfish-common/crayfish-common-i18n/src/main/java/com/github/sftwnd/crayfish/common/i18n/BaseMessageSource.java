package com.github.sftwnd.crayfish.common.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;

/**
 * Базовый класс помогающий реализовать классы реализующие интерфейс {@link MessageSource}.
 * Все методы интерфейса реализованы через вызовы других методов. Остаётся только реализовать
 * метод оставленный в этом классе абстрактным.
 */

public abstract class BaseMessageSource implements MessageSource {

    protected static String format(Locale locale, String message, Object... args) {
        if (args.length > 0) {
            MessageFormat messageFormat = new MessageFormat(message, locale);
            return messageFormat.format(args);
        } else {
            return message;
        }
    }

    @Override
    public String message(String code, Object... args) throws NoSuchMessageException {
        return message(Locale.getDefault(), code, args);
    }

    @Override
    abstract public String message(Locale locale, String code, Object... args) throws NoSuchMessageException;

    @Override
    public String messageDef(String code, String defaultMessage, Object... args) {
        return messageDef(Locale.getDefault(), code, defaultMessage, args);
    }

    @Override
    public String messageDef(Locale locale, String code, String defaultMessage, Object... args) {
        try {
            return message(locale, code, args);
        } catch (NoSuchMessageException e) {
            if (defaultMessage == null) {
                throw e;
            }
            return format(locale, defaultMessage, args);
        } catch (MissingResourceException e) {
            if (defaultMessage == null) {
                throw e;
            }
            return format(locale, defaultMessage, args);
        }
    }

}
