package com.github.sftwnd.crayfish.distributed.core.resource;

import java.io.Closeable;
import java.util.stream.Stream;

public interface ResourceManager<R extends ResourceInfo> extends Closeable {

    String getResourceTypeName();

    /**
     *
     * Получение списка имеющихся в наличии ресурсов
     *
     * @return Поток имеющихся ресурсов
     */
    Stream<Resource<R>> resources();

}
