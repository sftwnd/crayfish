package com.github.sftwnd.crayfish.common.state;

import com.github.sftwnd.crayfish.common.state.DefaultsHolder.ValueLevel;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DefaultsHelperTest {

    @Test
    void testDefaultsHelper() throws NoSuchFieldException, IllegalAccessException {
        Object object = new Object();
        DefaultsHolder<Object> holder = new DefaultsHolder<>(() -> object);
        Field field = DefaultsHelper.class.getDeclaredField("holder");
        field.setAccessible(true);
        DefaultsHelper<Object> helper = new DefaultsHelper<>(holder);
        assertSame(holder, field.get(helper), "DefaultsHolder.holder must contains construct parameter result");
    }

    @Test
    void testCall() throws Exception {
        Object defaultValue = new Object();
        DefaultsHolder<Object> holder = new DefaultsHolder<>(() -> defaultValue);
        DefaultsHelper<Object> helper = new DefaultsHelper<>(holder);
        Object newValue = new Object();
        Object callResult = helper.call(newValue, () -> holder.getCurrentValue());
        ValueLevel holderValueLevel = holder.getValueLevel();
        assertSame(newValue, callResult, "DefaultsHelper.call has to use new value as result of holder.getValue");
        assertEquals(holderValueLevel, holder.getValueLevel(), "DefaultsHelper valueLevel has to be unchanged after the call");
        holder.setCurrentValue(new Object());
        helper.call(newValue, () -> new Object());
        assertEquals(ValueLevel.CURRENT, holder.getValueLevel(), "DefaultsHelper has to be CURRENT after the call on holder in CURRENT level");
    }

    @Test
    void testRun() {
        Object defaultValue = new Object();
        DefaultsHolder<Object> holder = new DefaultsHolder<>(() -> defaultValue);
        DefaultsHelper<Object> helper = new DefaultsHelper<>(holder);
        Object newValue = new Object();
        AtomicReference<Object> result = new AtomicReference<>();
        helper.run(newValue, () -> result.set(holder.getCurrentValue()));
        ValueLevel holderValueLevel = holder.getValueLevel();
        assertSame(newValue, result.get(), "DefaultsHelper.run has to use new value as result of holder.getValue");
        assertEquals(holderValueLevel, holder.getValueLevel(), "DefaultsHelper valueLevel has to be unchanged after the run method call");
        holder.setCurrentValue(new Object());
        helper.run(newValue, () -> {});
        assertEquals(ValueLevel.CURRENT, holder.getValueLevel(), "DefaultsHelper has to be CURRENT after the run method call on holder in CURRENT level");
    }

    @Test
    void testProcess() throws Exception {
        Object defaultValue = new Object();
        DefaultsHolder<Object> holder = new DefaultsHolder<>(() -> defaultValue);
        DefaultsHelper<Object> helper = new DefaultsHelper<>(holder);
        Object newValue = new Object();
        AtomicReference<Object> result = new AtomicReference<>();
        helper.process(newValue, () -> result.set(holder.getCurrentValue()));
        ValueLevel holderValueLevel = holder.getValueLevel();
        assertSame(newValue, result.get(), "DefaultsHelper.process has to use new value as result of holder.getValue");
        assertEquals(holderValueLevel, holder.getValueLevel(), "DefaultsHelper valueLevel has to be unchanged after the process method call");
        holder.setCurrentValue(new Object());
        helper.process(newValue, () -> {});
        assertEquals(ValueLevel.CURRENT, holder.getValueLevel(), "DefaultsHelper has to be CURRENT after the process method call on holder in CURRENT level");
    }

}