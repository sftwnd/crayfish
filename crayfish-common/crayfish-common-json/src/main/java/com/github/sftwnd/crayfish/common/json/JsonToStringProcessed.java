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
