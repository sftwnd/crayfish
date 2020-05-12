/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.state;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StateTest {

    @Test
    void testState() {
        Integer value = new Random().nextInt(10000);
        @SuppressWarnings("unchecked")
        Supplier<Integer> getter = mock(Supplier.class);
        @SuppressWarnings("unchecked")
        Consumer<Integer> setter = mock(Consumer.class);
        for(Integer newValue:new Integer[]{value + new Random().nextInt(10000) + 1, null}){
            Mockito.reset(getter, setter);
            when(getter.get()).thenReturn(value);
            State<Integer> state = new State<>(newValue, getter, setter);
            verify(getter, atLeastOnce()).get();
            verify(setter, times(1)).accept(newValue);
        }
        assertThrows(NullPointerException.class, () -> new State<>(null, null, setter), "State::new(setter=null) has to throws NullPointerException");
        assertThrows(NullPointerException.class, () -> new State<>(null, getter, null), "State::new(getter=null) has to throws NullPointerException");
        AtomicReference<Integer> obj = new AtomicReference<>(value);
        Integer newValue = value + 17;
        State<Integer> state = new State<>(newValue, obj::get, obj::set);
        assertEquals(newValue, obj.get(),"State::new has to change state value on call");
    }

    @Test
    void testStateSameValue() {
        Integer value = new Random().nextInt(10000);
        Integer newValue = value;
        @SuppressWarnings("unchecked")
        Supplier<Integer> getter = mock(Supplier.class);
        @SuppressWarnings("unchecked")
        Consumer<Integer> setter = mock(Consumer.class);
        when(getter.get()).thenReturn(value);
        State<Integer> state = new State<>(newValue, getter, setter);
        verify(getter, atLeastOnce()).get();
        verify(setter, never()).accept(newValue);
        reset(getter, setter);
    }

    @Test
    void testStateEqualsValue() {
        Integer value = new Random().nextInt(10000);
        Integer newValue = value;
        @SuppressWarnings("unchecked")
        Supplier<Integer> getter = mock(Supplier.class);
        @SuppressWarnings("unchecked")
        Consumer<Integer> setter = mock(Consumer.class);
        when(getter.get()).thenReturn(Integer.valueOf(value.intValue()));
        new State<>(newValue, getter, setter);
        verify(getter, atLeastOnce()).get();
        verify(setter, never()).accept(newValue);
    }

    @Test
    @SuppressWarnings("try")
    void testClose() throws Exception {
        for (Integer value : new Integer[] {-1, new Random().nextInt(10000), null}) {
            for (Integer newValue : new Integer[] {value == null ? 0 : value + 13, null, value}) {
                AtomicReference<Integer> obj = new AtomicReference<>(value);
                try(AutoCloseable x = new State<>(newValue, obj::get, obj::set)) {
                }
                assertEquals(value, obj.get(), "Value has to be restored after autoclose State");
            }
        }
    }

    @Test
    @SuppressWarnings("try")
    void testConstruct() throws Exception {
        for (Integer value : new Integer[] {-1, new Random().nextInt(10000), null}) {
            for (Integer newValue : new Integer[] {value == null ? 0 : value + 13, null, value}) {
                AtomicReference<Integer> obj = new AtomicReference<>(value);
                try(AutoCloseable x = State.construct(newValue, obj::get, obj::set)) {
                }
                assertEquals(value, obj.get(), "Value has to be restored after autoclose State");
            }
        }
    }

}