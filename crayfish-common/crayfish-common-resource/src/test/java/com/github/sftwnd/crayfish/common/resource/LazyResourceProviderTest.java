package com.github.sftwnd.crayfish.common.resource;

import com.github.sftwnd.crayfish.common.exception.ExceptionUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LazyResourceProviderTest {

    @Test
    void constructTest() {
        LazyResourceProvider<Object, Object> provider =getProvider();
        assertEquals(obj, provider.construct());
    }

    @Test
    void provideTest() {
        LazyResourceProvider<Object, Object> provider =getProvider();
        assertSame(obj, provider.provide());
    }

    @Test
    void provideClosedTest() {
        LazyResourceProvider<Object, Object> provider = getProvider();
        provider.provide();
        provider.close();
        assertNull(provider.provide());
    }

    @Test
    void isProvidedTest() {
        LazyResourceProvider<Object, Object> provider =getProvider();
        assertFalse(provider.isProvided());
        provider.provide();
        assertTrue(provider.isProvided());
        provider.close();
        assertFalse(provider.isProvided());
    }

    @Test
    void clearAbsorbedThrowsTest() {
        LazyResourceProvider<Object, Object> provider = getProvider();
        assertFalse(Optional.ofNullable(provider.getAbsorbedThrows()).orElse(Collections.emptySet()).isEmpty());
        provider.clearAbsorbedThrows();
        assertTrue(Optional.ofNullable(provider.getAbsorbedThrows()).orElse(Collections.emptySet()).isEmpty());
    }

    @Test
    void resetAbsorbedThrowsTest() {
        LazyResourceProvider<Object, Object> provider = getProvider();
        assertEquals(1, Optional.ofNullable(provider.getAbsorbedThrows()).orElse(Collections.emptySet()).size());
        provider.addAbsorbedThrow(throwables[1]);
        assertEquals(2, Optional.ofNullable(provider.getAbsorbedThrows()).orElse(Collections.emptySet()).size());
        provider.resetAbsorbedThrows();
        assertEquals(1, Optional.ofNullable(provider.getAbsorbedThrows()).orElse(Collections.emptySet()).size());
    }

    @Test
    void getAbsorbedThrowsTest() {
        LazyResourceProvider<Object, Object> provider = getProvider();
        assertEquals(Stream.of(throwable).collect(Collectors.toSet()), provider.getAbsorbedThrows());
    }

    @Test
    void addAbsorbedThrowTest() {
        LazyResourceProvider<Object, Object> provider = getProvider();
        assertEquals(1, Optional.ofNullable(provider.getAbsorbedThrows()).orElse(Collections.emptySet()).size());
        provider.addAbsorbedThrow(throwables[1]);
        assertEquals(Stream.of(throwables).collect(Collectors.toSet()), provider.getAbsorbedThrows());
    }

    @Test
    void removeAbsorbedThrowTest() {
        LazyResourceProvider<Object, Object> provider = getProvider(throwables);
        assertEquals(2, Optional.ofNullable(provider.getAbsorbedThrows()).orElse(Collections.emptySet()).size());
        provider.removeAbsorbedThrow(throwables[1]);
        assertEquals(Stream.of(throwable).collect(Collectors.toSet()), provider.getAbsorbedThrows());
    }

    @Test
    void setOnCloseTest() {
        AtomicBoolean result = new AtomicBoolean(false);
        try (LazyResourceProvider<Object, Object> provider = getProvider()) {
            provider.setOnClose((p, r) -> result.set(Optional.ofNullable(p).orElse(new Object()).equals(r)));
            provider.provide();
        }
        assertTrue(result.get());
    }

    @Test
    void closeTest() {
        LazyResourceProvider<Object, Object> provider = getProvider();
        provider.provide();
        assertTrue(provider.isProvided());
        provider.close();
        assertFalse(provider.isProvided());
    }

    @Test
    void resetTest() {
        LazyResourceProvider<Object, Object> provider = getProvider();
        provider.provide();
        provider.close();
        provider.reset();
        assertSame(obj, provider.provide());
    }

    @Test
    public void absorbedThrowsTest() {
        LazyResourceProvider<Object, Object> provider = new LazyResourceProvider<>(() -> obj, o -> rethrow(new NumberFormatException("Something wrong!!!")), Stream.of(throwables).collect(Collectors.toSet()));
        assertNull(provider.provide());
        provider.removeAbsorbedThrow(NumberFormatException.class);
        assertThrows( NumberFormatException.class, () -> provider.provide());
    }

    @SneakyThrows
    private static final Object rethrow(Throwable throwable) {
        throw throwable;
    }

    private static final Object obj = Long.valueOf(Math.round(Math.random()*100000L));
    Class<? extends Throwable>[] throwables = new Class[]{IOException.class, NumberFormatException.class};
    Class<? extends Throwable>[] throwable  = new Class[]{throwables[0]};

    private LazyResourceProvider<Object, Object> getProvider() {
        return getProvider(throwable);
    }

    private LazyResourceProvider<Object, Object> getProvider(Class<? extends Throwable>[] thrws) {
        return new LazyResourceProvider<>(() -> obj, o -> o, Stream.of(thrws == null ? throwable : thrws).collect(Collectors.toSet()));
    }

}