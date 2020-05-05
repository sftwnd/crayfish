package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.github.sftwnd.crayfish.common.format.DateSerializeUtility;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Created by ashindarev on 08.02.16.
 */
@Slf4j
public final class JsonDateDeserializer extends JsonDeserializer<Date> {

    @SuppressWarnings("squid:S4784")
    private static final Pattern pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}(T\\d{1,2}(\\:\\d{1,2}(\\:\\d{1,2}(\\.\\d+)?)?)?)?(Z|z|(?:[\\+|-]\\d{1,2}\\:\\d{1,2}))?.*$");
    private static String defaultTimeZoneId = "UTC";

    private static ThreadLocal<String> timeZone = new ThreadLocal<>();

    private static final String[] DATE_DESERIALIZE_FORMAT_ELEMENTS = new String[] { "yyyy-MM-dd", "'T'HH", ":mm", ":ss", ".SSS", "XXX" };

    // Поддерживаются только производные форматы от ISO 8601:
    // yyyy-MM-dd'T'HH:mm:ss.SSS    yyyy-MM-dd'T'HH:mm:ss.SSSXXX
    // yyyy-MM-dd'T'HH:mm:ss        yyyy-MM-dd'T'HH:mm:ssXXX
    // yyyy-MM-dd'T'HH:mm           yyyy-MM-dd'T'HH:mmXXX
    // yyyy-MM-dd'T'HH              yyyy-MM-dd'T'HHXXX
    // yyyy-MM-dd                   yyyy-MM-ddXXX
    @Override
    @SneakyThrows
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        try {
            Matcher matcher = pattern.matcher(jsonParser.getText());
            DateSerializeUtility dateSerializeUtility;
            if (matcher.matches()) {
                StringBuilder sb = new StringBuilder(DATE_DESERIALIZE_FORMAT_ELEMENTS[0]);
                IntStream.rangeClosed(1, 5).forEach(i -> { if (matcher.group(i) != null) sb.append(DATE_DESERIALIZE_FORMAT_ELEMENTS[i]);});
                dateSerializeUtility = DateSerializeUtility.getDateSerializeUtility(getTimeZoneId(), sb.toString());
            } else {
                dateSerializeUtility = DateSerializeUtility.getDateSerializeUtility(getTimeZoneId(), null);
            }
            Date result =  dateSerializeUtility.deserialize(jsonParser.getText());
            logger.trace("deserialize(date:`{}`)", jsonParser.getText());
            return result;
        } catch (ParseException pex) {
            logger.error("unable to deserialize(date:`{}`)", jsonParser.getText());
            throw pex;
        }
    }

    public static String getTimeZoneId() {
        return Optional.ofNullable(timeZone.get()).orElse(defaultTimeZoneId);
    }

    public static void setTimeZoneId(String timeZoneId) {
        String currentTimeZoneId = getTimeZoneId();
        // Если временная зона та же, что и установлена
        if ( ( timeZoneId == null && currentTimeZoneId == null) ||
             ( timeZoneId != null && timeZoneId.equals(currentTimeZoneId) )
           )
        {
            // То ничего не меняем
            return;
        }
        if (timeZoneId == null) {
            timeZone.remove();
        } else {
            timeZone.set( timeZoneId.matches("(?i)UTC.+")
                        ? String.valueOf(new StringBuilder("GMT").append(timeZoneId.substring(3)))
                        : timeZoneId);
        }
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
            defaultTimeZoneId =
                    Optional.ofNullable(timeZoneId)
                            .map(tz -> tz.matches("(?i)UTC.+")
                                     ? String.valueOf(new StringBuilder("GMT").append(tz.substring(3)))
                                     : tz)
                            .orElse("UTC");
        }
    }

    public static String getDefaultTimeZoneId() {
        return defaultTimeZoneId;
    }

    public static void clearDefaultTimeZoneId() {
        setDefaultTimeZoneId(null);
    }

}
