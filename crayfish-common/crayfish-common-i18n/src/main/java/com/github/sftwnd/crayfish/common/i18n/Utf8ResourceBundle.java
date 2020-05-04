package com.github.sftwnd.crayfish.common.i18n;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Optional;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * tmutil listlocalsnapshotdates / | while read line;
 * do
 * tmutil deletelocalsnapshots $line
 * donesupport
 *
 * Utility that allows having multi-byte characters inside java .property files.
 * It removes the need for Sun's native2ascii application, you can simply have
 * UTF-8 encoded editable .property files.
 *
 * Use: ResourceBundle bundle = Utf8ResourceBundle.getBundle("bundle_name");
 *
 * @author Tomas Varaneckas <tomas.varaneckas@gmail.com>
 */
public final class Utf8ResourceBundle {

    /**
     * Prevent instantiation
     */
    private Utf8ResourceBundle() {
    }

    /**
     * Gets the unicode friendly resource bundle
     *
     * @param baseName
     * @see ResourceBundle#getBundle(String)
     * @return Unicode friendly resource bundle
     */
    public static ResourceBundle getBundle(final String baseName) {
        return createUtf8PropertyResourceBundle(
                   ResourceBundle.getBundle(baseName)
               );
    }

    /**
     * Gets the unicode friendly resource bundle
     *
     * @param baseName
     * @see ResourceBundle#getBundle(String, Locale)
     * @return Unicode friendly resource bundle
     */
    public static ResourceBundle getBundle(final String baseName, Locale locale) {
        return createUtf8PropertyResourceBundle(
                ResourceBundle.getBundle(baseName, locale)
        );
    }

    /**
     * Creates unicode friendly {@link PropertyResourceBundle} if possible.
     *
     * @param bundle
     * @return Unicode friendly property resource bundle
     */
    @SuppressWarnings("unchecked")
    private static ResourceBundle createUtf8PropertyResourceBundle(final ResourceBundle bundle) {
        return Optional.ofNullable(bundle)
                .filter(PropertyResourceBundle.class::isInstance)
                .map(PropertyResourceBundle.class::cast)
                .map(Utf8PropertyResourceBundle::new)
                .map(ResourceBundle.class::cast)
                .orElse(bundle);
    }

    public static class EncodingNotSupportedException extends RuntimeException {
        public EncodingNotSupportedException(String text, Exception ex) {
            super(text, ex);
        }
    }

    /**
     * Resource Bundle that does the hard work
     */
    private static final class Utf8PropertyResourceBundle extends ResourceBundle {

        /**
         * Bundle with unicode data
         */
        private final PropertyResourceBundle bundle;
        private final String charsetName;
        /**
         * Initializing constructor
         *
         * @param bundle
         */
        private Utf8PropertyResourceBundle(final PropertyResourceBundle bundle) {
            this.bundle = bundle;
            final String vmSpecificationVersion = System.getProperty("java.vm.specification.version");
            this.charsetName = Optional.ofNullable(System.getProperty("java.util.PropertyResourceBundle.encoding"))
                                  .map( cs -> cs.toUpperCase().matches("UTF[-]?8") ? null : cs)
                                  .orElseGet( () ->
                                      vmSpecificationVersion == null || vmSpecificationVersion.matches("1\\..*")
                                      // Задаём как iso 8859.1
                                      ? "ISO-8859-1"
                                      // Иначе не определяем
                                      : null
                                   );
            }

        @Override
        @SuppressWarnings("unchecked")
        public Enumeration<String> getKeys() {
            return bundle.getKeys();
        }

        @Override
        protected Object handleGetObject(final String key) {
            final String value = bundle.getString(key);
            try {
                return value == null || this.charsetName == null
                     ? value
                     : new String(value.getBytes(this.charsetName), StandardCharsets.UTF_8);
            } catch (final UnsupportedEncodingException ex) {
                throw new EncodingNotSupportedException("Encoding not supported", ex);
            }
        }

    }

}