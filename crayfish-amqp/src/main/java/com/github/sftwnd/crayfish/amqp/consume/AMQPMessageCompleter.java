package com.github.sftwnd.crayfish.amqp.consume;

import com.github.sftwnd.crayfish.messaging.MessageCompleter;
import com.github.sftwnd.crayfish.amqp.message.AMQPMessageTag;

/**
 * Интерфейс комплитера сообщений (применяется для нотификации об успешной обработке сообщения)
 */
public interface AMQPMessageCompleter extends MessageCompleter<AMQPMessageTag> {

}
