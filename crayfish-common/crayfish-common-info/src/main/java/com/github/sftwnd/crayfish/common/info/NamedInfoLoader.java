package com.github.sftwnd.crayfish.common.info;

import java.util.function.Function;
import java.util.stream.Stream;

@FunctionalInterface
public interface NamedInfoLoader<I extends Cloneable> extends Function<NamedInfoLoader.OnThrow, Stream<NamedInfo<I>>> {

    enum OnThrow {
        THROW
        ,SKIP
        ,CLEAN
        ,EMPTY
    }

}
