package com.github.sftwnd.crayfish.common.info;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.stream.Stream;

public abstract class NamedInfoStreamLoader<I extends Cloneable> implements NamedInfoStream<I> {

    @Getter(AccessLevel.PROTECTED)
    private final Class<I> clazz;

    public NamedInfoStreamLoader(Class<I> clazz) {
        this.clazz = clazz;
    }

    @Override
    abstract public Stream<NamedInfo<I>> get();

}

