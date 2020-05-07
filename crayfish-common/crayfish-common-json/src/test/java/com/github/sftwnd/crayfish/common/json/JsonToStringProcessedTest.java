package com.github.sftwnd.crayfish.common.json;

import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class JsonToStringProcessedTest {

    private static String apply(JsonToStringProcessed obj) {
        return Optional.ofNullable(obj).map(Object::toString).orElse(null);
    }

    private static String applyObj(JsonToStringProcessed obj) {
        return JsonToStringProcessed.toString(obj);
    }

    @SneakyThrows
    private void check(String process, Function<JsonToStringProcessed, String> serializer) {
        JsonToStringProcessed obj = new JsonToStringProcessedTestObject(new Random().nextLong());
        assertEquals(JsonMapper.serializeObject(obj), serializer.apply(obj), "Wrong value for "+process);
        assertEquals(null, serializer.apply(null), "Wrong value for "+process+" with null");
    }

    @Test
    void testToString() throws IOException {
        check("JsonToStringProcessed::toString()", JsonToStringProcessedTest::apply);
    }

    @Test
    void testToStringObj() {
        check("JsonToStringProcessed::toString()", JsonToStringProcessedTest::applyObj);
    }

    @Test
    void testToStringObjSneakyThrow() {
        Object obj = new JsonToStringProcessedTestObject() {
            @Override
            public Long getValue() {
                throw new NullPointerException();
            }
        };
        assertThrows(JsonMappingException.class, () -> JsonToStringProcessed.toString(obj), "JsonToStringProcessed.toString(obj) has to throw JsonMappingException");
    }

    @AllArgsConstructor
    @NoArgsConstructor
    static class JsonToStringProcessedTestObject extends JsonToStringProcessed {
        @Getter
        @Setter
        Long value;
    }
}