package com.github.sftwnd.crayfish.common.utl;

import java.util.Objects;

public class StringUtl {

    public static final String toString(Object obj, String emptyDefault) {
        String result = Objects.toString(obj, emptyDefault);
        if (result == null || result.length() == 0) {
            result = emptyDefault;
        }
        return result;
    }

}
