package com.github.sftwnd.crayfish.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ashindarev on 03.02.16.
 */
public abstract class ConfigUtility implements BaseConfigUtility {

    private static final Pattern semicolonListPattern = Pattern.compile("([\\;])?([^\\;]*)");
    private static final DateFormat defaultDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final Pattern substitutionPattern = Pattern.compile("\\{\\{(.+?)(?:\\[(\\d+)\\])?(?:([\\+|\\-|\\*|\\/|\\%])(\\d+))?\\}\\}");

    private final DateFormat dateFormat;
    private final Map<String, String[]> valueList = new HashMap<>();

    public ConfigUtility() {
        this((DateFormat) null);
    }

    public ConfigUtility(String dateFormat) {
        this(dateFormat == null ? null : new SimpleDateFormat(dateFormat));
    }

    public ConfigUtility(DateFormat dateFormat) {
        this.dateFormat = dateFormat == null ? defaultDateFormat : dateFormat;
    }

    /*
        loadValue
     */
    public abstract String loadValue(String key);

    public String loadValue(String prefix, String name) {
        return loadValue(getKey(prefix, name));
    }

    public String loadValue(Class<?> clazz, String name) {
        return loadValue(getKey(clazz, name));
    }

    /*
        getKey
     */
    public String getKey(String prefix, String name) {
        return String.valueOf(
                prefix == null
                        ? name
                        : name == null
                        ? prefix
                        : new StringBuilder(prefix).append('.').append(name)
        );
    }

    public String getKey(Class<?> clazz, String name) {
        return getKey(clazz == null ? null : clazz.getSimpleName(), name);
    }

    public String getKey(Class<?> clazz, String step, String name) {
        return getKey(getKey(clazz, step), name);
    }

    public String getKey(String prefix, String step, String name) {
        return getKey(getKey(prefix, step), name);
    }

    /*
        getValues
     */
    public String[] getValues(Class<?> clazz, String step, String name) {
        return getValues(getKey(clazz, step, name));
    }

    public String[] getValues(Class<?> clazz, String name) {
        return getValues(getKey(clazz, name));
    }

    public String[] getValues(String prefix, String step, String name) {
        return getValues(getKey(prefix, step, name));
    }

    @Override
    public String[] getValues(String prefix, String name) {
        return getValues(getKey(prefix, name));
    }

    @Override
    public String[] getValues(String key) {
        String[] values = this.valueList.get(key);
        if (values == null) {
            synchronized (this.valueList) {
                values = this.valueList.get(key);
                if (values == null) {
                    List<String> list = constructList(loadValue(key));
                    values = list == null ? null : list.toArray(new String[]{});
                    this.valueList.put(key, values);
                }
            }
        }
        return values;
    }

    /*
        getLongValues
     */

    public Long[] getLongValues(Class<?> clazz, String step, String name) {
        return getLongValues(getKey(clazz, step, name));
    }

    public Long[] getLongValues(Class<?> clazz, String name) {
        return getLongValues(getKey(clazz, name));
    }

    @Override
    public Long[] getLongValues(String prefix, String name) {
        return getLongValues(getKey(prefix, name));
    }

    public Long[] getLongValues(String prefix, String step, String name) {
        return getLongValues(getKey(prefix, step, name));
    }

    @Override
    public Long[] getLongValues(String key) {
        Long[] result = null;
        String[] values = getValues(key);
        if (values != null) {
            result = new Long[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = Long.valueOf(values[i]);
            }
        }
        return result;
    }

    /*
        getValue
     */
    public String getValue(Class<?> clazz, String step, String name, int id) {
        return getValue(getKey(clazz, step), name, id);
    }

    public String getValue(String prefix, String step, String name, int id) {
        return getValue(getKey(prefix, step), name, id);
    }

    public String getValue(Class<?> clazz, String name, int id) {
        return getValue(clazz, null, name, id);
    }

    public String getValue(Class<?> clazz, String name) {
        return getValue(clazz, name, -1);
    }

    @Override
    public String getValue(String prefix, String name) {
        return getValue(prefix, name, -1);
    }

