package com.github.sftwnd.crayfish.common.info;

public interface NamedInfoSaver<I> {

    void save(final NamedInfo<I> data) throws Exception;

}
