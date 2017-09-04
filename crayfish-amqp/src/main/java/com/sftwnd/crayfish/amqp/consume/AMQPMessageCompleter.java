package com.sftwnd.crayfish.amqp.consume;

import com.sftwnd.crayfish.amqp.message.AMQPMessageTag;
import com.sftwnd.crayfish.messaging.MessageCompleter;

/**
 * Интерфейс комплитера сообщений (применяется для нотификации об успешной обработке сообщения)
 */
public interface AMQPMessageCompleter extends MessageCompleter<AMQPMessageTag> {

}
