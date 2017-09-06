package com.github.sftwnd.crayfish.messaging;

import java.io.IOException;

/**
 * Created by ashindarev on 09.02.17.
 */
public class MessageConverterException extends IOException {

    public MessageConverterException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageConverterException(String message) {
        this(message, null);
    }

    public MessageConverterException(Throwable cause) {
        this(null, cause);
    }

    public MessageConverterException() {
        this(null, null);
    }

}
