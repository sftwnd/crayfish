package com.sftwnd.crayfish.utils.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by ashindarev on 08.02.16.
 */
public final class DateSerializeUtility {

    private static final Logger logger = LoggerFactory.getLogger(DateSerializeUtility.class);
    private static final Map<TimeZone, Map<String, DateSerializeUtility>> utilities = new HashMap<>();

    public  static final String defaultDateFormatStr = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public  static final TimeZone defaultTimeZone = TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getID());

    private ThreadLocal<DateFormat> dateFormat;

    public DateSerializeUtility() {
        this((TimeZone) null);
    }

    public DateSerializeUtility(String timeZoneId) {
        this(timeZoneId, null);
    }

    public DateSerializeUtility(String timeZoneId, String dateFormatStr) {
        this(timeZoneId == null ? null : TimeZone.getTimeZone(timeZoneId), dateFormatStr);
    }

    public DateSerializeUtility(TimeZone timeZone) {
        this(timeZone, null);
    }

    public DateSerializeUtility(TimeZone timeZone, String dateFormatStr) {
        dateFormat = new ThreadLocal<DateFormat>() {
            @Override
            protected DateFormat initialValue() {
                DateFormat dateFormat = new SimpleDateFormat(dateFormatStr == null ? defaultDateFormatStr : dateFormatStr);
                dateFormat.setTimeZone(timeZone == null ? defaultTimeZone : timeZone);
                return dateFormat;
            }
        };
    }

    public String serialize(Date dateTime) {
        logger.trace("serialize(Date:`{}`)", dateTime);
        return dateTime == null ? null : dateFormat.get().format(dateTime);
    }

    public String serialize(Instant dateTime) {
        return serialize(new Date(dateTime.getEpochSecond()));
    }

    public Date deserialize(String dateTime) throws ParseException {
        try {
            Date result =  dateTime == null || dateTime.trim().length() == 0
                           ? null
                           : dateFormat.get().parse(dateTime);
            logger.trace("deserialize(`{}`)", dateTime);
            return result;
        } catch (ParseException ex) {
            logger.error("Unable to deserialize string: `{}` as Date.", dateTime);
            throw ex;
        }
    }

    public static String defaultSerialize(Date dateTime) {
        return getDateSerializeUtility((TimeZone)null, null).serialize(dateTime);
    }

    public static Date defaultDeserialize(String dateTime) throws ParseException {
        return getDateSerializeUtility((TimeZone)null, null).deserialize(dateTime);
    }

    public static DateSerializeUtility getDateSerializeUtility(String timeZoneId, String dateFormat) {
        return getDateSerializeUtility(timeZoneId == null ? (TimeZone) null : TimeZone.getTimeZone(timeZoneId), dateFormat);
    }

    public static DateSerializeUtility getDateSerializeUtility(ZoneId zoneId, String dateFormat) {
        return getDateSerializeUtility(zoneId == null ? (TimeZone) null : TimeZone.getTimeZone(zoneId), dateFormat);
    }

    public static DateSerializeUtility getDateSerializeUtility(TimeZone timeZone, String dateFormat) {
        Map<String, DateSerializeUtility> dateFormatMap = utilities.get(timeZone);
        if (dateFormatMap == null) {
            synchronized (utilities) {
                dateFormatMap = utilities.get(timeZone);
                if (dateFormatMap == null) {
                    dateFormatMap = new HashMap<>();
                    utilities.put(timeZone, dateFormatMap);
                }
            }
        }
        DateSerializeUtility dateSerializeUtility = dateFormatMap.get(dateFormat);
        if (dateSerializeUtility == null) {
            synchronized (dateFormatMap) {
                dateSerializeUtility = dateFormatMap.get(dateFormat);
                if (dateSerializeUtility == null) {
                    dateSerializeUtility = new DateSerializeUtility(timeZone, dateFormat);
                    dateFormatMap.put(dateFormat, dateSerializeUtility);
                }
            }
        }
        return dateSerializeUtility;
    }

    public static void clear() {
        synchronized (utilities) {
            if (utilities != null) {
                utilities.clear();
            }
        }
    }

}
