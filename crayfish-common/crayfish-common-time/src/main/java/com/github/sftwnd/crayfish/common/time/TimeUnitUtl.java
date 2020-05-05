package com.github.sftwnd.crayfish.common.time;

import com.github.sftwnd.crayfish.common.base.Pair;
import com.github.sftwnd.crayfish.common.i18n.I18n;
import com.github.sftwnd.crayfish.common.i18n.MessageSource;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.CENTURIES;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.DECADES;
import static java.time.temporal.ChronoUnit.ERAS;
import static java.time.temporal.ChronoUnit.FOREVER;
import static java.time.temporal.ChronoUnit.HALF_DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MICROS;
import static java.time.temporal.ChronoUnit.MILLENNIA;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.ChronoUnit.YEARS;

@Slf4j
public class TimeUnitUtl {

    private static final String INVALID_PARAMETER_EXCEPTION = "crayfish-common-time.invalidParameterException";
    private static final String VALUE_WILL_BE_ROUNDED = "crayfish-common-time.log.valueWillBeRounded";
    private static final MessageSource timeUnits = I18n.getMessageSource(TimeUnitUtl.class, "timeunits");
    private static final MessageSource messageSource = I18n.getMessageSource(TimeUnitUtl.class, "messages");

    private TimeUnitUtl() {
        throw new IllegalStateException("TimeUnitUtl is utility class");
    }

    private static final Map<String, ChronoUnit> unitNamesMap = Stream.of(
               MICROS, MILLIS, SECONDS, MINUTES, HOURS, HALF_DAYS, DAYS, WEEKS, MONTHS, YEARS, DECADES, CENTURIES, MILLENNIA, ERAS, FOREVER
             )
            .flatMap(
                    cu -> {
                        String nm = cu.name().toLowerCase().replace("_","");
                        return Stream.of(Locale.getDefault(), Locale.forLanguageTag(""), null)
                                .flatMap(
                                        l -> Stream.of(
                                                ( l == null ? cu.name().toLowerCase().replace("_","")
                                                        : timeUnits.messageDef(l, nm, nm)
                                                ).split(",")
                                        )
                                ).distinct()
                                .collect(Collectors.toMap(n -> n, n -> cu)).entrySet().stream();
                    }
            ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    public static final ChronoUnit getChronoUnit(@Nonnull String unitName) {
        return getChronoUnit(unitName, null);
    }

    public static final ChronoUnit getChronoUnit(@Nonnull String unitName, ChronoUnit defaultUnit) {
        Objects.requireNonNull(unitName);
        return unitNamesMap.containsKey(unitName) ? unitNamesMap.get(unitName) : defaultUnit;
    }

    public static final TimeUnit getTimeUnit(@Nonnull String unitName) {
        return getTimeUnit(unitName, null);
    }

    public static final TimeUnit getTimeUnit(@Nonnull String unitName, TimeUnit defaultUnit) {
        Objects.requireNonNull(unitName);
        ChronoUnit chronoUnit = getChronoUnit(unitName);
        return chronoUnit == null ? defaultUnit : TimeUnit.of(chronoUnit);
    }

    @SuppressWarnings("squid:S4784")
    private static final Pattern pattern = Pattern.compile("^(\\d+)?\\s*(.*)$");

    public static final Period getPeriod(@Nonnull String str) {
        return getPeriod(str, ChronoUnit.DAYS);
    }

    public static final Period getPeriod(@Nonnull String str, ChronoUnit defaultChronoUnit) {
        Pair<Long, ChronoUnit> pair = parse(str, defaultChronoUnit);
        if (Objects.requireNonNullElse(pair.getKey(), 0L).intValue() < 1) {
            throw new InvalidParameterException(messageSource.message(INVALID_PARAMETER_EXCEPTION, str, pair.getKey()));
        }
        Duration duration;
        switch (pair.getValue()) {
            case YEARS: return Period.ofYears(pair.getKey().intValue());
            case MONTHS: return Period.ofMonths(pair.getKey().intValue());
            case WEEKS: return Period.ofWeeks(pair.getKey().intValue());
            case DAYS: return Period.ofDays(pair.getKey().intValue());
            case FOREVER: return Period.ofDays((int)FOREVER.getDuration().toDays());
            default: duration = Duration.of(pair.getKey(), pair.getValue());
        }
        if (duration.toDays() < 1) {
            throw new InvalidParameterException(messageSource.message(INVALID_PARAMETER_EXCEPTION, str, duration));
        } else {
            BigDecimal[] divided = BigDecimal.valueOf(duration.toMillis()).divideAndRemainder(BigDecimal.valueOf(Duration.ofDays(1).toMillis()));
            if (divided[1].intValue() > 0) {
                logger.warn(messageSource.message(VALUE_WILL_BE_ROUNDED));
            }
            return Period.ofDays(divided[0].intValue());
        }
    }

    private static final Pair<Long, ChronoUnit> parse(@Nonnull String str, @Nonnull ChronoUnit defaultChronoUnit) {
        Objects.nonNull(str);
        Objects.nonNull(defaultChronoUnit);
        Matcher matcher = pattern.matcher(str);
        Pair<Long, ChronoUnit> result = matcher.matches()
                ? Pair.of(
                      Long.valueOf(Objects.toString(matcher.group(1), "1"))
                     ,getChronoUnit(matcher.group(2), defaultChronoUnit)
                  )
                : Pair.of(Long.valueOf(Objects.toString(str, "1")), defaultChronoUnit);
        Objects.requireNonNull(result.getKey());
        Objects.requireNonNull(result.getValue());
        return result;
    }

    public static final Duration getDuration(@Nonnull String str) {
        return getDuration(str, ChronoUnit.MILLIS);
    }

    public static final Duration getDuration(@Nonnull String str, ChronoUnit defaultChronoUnit) {
        Pair<Long, ChronoUnit> pair = parse(str, defaultChronoUnit);
        return pair.getValue().getDuration().multipliedBy(pair.getKey());
    }

}
