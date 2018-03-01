package com.github.sftwnd.crayfish.utils.logging;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Slf4jMdcUtilityTest {

    private static final String logMessage = "Message...";

    TestLogger logger = TestLoggerFactory.getTestLogger(Slf4jMdcUtilitySupplier.class);

    public static class Slf4jMdcUtilitySupplier implements Supplier<String> {

        private static final Logger logger = LoggerFactory.getLogger(Slf4jMdcUtilitySupplier.class);

        @Override
        public String get() {
            logger.info(logMessage);
            return logMessage;
        }

    }

    @Test
    public void test() {
        String key = "user";
        String value = "USER";
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        Supplier<String> supplier = new Slf4jMdcUtilitySupplier();
        // Проверяем, что проставляется MDC в Logger-е
        Slf4jMdcUtility.supplier("user","USER", supplier).get();
        Assert.assertNotNull("There are noone message in log", logger.getAllLoggingEvents());
        Assert.assertEquals("There is wrong logger event list size", 1, logger.getAllLoggingEvents().size());
        logger.getAllLoggingEvents().forEach((event) -> {
            Assert.assertEquals("Wrong MDC value", map, event.getMdc());
        });
        // Проверяем, что MDC в Logger-е не проставляется после использования из Supplier-а
        logger.clearAll();
        logger.info(logMessage);
        Assert.assertNotNull("There are noone message in log", logger.getAllLoggingEvents());
        Assert.assertEquals("There is wrong logger event list size", 1, logger.getAllLoggingEvents().size());
        logger.getAllLoggingEvents().forEach((event) -> {
            Assert.assertEquals("Wrong MDC size", 0, event.getMdc().size());
        });
    }


}
