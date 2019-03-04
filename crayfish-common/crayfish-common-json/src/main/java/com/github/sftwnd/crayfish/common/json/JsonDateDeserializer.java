package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.github.sftwnd.crayfish.common.format.DateSerializeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ashindarev on 08.02.16.
 */
public final class JsonDateDeserializer extends JsonDeserializer<Date> {

    private static final Logger logger = LoggerFactory.getLogger(JsonDateDeserializer.class);
    private static final Pattern pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}(T\\d{1,2}(\\:\\d{1,2}(\\:\\d{1,2}(\\.\\d+)?)?)?)?(Z|z|(?:[\\+|-]\\d{1,2}\\:\\d{1,2}))?.*$");
    private static String defaultTimeZoneId = "UTC";

    private static ThreadLocal<String> timeZone = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return defaultTimeZoneId;
        }
    };

    // Поддерживаются только производные форматы от ISO 8601:
    // yyyy-MM-dd'T'HH:mm:ss.SSS    yyyy-MM-dd'T'HH:mm:ss.SSSXXX
    // yyyy-MM-dd'T'HH:mm:ss        yyyy-MM-dd'T'HH:mm:ssXXX
    // yyyy-MM-dd'T'HH:mm           yyyy-MM-dd'T'HH:mmXXX
    // yyyy-MM-dd'T'HH              yyyy-MM-dd'T'HHXXX
    // yyyy-MM-dd                   yyyy-MM-ddXXX
    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        try {
            Matcher matcher = pattern.matcher(jsonParser.getText());
            DateSerializeUtility dateSerializeUtility;
            if (matcher.matches()) {
                StringBuilder sb = new StringBuilder("yyyy-MM-dd");
                if (matcher.group(1) != null) {
                    sb.append("'T'HH");
                    if (matcher.group(2) != null) {
                        sb.append(":mm");
                        if (matcher.group(3) != null) {
                            sb.append(":ss");
                            if (matcher.group(4) != null) {
                                sb.append(".SSS");
                            }
                        }
                    }
                }
                if (matcher.group(5) != null) {
                    sb.append("XXX");
                }
                dateSerializeUtility = DateSerializeUtility.getDateSerializeUtility(getTimeZoneId(), sb.toString());
            } else {
                dateSerializeUtility = DateSerializeUtility.getDateSerializeUtility(getTimeZoneId(), null);
            }
            Date result =  dateSerializeUtility.deserialize(jsonParser.getText());
            logger.trace("deserialize(date:`{}`)", jsonParser.getText());
            return result;
        } catch (ParseException pex) {
            logger.error("unable to deserialize(date:`{}`)", jsonParser.getText());
            throw new RuntimeException(pex);
        }
    }

    public static String getTimeZoneId() {
        return timeZone.get();
    }

    public static void setTimeZoneId(String timeZoneId) {
        String currentTimeZoneId = getTimeZoneId();
        // Если временная зона та же, что и установлена
        if ( currentTimeZoneId == timeZoneId ||
             ( timeZoneId != null && timeZoneId.equals(currentTimeZoneId) )
           )
        {
            // То ничего не меняем
            return;
        }
        timeZone.set( timeZoneId == null
                      ? defaultTimeZoneId
                     // UTC зону не используем со смещением. Вместо неё берём GMT плюс смещение
                      : timeZoneId.matches("(?i)UTC.+")
                        ? String.valueOf(new StringBuilder("GMT").append(timeZoneId.substring(3)))
                        : timeZoneId );
    }

    public static void setLocalTimeZone() {
        timeZone.set(Calendar.getInstance().getTimeZone().getID());
    }

    public static void clearTimeZoneId() {
        setTimeZoneId(null);
    }

    public static void setDefaultTimeZoneIdToLocal() {
        setDefaultTimeZoneId(Calendar.getInstance().getTimeZone().getID());
    }

    public static void setDefaultTimeZoneId(String timeZoneId) {
        synchronized (JsonDateDeserializer.class) {
            defaultTimeZoneId = timeZoneId == null
                    ? "UTC"
                    : timeZoneId.matches("(?i)UTC.+")
                    ? String.valueOf(new StringBuilder("GMT").append(timeZoneId.substring(3)))
                    : timeZoneId;
        }
    }

    public static String getDefaultTimeZoneId() {
        return defaultTimeZoneId;
    }

    public static void clearDefaultTimeZoneId() {
        setDefaultTimeZoneId(null);
    }

}
