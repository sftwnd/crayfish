package com.github.sftwnd.crayfish.common.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Объект обёртка для другого объекта. Удобно использовать для объявления <code>final</code> полей (например, для
 * использования в замыканиях), которые по каким-то причинам необходимо изменять.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VolatileHolder<T> implements IHolder<T> {

    // Переменная сделана public сознательно для совместимости с javax.xml.ws
    @SuppressWarnings({"squid:ClassVariableVisibilityCheck"})
    public volatile T value;

}
