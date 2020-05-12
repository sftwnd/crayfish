/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.format;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Serialize date to String and deserialize it back utility
 *
 * Created: 2016-02-08
 *
 * @version 1.1.0
 * @since 1.0.0
 */
@Slf4j
public final class DateSerializeUtility {

    private static final Map<TimeZone, Map<String, DateSerializeUtility>> utilities = new HashMap<>();

    public static final String DEFAULT_DATE_FORMAT_STR = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getID());

    @SuppressWarnings("squid:S5164")
    private ThreadLocal<DateFormat> dateFormat;

    protected DateSerializeUtility() {
        this((TimeZone) null);
    }

    protected DateSerializeUtility(String timeZoneId) {
        this(timeZoneId, null);
    }

    protected DateSerializeUtility(String timeZoneId, String dateFormatStr) {
        this(timeZoneId == null ? null : TimeZone.getTimeZone(timeZoneId), dateFormatStr);
    }

    protected DateSerializeUtility(TimeZone timeZone) {
        this(timeZone, null);
    }

    @SuppressWarnings("squid:HiddenFieldCheck")
    public DateSerializeUtility(TimeZone timeZone, String dateFormatStr) {
        dateFormat = ThreadLocal.withInitial(() -> {
            DateFormat dateFormat = new SimpleDateFormat(dateFormatStr == null ? DEFAULT_DATE_FORMAT_STR : dateFormatStr);
            dateFormat.setTimeZone(timeZone == null ? DEFAULT_TIME_ZONE : timeZone);
            return dateFormat;
        });
    }

    public @Nullable String serialize(@Nullable Date dateTime) {
        logger.trace("serialize(Date:`{}`)", dateTime);
        return dateTime == null ? null : dateFormat.get().format(dateTime);
    }

    public @Nullable String serialize(@Nullable Instant dateTime) {
        return serialize(Optional.ofNullable(dateTime).map(Date::from).orElse(null));
    }

    public @Nullable Date deserialize(@Nullable String dateTime) throws ParseException {
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
        return getDateSerializeUtility(timeZoneId == null ? null : TimeZone.getTimeZone(timeZoneId), dateFormat);
    }

    public static DateSerializeUtility getDateSerializeUtility(ZoneId zoneId, String dateFormat) {
        return getDateSerializeUtility(zoneId == null ? null : TimeZone.getTimeZone(zoneId), dateFormat);
    }

    public static DateSerializeUtility getDateSerializeUtility(TimeZone timeZone, String dateFormat) {
        Map<String, DateSerializeUtility> dateFormatMap;
        synchronized (utilities) {
            dateFormatMap = utilities.computeIfAbsent(timeZone, tz -> new HashMap<>());
        }
        synchronized (dateFormatMap) {
            return dateFormatMap.computeIfAbsent(dateFormat, df -> new DateSerializeUtility(timeZone, df));
        }
    }

    public static void clear() {
        synchronized (utilities) {
            utilities.clear();
        }
    }

}
