package com.sftwnd.crayfish.messaging;

import java.io.IOException;

/**
 * Интерфейс обработчика сообщений. Применяется для нотификации о приходе нового сообщений
 */
public interface MessageHandler<Tag extends Comparable<Tag>, Payload>  {

    void handleMessage(Message<Tag, Payload> message) throws IOException;

    void handleException(Message<Tag, IOException> message) throws IOException;

    default void touch() throws IOException, InterruptedException {
        handleMessage(null);
    }

}
