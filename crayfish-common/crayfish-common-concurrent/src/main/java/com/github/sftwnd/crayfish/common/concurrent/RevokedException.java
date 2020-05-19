/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
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
