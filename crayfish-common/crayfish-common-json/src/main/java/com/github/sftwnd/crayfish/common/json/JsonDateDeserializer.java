package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.github.sftwnd.crayfish.common.format.DateSerializeUtility;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * <p>Json Data Deserializer using {@link DateSerializeUtility} with ability to control timeZone</p>
 *
 * Created 2016-02-08
 *
 * @author Andrey D. Shindarev
 * @version 1.1.1
 * @since 1.0.0
 */
@Slf4j
public final class JsonDateDeserializer extends JsonDeserializer<Date> {

    @SuppressWarnings("squid:S4784")
    private static final Pattern pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}(T\\d{1,2}(\\:\\d{1,2}(\\:\\d{1,2}(\\.\\d+)?)?)?)?(Z|z|(?:[\\+|-]\\d{1,2}\\:\\d{1,2}))?.*$");
    private static String defaultTimeZoneId = "UTC";

    private static ThreadLocal<String> timeZone = new ThreadLocal<>();

    private static final String[] DATE_DESERIALIZE_FORMAT_ELEMENTS = new String[] { "yyyy-MM-dd", "'T'HH", ":mm", ":ss", ".SSS", "XXX" };

    /**
     * Deserialize date from String
     *
     * Supportet formats (as part of ISO 8601 format):
     * yyyy-MM-dd'T'HH:mm:ss.SSS    yyyy-MM-dd'T'HH:mm:ss.SSSXXX
     * yyyy-MM-dd'T'HH:mm:ss        yyyy-MM-dd'T'HH:mm:ssXXX
     * yyyy-MM-dd'T'HH:mm           yyyy-MM-dd'T'HH:mmXXX
     * yyyy-MM-dd'T'HH              yyyy-MM-dd'T'HHXXX
     * yyyy-MM-dd                   yyyy-MM-ddXXX
     *
     * @param jsonParser
     * @param deserializationContext
     * @return date
     * @throws IOException
     */
    @Override
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
            throw new IOException(pex.getMessage(), pex);
        }
    }

    /**
     * Get timezone format for current Thread
     * @return TimeZoneId Sting value
     */
    public static @Nonnull String getTimeZoneId() {
        return Optional.ofNullable(timeZone.get()).orElse(defaultTimeZoneId);
    }

    /**
     * Set timezone for current Thread
     * @param timeZoneId String value of TimeZoneId
     */
    public static void setTimeZoneId(String timeZoneId) {
        if (timeZoneId == null || timeZoneId.isBlank()) {
            timeZone.remove();
        } else if (!timeZoneId.equals(getTimeZoneId())) {
            timeZone.set( timeZoneId.matches("(?i)UTC.+")
                        ? String.valueOf(new StringBuilder("GMT").append(timeZoneId.substring(3)))
                        : timeZoneId);
        }
    }

    /**
     * Set current Thread timeZoneId to the current timeZoneId
     */
    public static void setLocalTimeZone() {
        timeZone.set(Calendar.getInstance().getTimeZone().getID());
    }

    /**
     * Set current Thread timeZoneId to default
     */
    public static void clearTimeZoneId() {
        setTimeZoneId(null);
    }

    /**
     * Set JVM default timeZoneId to local
     */
    public static void setDefaultTimeZoneIdToLocal() {
        setDefaultTimeZoneId(Calendar.getInstance().getTimeZone().getID());
    }

    /**
     * Set JVM default timeZoneId to defined
     * @param timeZoneId time zone id
     */
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

    /**
     * Get system default timeZoneId
     * @return String value of default timeZone
     */
    public static String getDefaultTimeZoneId() {
        return defaultTimeZoneId;
    }

    /**
     * Set default time zone to the current
     */
    public static void clearDefaultTimeZoneId() {
        setDefaultTimeZoneId(null);
    }

}
