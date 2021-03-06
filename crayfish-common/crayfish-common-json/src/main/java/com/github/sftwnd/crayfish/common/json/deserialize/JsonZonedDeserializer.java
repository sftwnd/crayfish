package com.github.sftwnd.crayfish.common.json.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.github.sftwnd.crayfish.common.format.parser.TemporalParser;
import com.github.sftwnd.crayfish.common.state.StateHelper;

import java.io.IOException;
import java.util.Optional;
import java.util.TimeZone;

import static com.github.sftwnd.crayfish.common.state.DefaultsHolder.ValueLevel.CURRENT;

public abstract class JsonZonedDeserializer<T> extends JsonDeserializer<T> {

    public JsonZonedDeserializer() {
        super();
        TemporalParser.register(this.getClass(), this::constructParser);
    }

    public abstract TemporalParser<T> constructParser();

    /**
     * Deserialize JSON content into the value type this serializer handles.
     * Returned instance is to be constructed by method.
     * @param jsonParser Parsed used for reading JSON content
     * @param deserializationContext Context that can be used to access information about this deserialization activity.
     * @return Deserialized value
     * @throws IOException generated exception
     */
    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        @SuppressWarnings("unchecked")
        TemporalParser<T> parser = (TemporalParser<T>) TemporalParser.register(this.getClass(), this::constructParser);
        return Optional.ofNullable(jsonParser.getText())
                .map(txt -> Optional.ofNullable(deserializationContext.getTimeZone())
                                    .filter(tz -> parser.getValueLevel() != CURRENT)
                                    .map(TimeZone::toZoneId)
                                    .map(zoneId -> StateHelper.supply(
                                            zoneId,
                                            parser::getZoneId,
                                            parser::setCurrentZoneId,
                                            z -> parser.clearCurrentZoneId(),
                                            () -> parser.parse(txt)
                                    ))
                                    .orElseGet(() -> parser.parse(txt))
                )
                .orElse(null);
    }

}