    @Override
    public String getValue(String prefix, String name, int id) {
        String text = loadValue(prefix, name);
        if (text != null) {
            Matcher matcher = substitutionPattern.matcher(text);
            Map<String, String> substitutions = new HashMap<>();
            while (matcher.find()) {
                if (!substitutions.containsKey(matcher.group(0))) {
                    String result = constructSubstitution(prefix, matcher.group(1), matcher.group(3), matcher.group(4), matcher.group(2) == null ? id : Integer.parseInt(matcher.group(2)));
                    substitutions.put(matcher.group(0), result);
                }
            }
            for (Map.Entry<String, String> entry : substitutions.entrySet()) {
                if (entry.getValue() != null) {
                    text = text.replace(entry.getKey(), entry.getValue());
                }
            }
        }
        return text;
    }

    @Override
    public String getValue(String name) {
        return getValue((String)null, name);
    }

    @Override
    public String getValue(String name, int id) {
        return getValue((String)null, name, id);
    }

    /*
        constructSubstitution
     */
    private String constructSubstitution(String prefix, String name, String operation, String add, int id) {
        String value;
        if ("currentTimestamp".equals(name)) {
            value = String.valueOf(System.currentTimeMillis());
        } else {
            value = loadValue(prefix, name);
            if (value == null && id >= 0) {
                String[] values = getValues(prefix, String.valueOf(new StringBuilder(name).append('s')));
                value = values == null ? null : values[id];
            }
        }
        if (operation != null) {
            Long val = Long.valueOf(value);
            Long addVal = Long.valueOf(add);
            if (val != null) {
                switch (operation) {
                    case "+":
                        val = val + addVal;
                        break;
                    case "-":
                        val = val - addVal;
                        break;
                    case "*":
                        val = val * addVal;
                        break;
                    case "/":
                        val = val / addVal;
                        break;
                    case "%":
                        val = val % addVal;
                        break;
                    default:
                        break;
                }
            }
            value = String.valueOf(val);
        }
        if ("currentTimestamp".equals(name)) {
            value = dateFormat.format(new Timestamp(Long.parseLong(value)));
        }
        return value;
    }

    /*
        constructList
     */
    public static final List<String> constructList(String text) {
        List<String> elements = null;
        if (text != null) {
            elements = new ArrayList<>();
            Matcher matcher = semicolonListPattern.matcher(text);
            while (matcher.find()) {
                if (matcher.group(1) != null && elements.isEmpty()) {
                    elements.add(null);
                }
                if (matcher.group(1) != null || elements.isEmpty()) {
                    elements.add(matcher.group(2).trim().length() == 0 ? null : matcher.group(2).trim());
                }
            }
        }
        return elements;
    }

    public BaseConfigUtility getBaseConfigUtility(Class<?> clazz, String step) {
        return getBaseConfigUtility(getKey(clazz, step));
    }

    public BaseConfigUtility getBaseConfigUtility(String prefix, String step) {
        return getBaseConfigUtility(getKey(prefix, step));
    }

    public BaseConfigUtility getBaseConfigUtility(Class<?> clazz) {
        return clazz == null ? null : getBaseConfigUtility(getKey(clazz, null));
    }

    public BaseConfigUtility getBaseConfigUtility(final String prefix) {
        final ConfigUtility configUtility = this;
        return new BaseConfigUtility() {


            @Override
            public String getValue(String step, String name) {
                return getValue(step, name, -1);
            }

            @Override
            public String getValue(String name) {
                return getValue(name, -1);
            }

            @Override
            public String getValue(String step, String name, int id) {
                return configUtility.getValue(prefix, step, name, id);
            }

            @Override
            public String getValue(String name, int id) {
                return configUtility.getValue(prefix, name, id);
            }

            @Override
            public String[] getValues(String step, String name) {
                return configUtility.getValues(prefix, step, name);
            }

            @Override
            public String[] getValues(String name) {
                return configUtility.getValues(prefix, name);
            }

            @Override
            public Long[] getLongValues(String step, String name) {
                return configUtility.getLongValues(prefix, step, name);
            }

            @Override
            public Long[] getLongValues(String name) {
                return configUtility.getLongValues(prefix, name);
            }
        };

    }

}