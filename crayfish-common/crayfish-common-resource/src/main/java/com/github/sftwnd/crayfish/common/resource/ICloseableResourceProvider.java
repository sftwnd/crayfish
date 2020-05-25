package com.github.sftwnd.crayfish.common.resource;

/**
 *
 *  Интерфейс для ленивой инициализации соединения c ресурсом.
 *  Описывает провайдер соединения, который оформляет получение ресурса
 *
 *  @author Andrey D. Shindarev
 *  2019.09.25
 */
@SuppressWarnings("try")
public interface ICloseableResourceProvider<R> extends IResourceProvider<R>, AutoCloseable {

    @Override
    void close();

}
