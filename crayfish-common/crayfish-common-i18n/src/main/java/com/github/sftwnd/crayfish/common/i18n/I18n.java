package com.github.sftwnd.crayfish.common.i18n;

/**
 * Класс содержащий общий для всех, нижележащих классов, источник локализованных сообщений.
 */

public final class I18n {

    private final static String defaultBundleName = "messages";
    private final static MessageSource messageSource = getMessageSource(defaultBundleName);

    private final Package pack;

    private I18n(Package pack) {
        this.pack = pack;
    }

    public static MessageSource getMessageSource() {
        return messageSource;
    }

    private static MessageSource getMessageSource(String boundleName) {
        return getMessageSource(I18n.class.getPackage(), boundleName);
    }

    public static MessageSource getMessageSource(Package pack) {
        return getMessageSource(pack, defaultBundleName);
    }

    public static MessageSource getMessageSource(Class<?> clazz) {
        return getMessageSource(clazz, defaultBundleName);
    }

    public static MessageSource getMessageSource(Package pack, String boundleName) {
        return new Utf8ResourceBoundleMessageSource(pack, boundleName);
    }

    public static MessageSource getMessageSource(Class<?> clazz, String boundleName) {
        return getMessageSource(clazz.getPackage(), boundleName);
    }

}
