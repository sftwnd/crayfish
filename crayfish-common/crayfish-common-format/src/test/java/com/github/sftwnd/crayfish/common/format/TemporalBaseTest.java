package com.github.sftwnd.crayfish.common.format;

import com.github.sftwnd.crayfish.common.format.formatter.TemporalFormatter;
import com.github.sftwnd.crayfish.common.format.parser.InstantParser;
import com.github.sftwnd.crayfish.common.format.parser.TemporalParser;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class TemporalBaseTest {

    @Test
    void testRegisterAndFormatterForRegisteredObject() {
        TemporalFormatter formatter = new TemporalFormatter();
        Object obj = new Object();
        try {
            TemporalFormatter.register(obj, () -> formatter);
            assertSame(formatter, TemporalFormatter.formatter(obj), "TemporalFormatter.formatter has to return real value for registered object");
        } finally {
            TemporalFormatter.unregister(obj);
        }
    }

    @Test
    void testUnregister() {
        Object obj = new Object();
        TemporalFormatter.register(obj, () -> new TemporalFormatter());
        TemporalFormatter.unregister(obj);
        assertNull(TemporalFormatter.formatter(obj), "TemporalFormatter.formatter has to be null for unregistred object");
    }

    @Test
    void testFormatterUnregisteredObject() {
        assertNull(TemporalFormatter.formatter(new Object()), "TemporalFormatter.formatter has to be null for unknown object");
    }

    @Test
    void testFormatterUnregisteredNonSelfRegisteredClass() {
        assertNull(TemporalFormatter.formatter(Object.class), "TemporalFormatter.formatter has to be null for non self-registrable class");
    }

    @Test
    void testFormatterUnregisteredSelfRegisteredClass() {
        Object formatter = TemporalFormatter.formatter(TemporalBaseTestSelfRegisteredObject.class);
        assertNotNull(formatter, "TemporalFormatter.formatter has to be null for non self-registrable class");
        assertEquals(
                TemporalFormatter.class,
                Optional.ofNullable(formatter).map(Object::getClass).orElse(null),
                "TemporalFormatter.formatter(class) has to return instance of self-registrable class from parameter"
        );
    }

    @Test
    void testFormatterUnregisteredWrongSelfRegisteredClass() {
        assertNull(
                TemporalFormatter.formatter(TemporalBaseTestWrongSelfRegisteredObject.class),
                "TemporalFormatter.formatter has to be null for non self-registrable class"
        );
    }

    @Test
    void testFormatterUnregisteredNoConstructorClass() {
        assertNull(
                TemporalParser.parser(TemporalBaseTestNoConstructorObject.class),
                "TemporalFormatter.formatter has to be null for class without simple constructor"
        );
    }

    @AfterEach
    void tearDown() {
        TemporalFormatter.unregister(TemporalBaseTestSelfRegisteredObject.class);
    }

    static class TemporalBaseTestSelfRegisteredObject {
        TemporalBaseTestSelfRegisteredObject() {
            TemporalFormatter.register(this.getClass(), () -> new TemporalFormatter());
        }
    }

    static class TemporalBaseTestWrongSelfRegisteredObject {
        @SneakyThrows
        TemporalBaseTestWrongSelfRegisteredObject() {
            throw new InstantiationException();
        }
    }

    static class TemporalBaseTestNoConstructorObject {
        TemporalBaseTestNoConstructorObject(TemporalFormatter obj) {
            TemporalFormatter.register(this.getClass(), () -> obj);
        }
    }

}