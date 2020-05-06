package com.github.sftwnd.crayfish.common.json;

import lombok.SneakyThrows;

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
