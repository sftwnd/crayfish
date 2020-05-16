package com.github.sftwnd.crayfish.common.json.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.sftwnd.crayfish.common.format.formatter.TemporalFormatter;
import com.github.sftwnd.crayfish.common.state.StateHolder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.TimeZone;

import static com.github.sftwnd.crayfish.common.exception.ExceptionUtils.uncheckExceptions;
import static com.github.sftwnd.crayfish.common.state.DefaultsHolder.ValueLevel.CURRENT;

@Slf4j
public class JsonZonedSerializer<T> extends JsonSerializer<T> {

    public JsonZonedSerializer() {
        super();
    }

    /**
     * Serialize ZoneDateTime to String
     *
     * @param dateTime Serialized data with timezone
     * @param gen Json Generator
     * @param provider Serializer Provider
     * @throws IOException
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
                                        .map(zoneId -> StateHolder.supply(
                                                zoneId,
                                                formatter::getZoneId,
                                                formatter::setCurrentZoneId,
                                                formatter.getValueLevel() == CURRENT ? formatter::setCurrentZoneId : z -> formatter.clearCurrentZoneId(),
                                                () -> formatter.format(temporal)
                                        ))
                                        .orElseGet(() -> formatter.format(temporal))
                        )
                    .orElse(null)
        );
    }

    protected TemporalAccessor temporalValue(T dateTime) {
        return dateTime == null ? null
             : dateTime instanceof TemporalAccessor ? TemporalAccessor.class.cast(dateTime)
             : uncheckExceptions(new IllegalArgumentException("JsonZonedSerializer::temporalValue(dateTime) - wrong dataTime argument type: "+dateTime.getClass().getCanonicalName()));
    }

    protected TemporalFormatter constructSerializer() {
        return new TemporalFormatter();
    }

}
