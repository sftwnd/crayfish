package com.github.sftwnd.crayfish.common.format.formatter;

import com.github.sftwnd.crayfish.common.state.DefaultsHolder;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static com.github.sftwnd.crayfish.common.format.formatter.TemporalFormatter.DEFAULT_FORMATTER;
import static com.github.sftwnd.crayfish.common.format.formatter.TemporalFormatter.DEFAUT_ZONE_ID;
import static com.github.sftwnd.crayfish.common.state.DefaultsHolder.ValueLevel.CURRENT;
import static com.github.sftwnd.crayfish.common.state.DefaultsHolder.ValueLevel.DEFAULT;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
class TemporalFormatterTest {

    private static final ZoneId zoneIdNovosib = ZoneId.of("Asia/Novosibirsk");
    private static final ZoneId zoneIdKgrad = ZoneId.of("Europe/Kaliningrad");
    private static final ZonedDateTime now = ZonedDateTime.now();
    private static final DateTimeFormatter coreDateTimeFormatter = DEFAULT_FORMATTER.withZone(DEFAUT_ZONE_ID);
    private static final DateTimeFormatter dateTimeFormatter = ISO_OFFSET_DATE_TIME.withZone(zoneIdNovosib);
    private static final DateTimeFormatter dabeDateTimeFormatter = DEFAULT_FORMATTER.withZone(DEFAUT_ZONE_ID);

    @Test
    void testTemporalFormatter() throws NoSuchFieldException, IllegalAccessException {
        TemporalFormatter formatter = new TemporalFormatter();
        Field fieldefaults = TemporalFormatter.class.getDeclaredField("defaults");
        fieldefaults.setAccessible(true);
        DefaultsHolder<DateTimeFormatter> defaults = (DefaultsHolder<DateTimeFormatter>)fieldefaults.get(formatter);
        AtomicReference<String> name = new AtomicReference<>("Default");
        IntStream.range(0, 2).forEach( i-> {
            assertEquals(coreDateTimeFormatter.format(now),
                    formatter.format(now),
                    name.get()+" date formatter have got to produce same result as DEFAULT_FORMATTER with DEFAUT_ZONE_ID");
            defaults.clearDefaultValue();
            name.set("System");
        });
    }

    @Test
    void testTemporalFormatterDateTimeormatter() throws NoSuchFieldException, IllegalAccessException {
        TemporalFormatter formatter = new TemporalFormatter(dateTimeFormatter);
        Field fieldefaults = TemporalFormatter.class.getDeclaredField("defaults");
        fieldefaults.setAccessible(true);
        DefaultsHolder<DateTimeFormatter> defaults = (DefaultsHolder<DateTimeFormatter>)fieldefaults.get(formatter);
        assertEquals(dateTimeFormatter.format(now), formatter.format(now),
                "Default date formatter have got to produce same result as dateTimeFormatter in constructor");
        defaults.clearDefaultValue();
        assertEquals(coreDateTimeFormatter.format(now), formatter.format(now),
                "Core date formatter have got to produce same result as DEFAULT_FORMATTER with DEFAUT_ZONE_ID");

    }

    @Test
    void testTemporalFormatterZoneDateTime() throws NoSuchFieldException, IllegalAccessException {
        TemporalFormatter formatter = new TemporalFormatter(zoneIdNovosib);
        Field fieldefaults = TemporalFormatter.class.getDeclaredField("defaults");
        fieldefaults.setAccessible(true);
        DefaultsHolder<DateTimeFormatter> defaults = (DefaultsHolder<DateTimeFormatter>)fieldefaults.get(formatter);
        assertEquals(DEFAULT_FORMATTER.withZone(zoneIdNovosib).format(now),
                     formatter.format(now),
                "Default date formatter have got to produce same result as DEFAULT_FORMATTER with zoneId in constructor");
        defaults.clearDefaultValue();
        assertEquals(coreDateTimeFormatter.format(now), formatter.format(now),
                "Core date formatter have got to produce same result as DEFAULT_FORMATTER with DEFAUT_ZONE_ID");
    }

    @Test
    void testTemporalFormatterFormatters() throws NoSuchFieldException, IllegalAccessException {
        DateTimeFormatter baseFormatter = dateTimeFormatter.withZone(zoneIdNovosib);
        DateTimeFormatter defaultFormatter = dateTimeFormatter.withZone(zoneIdNovosib);
        TemporalFormatter formatter = new TemporalFormatter(baseFormatter, defaultFormatter);
        Field fieldefaults = TemporalFormatter.class.getDeclaredField("defaults");
        fieldefaults.setAccessible(true);
        DefaultsHolder<DateTimeFormatter> defaults = (DefaultsHolder<DateTimeFormatter>)fieldefaults.get(formatter);
        assertEquals(defaultFormatter.format(now),
                formatter.format(now),
                "Default date formatter have got to produce same result as formatter in constructor");
        defaults.clearDefaultValue();
        assertEquals(baseFormatter.format(now), formatter.format(now),
                "Default date formatter have got to produce same result as baseFormatter in constructor");
        baseFormatter = ISO_OFFSET_DATE_TIME.withZone(null);
        defaultFormatter = ISO_ZONED_DATE_TIME.withZone(null);
        formatter = new TemporalFormatter(baseFormatter, defaultFormatter);
        defaults = (DefaultsHolder<DateTimeFormatter>)fieldefaults.get(formatter);
        assertEquals(defaultFormatter.withZone(DEFAUT_ZONE_ID).format(now),
                formatter.format(now),
                "Default date formatter have got to produce same result as formatter in constructor");
        defaults.clearDefaultValue();
        assertEquals(baseFormatter.format(now),
                formatter.format(now),
                "Default date formatter have got to produce same result as baseFormatter in constructor");
    }

