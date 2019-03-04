package com.github.sftwnd.crayfish.common.time;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeUnitUtlTest {

    private static final Locale defaultLocale = Locale.getDefault();

    private static final Map<String, TimeUnit> mapTime = Map.of(  "мк", TimeUnit.MICROSECONDS
                                                                ,"миллисекунд", TimeUnit.MILLISECONDS
                                                                ,"сек", TimeUnit.SECONDS
                                                                ,"мин", TimeUnit.MINUTES
                                                                ,"часа", TimeUnit.HOURS
                                                                ,"сутки", TimeUnit.DAYS );

    private static final Map<String, ChronoUnit> mapChrono = Map.of(  "мк", ChronoUnit.MICROS
                                                                ,"половинадня", ChronoUnit.HALF_DAYS
                                                                ,"неделя", ChronoUnit.WEEKS
                                                                ,"месяцев", ChronoUnit.MONTHS
                                                                ,"лет", ChronoUnit.YEARS
                                                                ,"cent", ChronoUnit.CENTURIES
                                                                ,"mill", ChronoUnit.MILLENNIA
                                                                ,"era", ChronoUnit.ERAS
                                                                ,"forever", ChronoUnit.FOREVER );

    @Before
    public void startUp() {
        Locale.setDefault(Locale.forLanguageTag("ru"));
    }

    @After
    public void tearDown() {
        Locale.setDefault(defaultLocale);
    }

    @Test
    void getChronoUnit() {
        mapChrono.forEach(
                (k, v) -> assertEquals(v, TimeUnitUtl.getChronoUnit(k))
        );
    }

    @Test
    void getChronoUnitUnknownDefault() {
        assertEquals(ChronoUnit.FOREVER, TimeUnitUtl.getChronoUnit("n/a", ChronoUnit.FOREVER));
    }

    @Test
    void getTimeUnit() {
        mapTime.forEach(
                (k, v) -> assertEquals(v, TimeUnitUtl.getTimeUnit(k))
        );
    }

    @Test
    void getTimeUnitUnknownDefault() {
        assertEquals(TimeUnit.NANOSECONDS, TimeUnitUtl.getTimeUnit("n/a", TimeUnit.NANOSECONDS));
    }

    @Test
    void getPeriodWithDefaultChronoUnit() {
        assertEquals(Period.ofDays(2), TimeUnitUtl.getPeriod("2", ChronoUnit.DAYS));
        assertEquals(Period.ofWeeks(3), TimeUnitUtl.getPeriod("3", ChronoUnit.WEEKS));
        assertEquals(Period.ofMonths(4), TimeUnitUtl.getPeriod("4", ChronoUnit.MONTHS));
        assertEquals(Period.ofYears(5), TimeUnitUtl.getPeriod("5", ChronoUnit.YEARS));
    }

    @Test
    void getPeriod() {
        assertEquals(Period.ofDays(1), TimeUnitUtl.getPeriod("день"));
        assertEquals(Period.ofDays(2), TimeUnitUtl.getPeriod("2 дня"));
        assertEquals(Period.ofWeeks(3), TimeUnitUtl.getPeriod("3 недели"));
        assertEquals(Period.ofMonths(4), TimeUnitUtl.getPeriod("4 мес"));
        assertEquals(Period.ofYears(5), TimeUnitUtl.getPeriod("5 лет"));
        assertEquals(Period.ofYears(1), TimeUnitUtl.getPeriod("год"));
        assertEquals(Period.ofDays(1), TimeUnitUtl.getPeriod("25 часов"));
    }

    @Test
    void getDurationMsec() {
        assertEquals(Duration.of(10, ChronoUnit.SECONDS), TimeUnitUtl.getDuration("10000"));
    }

    @Test
    void getDurationDefault() {
        assertEquals(Duration.of(300, ChronoUnit.SECONDS), TimeUnitUtl.getDuration("5", ChronoUnit.MINUTES));
    }

    @Test
    void getDuration() {
        assertEquals(Duration.of(2, ChronoUnit.HOURS), TimeUnitUtl.getDuration("2 часа"));
        assertEquals(Duration.ofDays(1), TimeUnitUtl.getDuration("день"));
        assertEquals(Duration.ofMillis(1), TimeUnitUtl.getDuration("миллисекунда"));
    }

    @Test
    void getPeriodWrong() {
        assertThrows(
                InvalidParameterException.class,
                () -> TimeUnitUtl.getPeriod("1 час")
        );
    }

}