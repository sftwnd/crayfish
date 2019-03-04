package com.github.sftwnd.crayfish.common.json;

import com.github.sftwnd.crayfish.common.exception.ExceptionUtils;

import java.io.IOException;

public class JsonToStringProcessed {

    @Override
    public String toString() {
        return toString(this);
    }

    public static final String toString(Object obj) {
        try {
            return obj == null ? null : JsonMapper.serializeObject(obj);
        } catch (IOException ioex) {
            return ExceptionUtils.uncheckExceptions(ioex);
        }
    }



}
