package com.github.sftwnd.crayfish.common.resource;

import lombok.Generated;

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
@Generated
public class LazyResourceSelfProvider<R> extends LazyResourceProvider<R, R> {

    public LazyResourceSelfProvider(@Nonnull Constructor<R> constructor) {
        super(constructor, resource -> resource);
    }

    public synchronized void setOnDestroy(Consumer<R> onDestroy) {
        super.setOnDestroy((p, r) -> onDestroy.accept(r));
    }

}
