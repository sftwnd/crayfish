package com.github.sftwnd.crayfish.utils.logging;

import org.slf4j.MDC;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Slf4jMdcUtility {

    public static <T> Supplier<T> supplier(Map<String, String> mdcContext, Supplier<T> supplier) {
        return supplier(mdcContext, supplier, true);
    }

    public static <T> Supplier<T> supplier(Map<String, String> mdcContext, Supplier<T> supplier, boolean addContext) {
        return new Supplier<T>() {
            @Override
            public T get() {
                Map<String, String> savedMdcContext = MDC.getCopyOfContextMap();
                try {
                    setMdcContext(mdcContext, addContext);
                    return supplier.get();
                } finally {
                    setMdcContext(savedMdcContext, false);
                }
            }
        };
    }

    private static void setMdcContext(Map<String, String> mdcContext, boolean addContext) {
        if (mdcContext == null || mdcContext.isEmpty()) {
            if (!addContext) {
                MDC.clear();
            }
        } else {
            if (addContext) {
                mdcContext.forEach(
                        (key, value) -> MDC.put(key, value)
                );
            } else {
                MDC.setContextMap(mdcContext);
            }
        }
    }

    public static <T> Supplier<T> supplier(String mdcKey, String mdcValue, Supplier<T> supplier) {
        return supplier(mdcKey, mdcValue, supplier, true);
    }

    public static <T> Supplier<T> supplier(String mdcKey, String mdcValue, Supplier<T> supplier, boolean addContext) {
        // ToDo: использование собственной реализации без map эффактивнее. По возможности можно соптимизировать, но пока не обязательно
        assert mdcKey != null;
        return supplier(IntStream.range(0, 1).boxed().collect(Collectors.toMap(i -> mdcKey, i ->mdcValue)), supplier, addContext);
    }

}
