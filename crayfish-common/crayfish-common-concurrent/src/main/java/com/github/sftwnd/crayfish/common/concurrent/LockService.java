/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.concurrent;

import javax.annotation.Nonnull;
import java.util.concurrent.locks.Lock;

/**
 * Сервис блокировок.
 */

public interface LockService {
    /**
     * <p>Позволяет получить объект для получения/снятия именованной блокировки.
     * <p>Блокировка получается на уровне треда, и является reentrable (т.е. тот-же тред может успешно несколько раз подряд
     * вызвать методы получения блокировки
     * ({@link Lock#lock()},
     * {@link Lock#tryLock()},
     * {@link Lock#tryLock(long, java.util.concurrent.TimeUnit)},
     * {@link Lock#lockInterruptibly()}). Каждый успешный вызов метода установки блокировки необходимо завешить
     * вызовом метода {@link Lock#unlock()}.
     * @param name имя блокировки
     * @return объект управления блокировкой
     */
    Lock getNamedLock(@Nonnull String name);
}
