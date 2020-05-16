package com.github.sftwnd.crayfish.common.state;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultsHolderTest {

    private static Field systemViewField;
    private static Field defaultViewField;
    private static Field currentViewField;
    private static Field holderMapField;

    @BeforeAll
    static void startUp() throws NoSuchFieldException {
        systemViewField = DefaultsHolder.class.getDeclaredField("systemValue");
        systemViewField.setAccessible(true);
        defaultViewField = DefaultsHolder.class.getDeclaredField("defaultValue");
        defaultViewField.setAccessible(true);
        currentViewField = DefaultsHolder.class.getDeclaredField("currentValue");
        currentViewField.setAccessible(true);
        holderMapField = DefaultsHolder.class.getDeclaredField("holderMap");
        holderMapField.setAccessible(true);
    }

    @SuppressWarnings("unchecked")
    public <T> T systemValue(DefaultsHolder<T> holder) throws IllegalAccessException {
        return (T) Supplier.class.cast(systemViewField.get(holder)).get();
    }

    @SuppressWarnings("unchecked")
    public <T> T defaultValue(DefaultsHolder<T> holder) throws IllegalAccessException {
        return (T) Supplier.class.cast(defaultViewField.get(holder)).get();
    }

    @SuppressWarnings("unchecked")
    public Map<Object, DefaultsHolder<?>> holderMap() throws IllegalAccessException {
        return (Map<Object, DefaultsHolder<?>>)holderMapField.get(null);
    }

    @SuppressWarnings("unchecked")
    public <T> ThreadLocal<T> currentValue(DefaultsHolder<T> holder) throws IllegalAccessException {
        return (ThreadLocal<T>) Supplier.class.cast(currentViewField.get(holder)).get();
    }

    @Test
    void testDefaultsHolder() throws NoSuchFieldException, IllegalAccessException {
        DefaultsHolder<Object> holder = new DefaultsHolder<>();
        assertNotNull(systemViewField.get(holder), "DefaultsHolder.systemView has to be not null for DefaultsHolder::new");
        assertNull(systemValue(holder), "DefaultsHolder.systemView has to be not null for DefaultsHolder::new");
    }

    @Test
    void testDefaultsHolderNull() throws NoSuchFieldException, IllegalAccessException {
        DefaultsHolder<Object> holder = new DefaultsHolder<>(null);
        assertNotNull(systemViewField.get(holder), "DefaultsHolder.systemView has to be not null for DefaultsHolder::new(null)");
        assertNull(systemValue(holder), "DefaultsHolder.systemView has to be not null for DefaultsHolder::new(null)");
    }
    
    @Test
    void testDefaultsHolderWithInitializationSupplier() throws IllegalAccessException {
        Object obj = new Object();
        DefaultsHolder<Object> holder = new DefaultsHolder<>(() -> obj);
        assertSame(obj, holder.getDefaultValue(), "Default value for DefaultsHolder::new(() -> obj) has to be the same obj");
        assertSame(obj, holder.getCurrentValue(), "Current value for DefaultsHolder::new() has to be the same obj");
        assertSame(obj, systemValue(holder), "DefaultsHolder.systemView has to be not null");
    }
    
    @Test
    void testSetGetDefaultSupplier() throws IllegalAccessException {
        Object obj = new Object();
        Supplier<Object> supplier = () -> obj;
        DefaultsHolder<Object> holder = new DefaultsHolder<>(() -> obj);
        holder.setDefaultValue(supplier);
        assertSame(supplier, defaultViewField.get(holder), "Default::setDefaultValue must set right defaultValue");
        assertSame(obj, defaultValue(holder), "Default::setDefaultValue - defaultValue must supply right result");
        assertSame(obj, holder.getDefaultValue(), "Default value has to be the same obj as defaultSupplier return");
    }

    @Test
    void testSetGetDefaultValue() throws IllegalAccessException {
        Object obj = new Object();
        DefaultsHolder<Object> holder = new DefaultsHolder<>(() -> obj);
        holder.setDefaultValue(obj);
        assertSame(obj, defaultValue(holder), "Default::setDefaultValue - defaultValue must supply right result");
        assertSame(obj, holder.getDefaultValue(), "Default value has to be the same obj as defaultSupplier return");
    }

    @Test
    void testClearDefaultValue() throws IllegalAccessException {
        Object systemDefault = new Object();
        Object defaultObj = new Object();
        DefaultsHolder<Object> holder = new DefaultsHolder<>(() -> systemDefault);
        holder.setDefaultValue(defaultObj);
        holder.clearDefaultValue();
        assertNull(defaultViewField.get(holder), "Default value supplier has to be null after clearDefault");
        assertSame(systemDefault, holder.getDefaultValue(), "Default value has to be the same as systemDefault after clearDefaultValue");
    }

    @Test
    void testSetGetCurrentSupplier() throws Exception {
        final Object systemObj = new Object();
        final Object currentObj = new Object();
        DefaultsHolder<Object> holder = new DefaultsHolder<>(() -> systemObj);
        holder.setCurrentValue(() -> currentObj);
        assertSame(currentObj, holder.getCurrentValue(), "Current value has to be the same obj as defaultSupplier return");
        final Object newObject = new Object();
        AtomicReference<Object> ref = new AtomicReference<>();
        CountDownLatch cdl = new CountDownLatch(1);
        new Thread(() -> {
            holder.setCurrentValue(() -> newObject);
            ref.set(holder.getCurrentValue());
            cdl.countDown();
        }).start();
        cdl.await(50, TimeUnit.MILLISECONDS);
        assertSame(newObject, ref.get(), "Current value has to be the same with current supplier result, defined in the same thread");
        assertNotSame(newObject, holder.getCurrentValue(), "Current value has to be different with current supplier result, defined in other thread");
    }

    @Test
    void testSetGetCurrentValue() throws Exception {
        final Object systemObj = new Object();
        final Object currentObj = new Object();
        DefaultsHolder<Object> holder = new DefaultsHolder<>(() -> systemObj);
        holder.setCurrentValue(currentObj);
        assertSame(currentObj, holder.getCurrentValue(), "Current value has to be the same obj as defaultSupplier return");
    }

    @Test
    void testClearCurrentValue() {
        Object systemDefault = new Object();
        Object defaultObj = new Object();
        Object currentObj = new Object();
        DefaultsHolder<Object> holder = new DefaultsHolder<>(() -> systemDefault);
        holder.setDefaultValue(defaultObj);
        holder.setCurrentValue(currentObj);
        holder.clearCurrentValue();
        assertSame(defaultObj, holder.getCurrentValue(), "Default value has to be the same as default after clearCurrentValue");
    }

    @Test
    void testRegisterNullParam() {
        assertThrows(NullPointerException.class, () -> DefaultsHolder.register(null, new DefaultsHolder<>()), "DefaultsHolder.register(obj=null) has to throw NullPointerException");
        assertThrows(NullPointerException.class, () -> DefaultsHolder.register(new Object(), null), "DefaultsHolder.register(holder=null) has to throw NullPointerException");
    }

    @Test
    void testRegister() throws IllegalAccessException {
        DefaultsHolder<?> holder = new DefaultsHolder<>();
        Object obj = new Object();
        assertSame(holder, DefaultsHolder.register(obj, holder), "DefaultsHolder.register has got to return registered holder as result");
        assertTrue(holderMap().containsKey(obj), "DefaultsHolder.holderMap has to contains registered object key");
        assertSame(holder, holderMap().get(obj), "DefaultsHolder.holderMap has to contains registered holder value");
    }

    @Test
    void testUnregister() throws IllegalAccessException {
        DefaultsHolder<?> holder = new DefaultsHolder<>();
        Object obj = new Object();
        assertNull(DefaultsHolder.unregister(obj), "DefaultsHolder.unregister non registered object has to return null");
        DefaultsHolder.register(obj, holder);
        assertSame(holder, DefaultsHolder.unregister(obj), "DefaultsHolder.unregister non registered object has to return null");
        assertFalse(holderMap().containsKey(obj), "DefaultsHolder.holderMap unable to contains registered object key");
    }

    @Test
    void testHolder() {
        DefaultsHolder<?> holder = new DefaultsHolder<>();
        Object obj = new Object();
        assertNull(DefaultsHolder.holder(obj, null), "DefaultsHolder.holder has to return null after register call with null holder constructor parameter");
        assertNull(DefaultsHolder.holder(obj, () -> null), "DefaultsHolder.holder has to return null after register call with holder constructor generated null");
        DefaultsHolder.register(obj, holder);
        assertSame(holder, DefaultsHolder.holder(obj), "DefaultsHolder.holder has to return same holder after register call");
        assertNull(DefaultsHolder.holder(new Object()), "DefaultsHolder.holder has to return null for unknown object");
    }

    @Test
    void testHolderWithConstrucSupplier() {
        DefaultsHolder<?> holder = new DefaultsHolder<>();
        Object obj = new Object();
        assertNull(DefaultsHolder.holder(obj, null), "DefaultsHolder.holder has to return null if constructor is null");
        assertSame(holder, DefaultsHolder.holder(obj, () -> holder), "DefaultsHolder.holder has to return function-defined holder");
        assertSame(holder, DefaultsHolder.holder(obj), "DefaultsHolder.holder has to return same holder after holder call with function-defined holder constructor");
    }

}