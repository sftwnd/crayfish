package com.github.sftwnd.crayfish.common.info;

import java.util.stream.Stream;

public interface NamedInfoLoader<I>  {

    Stream<NamedInfo<I>> load() throws NamedInfoLoadEception;

}
