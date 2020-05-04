package com.github.sftwnd.crayfish.common.i18n;

import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * Интерфейс получения сообщения поользователся с возсожностью форматирования сообщения
 * (по правилам {@link java.text.MessageFormat}), указания локали и значения по умолчанию.
 */

public interface MessageSource {
    /**
     * Возвращает сообщение по указанному идентификационному коду.
     * @param code код идентифицирующий сообщение
     * @param args аргументы для заполнения параметров сообщения
     * @return найденное сообщение
     * @throws NoSuchMessageException
     */
    String message(@Nonnull String code, Object... args);

    /**
     * Возвращает сообщение по указанному идентификационному коду для указанной локали.
     * @param locale локаль в которой нужно возвратить сообщение
     * @param code код идентифицирующий сообщение
     * @param args аргументы для заполнения параметров сообщения
     * @return найденное сообщение
     * @throws NoSuchMessageException
     */
    String message(Locale locale, @Nonnull String code, Object... args);

    /**
     * Возвращает сообщение по указанному идентификационному коду.
     * Если сообщение не найдено, то возвращает указанное умолчальное сообщение.
     * @param code код идентифицирующий сообщение
     * @param defaultMessage сообщение по умолчанию
     * @param args аргументы для заполнения параметров сообщения
     * @return найденное сообщение
     */
    String messageDef(@Nonnull String code, String defaultMessage, Object... args);

    /**
     * Возвращает сообщение по указанному идентификационному коду для указанной локали.
     * @param locale локаль в которой нужно возвратить сообщение
     * @param code код идентифицирующий сообщение
     * @param defaultMessage сообщение по умолчанию
     * @param args аргументы для заполнения параметров сообщения
     * @return найденное сообщение
     */
    String messageDef(Locale locale, @Nonnull String code, String defaultMessage, Object... args);
}
