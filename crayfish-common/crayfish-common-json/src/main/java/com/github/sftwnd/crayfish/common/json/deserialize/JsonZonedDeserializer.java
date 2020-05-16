package com.github.sftwnd.crayfish.common.json.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.github.sftwnd.crayfish.common.format.parser.TemporalParser;
import com.github.sftwnd.crayfish.common.state.StateHolder;

import java.io.IOException;
import java.util.Optional;
import java.util.TimeZone;

import static com.github.sftwnd.crayfish.common.state.DefaultsHolder.ValueLevel.CURRENT;

public abstract class JsonZonedDeserializer<T> extends JsonDeserializer<T> {

    public JsonZonedDeserializer() {
        super();
    }

    public abstract TemporalParser<T> constructParser();

    /**
     * Deserialize T from String
     * @param jsonParser
     * @param deserializationContext
     * @return T
     * @throws IOException
     */
    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        @SuppressWarnings("unchecked")
        TemporalParser<T> parser = (TemporalParser<T>) TemporalParser.register(this.getClass(), this::constructParser);
        return Optional.ofNullable(jsonParser.getText())
                .map(txt -> Optional.ofNullable(deserializationContext.getTimeZone())
                                    .map(TimeZone::toZoneId)
                                    .map(zoneId -> StateHolder.supply(
                                            zoneId,
                                            parser::getZoneId,
                                            parser::setCurrentZoneId,
                                            parser.getValueLevel() == CURRENT ? parser::setCurrentZoneId : z -> parser.clearCurrentZoneId(),
                                            () -> parser.parse(txt)
                                    ))
                                    .orElseGet(() -> parser.parse(txt))
                )
                .orElse(null);
    }

}
