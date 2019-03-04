package com.github.sftwnd.crayfish.common.concurrent;

public class RevokedException extends RuntimeException {

    private static final long serialVersionUID = -8140155950354692609L;

    public RevokedException() {
        super();
    }

    public RevokedException(String message) {
        super(message);
    }

}
