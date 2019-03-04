package com.github.sftwnd.crayfish.common.info;

import java.util.function.Supplier;
import java.util.stream.Stream;

public interface NamedInfoStream<I extends Cloneable> extends Supplier<Stream<NamedInfo<I>>> {

}
