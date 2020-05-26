package com.github.sftwnd.crayfish.common.resource;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.sftwnd.crayfish.common.exception.ExceptionUtils.uncheckExceptions;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LazyResourceProviderTest {

    @Test
    @SuppressWarnings("unchecked")
    void testLazyResourceProvider() {
        LazyResourceProvider<AtomicReference<Object>, Object> provider = new LazyResourceProvider<>(
                () -> new AtomicReference<>(),
                r -> Optional.ofNullable(r.get())
                             .orElseGet(() -> { r.set(new Object()); return r.get(); } )
        );
        AtomicReference<Object> ref = provider.construct();
        assertNotNull(ref, "LazyResourceProvider::construct has to return not null constructor");
        assertSame(ref, provider.construct(), "LazyResourceProvider::construct has to return same constructor");
        Object obj = provider.provide();
        assertNotNull(obj, "LazyResourceProvider::provide has to return not null resource");
        assertSame(obj, provider.provide(), "LazyResourceProvider::provide has to return same resource");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testLazyResourceProviderWithThrowSet() throws IOException {
        lazy = new LazyResourceProvider<>(
                constructor, provider, Stream.of(IOException.class).collect(Collectors.toSet())
        );
        when(constructor.construct()).thenReturn(obj);
        when(provider.provide(any())).thenThrow(new IOException());
        assertDoesNotThrow(() -> lazy.provide(), "Declared throw has to be hided");
        lazy.clearAbsorbedThrows();
        assertThrows(IOException.class, () -> lazy.provide(), "Undeclared throw has to be raised");
        LazyResourceProvider<Object, Object> lazy1 = new LazyResourceProvider<>(
                constructor, provider, Collections.emptySet()
        );
        assertEquals(0, Optional.ofNullable(lazy1.getAbsorbedThrows()).map(Set::size).orElse(0), "LazyResourceProvider::new with empty throws set returns set with 0 elements");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testDestroy() throws IOException {
        when(constructor.construct()).thenReturn(obj);
        when(provider.provide(any())).thenReturn(obj);
        assertFalse(lazy.isDestroyed(), "LazyResourceProvider::isDestroyed has to return false for new object");
        lazy.destroy();
        assertNull(lazy.construct(), "LazyResourceProvider::construct has to return null for destroyed object");
        assertNull(lazy.provide(), "LazyResourceProvider::provide has to return null for destroyed object");
        assertTrue(lazy.isDestroyed(), "LazyResourceProvider::isDestroyed has to return true for destroyed object");
        lazy.reset();
        assertFalse(lazy.isDestroyed(), "LazyResourceProvider::isDestroyed has to return false for the reseted object after the destroy operation");
        assertNotNull(lazy.construct(), "LazyResourceProvider::construct has to return real object for resetted object");
        assertNotNull(lazy.provide(), "LazyResourceProvider::provide has to return real object for resetted object");
        lazy.setOnDestroy( (r, p) -> { throw new RuntimeException(); });
        assertThrows(RuntimeException.class, () -> lazy.destroy(), "LazyResourceProvider::destroy has throw exception provided by onDestroy call");
        BiConsumer<Object, Object> onDestroy = mock(BiConsumer.class);
        lazy.setOnDestroy(onDestroy);
        lazy.destroy();
        verify(onDestroy, times(0)).accept(any(), any());
        lazy.reset();
        lazy.destroy();
        verify(onDestroy, times(1)).accept(any(), any());
    }

    @Test
    void testIsProvided() throws IOException {
        when(constructor.construct()).thenReturn(obj);
        when(provider.provide(any())).thenReturn(obj);
        assertFalse(lazy.isProvided(), "LazyResourceProvider::isProvided has to return false for new object");
        lazy.construct();
        assertFalse(lazy.isProvided(), "LazyResourceProvider::isProvided has to return false for just constructed provider");
        lazy.provide();
        assertTrue(lazy.isProvided(), "LazyResourceProvider::isProvided has to return true after successfull provide");
    }

    @Test
    void testDoConstruct() throws IOException {
        when(constructor.construct()).thenReturn(obj);
        assertSame(obj, lazy.construct(), "LazyResourceProvider::construct has to return right object if doConstruct method generate it");
        lazy.reset();
        when(constructor.construct()).thenThrow(new IOException("oops..."));
        assertThrows(IOException.class, () -> lazy.construct(), "IOException has to be throws by doConstruct method in the case on throws this exception in constructor");
        lazy.reset();
        lazy.addAbsorbedThrow(IOException.class);
        assertNull(lazy.construct(), "LazyResourceProvider::construct has to return null if doConstruct method throws absorbable exception");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testDoProvide() throws IOException {
        when(constructor.construct()).thenReturn(obj);
        when(provider.provide(any())).thenReturn(obj);
        IntStream.range(0,2).forEach(
                i -> assertSame(obj, lazy.provide(), "LazyResourceProvider::provide has to return right object if doProvide method generate it")
        );
        assertSame(obj, lazy.provide(), "LazyResourceProvider::provide has to return right object if doProvide method generate it");
        lazy.reset();
        when(provider.provide(any())).thenThrow(new IOException(""));
        assertThrows(IOException.class, () -> lazy.provide(), "IOException has to be throws by doProvide method in the case on throws this exception in constructor");
        lazy.reset();
        lazy.addAbsorbedThrow(IOException.class);
        assertNull(lazy.provide(), "LazyResourceProvider::provide has to return null if doProvide method throws absorbable exception");
        lazy.removeAbsorbedThrow(IOException.class);
        reset(constructor);
        when(constructor.construct()).thenReturn(null);
        lazy.reset();
        assertNull(lazy.provide(), "LazyResourceProvider::provide has to return null if doConstruct method makes null provider");
    }

    @Test
    void testCheckResource() throws IOException {
        when(constructor.construct()).thenReturn(obj);
        when(provider.provide(any())).thenReturn(obj);
        lazy.setCheckResource(r -> true);
        assertSame(obj, lazy.provide(), "LazyResourceProvider::provide has to return right object on success checkResource");
        lazy.setCheckResource(r -> false);
        assertNull(lazy.provide(), "LazyResourceProvider::provide has to return null object on unsuccess checkResource");
        lazy.setCheckResource(r -> uncheckExceptions(new IOException()));
        assertThrows(IOException.class, lazy::provide, "LazyResourceProvider::provide has to throws when checkResource throws an exception");
    }

    @Test
    void testClose() throws IOException {
        when(constructor.construct()).thenReturn(obj);
        when(provider.provide(any())).thenReturn(obj);
        lazy.setCheckResource(r -> true);
        assertSame(obj, lazy.provide());
        assertTrue(lazy.isProvided(), "Unabcle to check close() operation - Provider is not provided");
        lazy.close();
        assertFalse(lazy.isProvided(), "Closed provider hasn't got to be provided");
        lazy.destroy();
        assertDoesNotThrow(() -> lazy.close(), "Closed resource unable to throw exception on destroy call");
    }

    @Test
    void testSetVetoableProvider() throws IOException, PropertyVetoException {
        when(constructor.construct()).thenReturn(obj);
        when(provider.provide(any())).thenReturn(obj);
        LazyVetoableResourceProvider<Object, Object> lazy = spy(new LazyVetoableResourceProvider<>(constructor, provider, VETO_ON_CONSTRUCT));
        assertThrows(PropertyVetoException.class, () -> lazy.construct(), "LazyVetoableResourceProvider(VETO_ON_CONSTRUCT)::construct has to throw exception ");
        assertThrows(PropertyVetoException.class, () -> lazy.provide(), "LazyVetoableResourceProvider(VETO_ON_CONSTRUCT)::provide has to throw exception ");
        verify(lazy, times(2)).providerChange(any(), any());
        verify(lazy, times(0)).resourceChange(any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSetVetoableReource() throws IOException, PropertyVetoException {
        when(constructor.construct()).thenReturn(obj);
        when(provider.provide(any())).thenReturn(obj);
        LazyVetoableResourceProvider<Object, Object> lazy = spy(new LazyVetoableResourceProvider<>(constructor, provider, VETO_ON_PROVIDE));
        assertDoesNotThrow(() -> lazy.construct(), "LazyVetoableResourceProvider(VETO_ON_PROVIDE)::construct has to be called successfully");
        assertThrows(PropertyVetoException.class, () -> lazy.provide(), "LazyVetoableResourceProvider(VETO_ON_PROVIDE)::provide has to throw exception ");
        verify(lazy, times(1)).providerChange(any(), any());
        verify(lazy, times(1)).resourceChange(any(), any());
        lazy.close();
        reset(lazy);
        setResourceValue(lazy, obj);
        assertDoesNotThrow(() -> lazy.provide(), "LazyVetoableResourceProvider(VETO_ON_PROVIDE)::provide hasn't got to throw exception if resource value is the same");
        verify(lazy, times(0)).resourceChange(any(), any());
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void init() {
        constructor = mock(LazyResourceProvider.Constructor.class);
        provider = mock(LazyResourceProvider.Provider.class);
        lazy = new LazyResourceProvider<>(constructor, provider);
        obj = new Object();
    }

    @AfterEach
    void complete() {
        constructor = null;
        provider = null;
        lazy = null;
        obj = null;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private <R> void setResourceValue(LazyResourceProvider<?, R> provider, R resource) {
        Field resourceField = LazyResourceProvider.class.getDeclaredField("resource");
        resourceField.setAccessible(true);
        resourceField.set(provider, resource);
    }

    private LazyResourceProvider.Constructor<Object> constructor;
    private LazyResourceProvider.Provider<Object, Object> provider;
    private LazyResourceProvider<Object, Object> lazy;
    private Object obj;

    private static final int VETO_ON_CONSTRUCT = 1;
    private static final int VETO_ON_PROVIDE = 2;

    private static class LazyVetoableResourceProvider<P, R> extends LazyResourceProvider<P, R> {

        @Getter @Setter private int veto;
        public LazyVetoableResourceProvider(LazyResourceProvider.Constructor<P> providerConstructor, LazyResourceProvider.Provider<P,R> resourceProvider, int veto) {
            super(providerConstructor, resourceProvider);
            this.veto = veto;
        }

        public void providerChange(P oldValue, P newValue) throws PropertyVetoException {
            if ( (this.veto & VETO_ON_CONSTRUCT) != 0) {
                throw new PropertyVetoException("Unable to call providerChange", null);
            }
        }

        public void resourceChange(R oldValue, R newValue) throws PropertyVetoException {
            if ( (this.veto & VETO_ON_PROVIDE) != 0) {
                throw new PropertyVetoException("Unable to call resourceChange", null);
            }
        }

    }

}