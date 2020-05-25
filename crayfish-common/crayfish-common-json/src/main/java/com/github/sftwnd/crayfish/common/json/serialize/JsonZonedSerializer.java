package com.github.sftwnd.crayfish.common.json.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.sftwnd.crayfish.common.format.formatter.TemporalFormatter;
import com.github.sftwnd.crayfish.common.state.StateHelper;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.TimeZone;

import static com.github.sftwnd.crayfish.common.state.DefaultsHolder.ValueLevel.CURRENT;

@Slf4j
public class JsonZonedSerializer<T> extends JsonSerializer<T> {

    public JsonZonedSerializer() {
        super();
        TemporalFormatter.register(this.getClass(), this::constructSerializer);
    }

    /**
     * Serialize ZoneDateTime to String
     *
     * @param dateTime Serialized data with timezone
     * @param gen Json Generator
     * @param provider Serializer Provider
     * @throws IOException throwed exception
     */
    @Override
    public void serialize(T dateTime, JsonGenerator gen, SerializerProvider provider) throws IOException {
        logger.trace("serialize(date:`{}`)", dateTime);
        TemporalFormatter formatter = TemporalFormatter.register(this.getClass(), this::constructSerializer);
        gen.writeString(
                Optional.ofNullable(dateTime)
                    .map(this::temporalValue)
                    .map(temporal -> Optional.ofNullable(provider.getTimeZone())
                                        .map(TimeZone::toZoneId)
                                        .filter(tz -> formatter.getValueLevel() != CURRENT)
                                        .map(zoneId -> StateHelper.supply(
                                                zoneId,
                                                formatter::getZoneId,
                                                formatter::setCurrentZoneId,
                                                zid -> formatter.clearCurrentZoneId(),
                                                () -> formatter.format(temporal)
                                        ))
                                        .orElseGet(() -> formatter.format(temporal))
                        )
                    .orElse(null)
        );
    }

    protected TemporalAccessor temporalValue(@Nonnull T dateTime) {
        if (dateTime instanceof TemporalAccessor) {
            return TemporalAccessor.class.cast(dateTime);
        }
        throw new IllegalArgumentException("JsonZonedSerializer::temporalValue(dateTime) - wrong dataTime argument type: "+dateTime.getClass().getCanonicalName());
    }

    protected @Nonnull TemporalFormatter constructSerializer() {
        return new TemporalFormatter();
    }

}
