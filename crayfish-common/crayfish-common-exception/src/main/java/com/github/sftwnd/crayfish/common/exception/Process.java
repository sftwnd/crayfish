package com.github.sftwnd.crayfish.common.exception;

public interface Process<E extends Exception> {

    void work() throws E;

}
