package com.github.sftwnd.crayfish.distributed.core.instance;

import com.github.sftwnd.crayfish.common.info.BaseNamedInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

class InstanceTest {

    private final String name  = "Instance";
    private final long   value = new Random().nextLong();

    private Instance<LongInstanceInfo> instance;

    @BeforeEach
    void setUp() {
        instance = new Instance<>("Instance", new LongInstanceInfo(value));
    }

    @AfterEach
    void tearDown() {
        instance = null;
    }

    @Test
    @SuppressWarnings("unchecked")
    void cloneTest() {
        new BaseNamedInfo<LongInstanceInfo>();
        Instance<LongInstanceInfo> clone = this.instance.clone();
        Assertions.assertEquals(clone.getClass(), Instance.class, "Cloned value has wrong class");
        Assertions.assertNotNull(clone.getInfo(), "InstanceInfo.info has to be not null");
        Assertions.assertEquals(clone.getInfo().value, value, "InstanceInfo has to be equals");
    }

    @AllArgsConstructor
    class LongInstanceInfo implements InstanceInfo {

        @Getter
        private long value;

        @Override
        @SuppressWarnings("squid:S2975")
        public LongInstanceInfo clone() {
            return new LongInstanceInfo(value);
        }

    }

}