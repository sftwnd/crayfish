package com.github.sftwnd.crayfish.common.resource;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 *
 *  Класс для ленивой инициализации доступа к ресурсу.
 *  Реализует провайдер, который сам уже является ресурсом
 *
 *  @author Andrey D. Shindarev
 *  @ 2019.09.25
 */
public class LazyResourceSelfProvider<R> extends LazyResourceProvider<R, R> {

    protected LazyResourceSelfProvider(@Nonnull Constructor<R> constructor) {
        super(constructor, (resource) -> resource);
    }

    public synchronized void setOnClose(Consumer<R> onClose) {
        super.setOnClose((p, r) -> onClose.accept(r));
    }


}
