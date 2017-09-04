package com.sftwnd.crayfish.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * Utility to make Json-like (not equal) string
 *
 * @author Andrey D. Shindarev
 * @author CJSC PETER-SERVICE
 *
 */
public class ToStringer {

    // Разделитель-запятая
    private static final String comma = ",";
    // Формат даты с долями и часовым поясом
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    // Формат даты с долями и часовым поясом
    private static final String doubleQuotes = "\"";
    // Формат даты с долями и часовым поясом
    private static final String emptyString = "";
    // Буфер для формирования строки
    private final StringBuilder sb = new StringBuilder("{");
    // Базовый суффикс
    private String suffix = "}";
    // Префикс следующего элемента
    private String prefix = "";
    //
    private boolean withNulls;

    /**
     * Create instance based on Class
     *
     */
    public ToStringer(Class<?> clazz) {
        this(clazz, false);
    }

    /**
     * Create instance based on Class
     *
     */
    public ToStringer(Class<?> clazz, boolean withNulls) {
        this(clazz != null ? clazz.getSimpleName() : (String)null, withNulls);
    }

    /**
     * Create unnamed instance
     *
     */
    public ToStringer() {
        this((String)null);
    }

    /**
     * Create instance based on String
     *
     */
    public ToStringer(String name) {
        this(name, false);
    }

    /**
     * Create instance based on String
     *
     */
    public ToStringer(String name, boolean withNulls) {
        if (name != null) {
            sb.append('"').append(name).append("\":{");
            suffix = "}}";
        }
        this.withNulls = withNulls;
    }

    public ToStringer append(String name, String value) {
        return append(name, value, true);
    }

    public ToStringer append(String name, String value, boolean useDoubleQuotes) {
        if (suffix != null && (value != null || withNulls)) {
            sb.append(prefix).append('"').append(name).append("\":");
            if (value == null) {
                sb.append("null");
            } else {
                sb.append(useDoubleQuotes ? doubleQuotes : emptyString).append(value).append(useDoubleQuotes ? doubleQuotes : emptyString);
            }
            prefix = comma;
        }
        return this;
    }

    public ToStringer push(String name, String value) {
        if (suffix != null) {
            sb.append(prefix);
            if (name != null) {
                sb.append('"').append(name).append("\":");
            }
            sb.append(value == null ? "null" : value);
            prefix = comma;
        }
        return this;
    }

    public ToStringer push(String value) {
        return push(null, value);
    }

    public ToStringer append(String name, Number value) {
        if (suffix != null) {
            sb.append(prefix).append('"').append(name).append("\":")
                    .append(value == null ? "null" : value.toString());
            prefix = comma;
        }
        return this;
    }

    public ToStringer append(String name, Date value) {
        return append(name, value == null ? (String) null : dateFormat.format(value));
    }

    public ToStringer append(String name, Timestamp value) {
        return append(name, value == null ? (Date) null : new Date(value.getTime()));
    }

    public ToStringer append(String name, Object obj) {
        return obj == null ? append(name, (String)null) : append(name, String.valueOf(obj), !(obj instanceof List));
    }

    @Override
    public String toString() {
        if (suffix != null) {
            sb.append(suffix);
            suffix = null;
        }
        return String.valueOf(sb);
    }

}
