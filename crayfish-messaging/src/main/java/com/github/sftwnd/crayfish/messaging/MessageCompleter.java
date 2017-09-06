package com.github.sftwnd.crayfish.messaging;

import java.io.IOException;

/**
 * Интерфейс комплитера сообщений (применяется для нотификации об успешной обработке сообщения)
 */
public interface MessageCompleter<Tag extends Comparable<Tag>>  {

    void messageComplete(Tag message) throws IOException;

}