    @Test
    public void testFormat() {
        DateTimeFormatter dateTimeFormatter = ISO_ZONED_DATE_TIME.withZone(zoneIdKgrad);
        TemporalAccessor temporalAccessor = ZonedDateTime.now();
        TemporalFormatter formatter = new TemporalFormatter(dateTimeFormatter);
        assertEquals(
                dateTimeFormatter.format(temporalAccessor),
                formatter.format(temporalAccessor),
                "TemporalFormatter.format result has to be equale DateTimeFormatter.format result"
        );
    }

    @Test
    public void testFormatZoneId() {
        DateTimeFormatter dateTimeFormatter = ISO_ZONED_DATE_TIME.withZone(zoneIdKgrad);
        TemporalAccessor temporalAccessor = ZonedDateTime.now();
        TemporalFormatter formatter = new TemporalFormatter(dateTimeFormatter);
        ZoneId otherZoneId = zoneIdNovosib;
        formatter.clearCurrentZoneId();
        assertNull(
                formatter.format(null, otherZoneId),
                "TemporalFormatter.format(null, zoneId) result has to be null"
        );
        assertEquals(
                dateTimeFormatter.withZone(otherZoneId).format(temporalAccessor),
                formatter.format(temporalAccessor, otherZoneId),
                "TemporalFormatter.format(..., zoneId) result has to be equale DateTimeFormatter.zoneId.format result"
        );
        assertEquals(
                DEFAULT,
                formatter.getValueLevel(),
                "ValueLevel has to be DEFAULT after TemporalFormatter.format(..., zoneId)"
        );
        ZoneId currentZoneId = zoneIdKgrad;
        formatter.setCurrentZoneId(currentZoneId);
        assertEquals(
                dateTimeFormatter.withZone(otherZoneId).format(temporalAccessor),
                formatter.format(temporalAccessor, otherZoneId),
                "TemporalFormatter.format(..., zoneId=null) result has to be equale DateTimeFormatter.format(currentZoneId) result after change currentZoneId"
        );
        assertEquals(
                CURRENT,
                formatter.getValueLevel(),
                "ValueLevel has to be CURRENT after TemporalFormatter.format(..., zoneId)"
        );
    }

    @Test
    public void testClearDefaultZoneId() {
        DateTimeFormatter noZoneFormatter = ISO_ZONED_DATE_TIME.withZone(DEFAUT_ZONE_ID);
        DateTimeFormatter defaultFormatter = noZoneFormatter.withZone(zoneIdNovosib);
        TemporalFormatter formatter = new TemporalFormatter(defaultFormatter);
        assertEquals(
                defaultFormatter.format(now),
                formatter.format(now),
                "TemporalFormatter.format(...) result has to be equals defaultFormatter.format result"
        );
        formatter.clearDefaultZoneId();
        assertNotEquals(
                defaultFormatter.format(now),
                formatter.format(now),
                "TemporalFormatter.format(...) result has not got to be equals defaultFormatter.format result after clearDefaultZpne"
        );
        assertNotEquals(
                defaultFormatter.format(now),
                formatter.format(now, DEFAUT_ZONE_ID),
                "TemporalFormatter.format(...) result has to be equals defaultFormatter.format)DEFAULT_ZONE_ID) result after clearDefaultZpne"
        );
    }

    @Test
    public void testSetDefaultZoneId() {
        DateTimeFormatter defaultFormatter = ISO_ZONED_DATE_TIME.withZone(zoneIdNovosib);
        TemporalFormatter formatter = new TemporalFormatter(defaultFormatter);
        ZoneId newDefaultZoneId = zoneIdKgrad;
        formatter.setDefaultZoneId(newDefaultZoneId);
        assertEquals(
                defaultFormatter.withZone(newDefaultZoneId).format(now),
                formatter.format(now),
                "TemporalFormatter.format(...) result has to be equals defaultFormatter.zone(defaultZone).format result"
        );
    }

    @Test
    void testRegisterFormatterUnregister() {
        TemporalFormatter formatter = new TemporalFormatter();
        Object obj = new Object();
        TemporalFormatter.register(obj, () -> formatter);
        try {
            assertSame(formatter, TemporalFormatter.formatter(obj), "TemporalFormatter::formatter(obj) has return registered formatter");
        } finally {
            TemporalFormatter.unregister(obj);
        }
        assertNull(TemporalFormatter.formatter(obj), "TemporalFormatter::formatter(obj) has return null after unregister formatter");

    }
}