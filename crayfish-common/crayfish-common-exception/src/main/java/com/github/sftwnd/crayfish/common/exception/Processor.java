package com.github.sftwnd.crayfish.common.exception;

public interface Processor<E extends Exception> {

    void process() throws E;

}
