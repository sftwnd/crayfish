package com.github.sftwnd.crayfish.distributed.core.instance;

import java.util.stream.Stream;

public interface InstanceManager<I extends InstanceInfo> {

    /**
     *
     * Получение списка активных экземпляров координаторов, работающих в данный момент
     * Не проверяет наличие блокировки
     *
     * @return Поток активных экземпляров координаторов
     */
    Stream<Instance<I>> instances();

    Instance<I> getCurrentInstance();

    void save();

}
