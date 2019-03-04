package com.github.sftwnd.crayfish.zookeeper;

import org.apache.curator.framework.CuratorFramework;

/**
 *
 */

public interface ZookeeperService {
    /**
     * Возвращает объект для нативной работы с ZooKeeper.
     * <p>
     * Если необходим отдельный экземпляр {@link CuratorFramework}, то можно воспользоваться методом {@link #createCuratorFramework()}.
     * <p>У возвращаемого объекта свойство {@link CuratorFramework#getNamespace()} не установлено.
     */
    CuratorFramework getCuratorFramework();

    /**
     * Возвращает новый объект для нативной работы с ZooKeeper.
     * <p> Это отдельный объект. Пользователь должен самостоятельно вызвать метод {@link CuratorFramework#start()} при начале работы
     * и {@link CuratorFramework#close()} при её окончании.
     */
    CuratorFramework createCuratorFramework();

}
