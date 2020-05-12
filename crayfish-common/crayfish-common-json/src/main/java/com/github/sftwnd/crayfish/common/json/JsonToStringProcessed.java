/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.json;

import lombok.SneakyThrows;

/**
 * Base class for Object with ability to provide toString result as JSON representation
 */
public class JsonToStringProcessed {

    @Override
    public String toString() {
        return toString(this);
    }

    @SneakyThrows
    public static final String toString(Object obj) {
        return obj == null ? null : JsonMapper.serializeObject(obj);
    }

}
