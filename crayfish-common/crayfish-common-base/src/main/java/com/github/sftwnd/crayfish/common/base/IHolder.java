package com.github.sftwnd.crayfish.common.base;

public interface IHolder<T> {

    T getValue();
    void setValue(T value);

}
