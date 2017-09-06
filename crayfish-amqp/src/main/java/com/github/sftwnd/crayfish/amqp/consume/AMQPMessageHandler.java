package com.github.sftwnd.crayfish.amqp.consume;

import com.github.sftwnd.crayfish.amqp.message.AMQPMessage;
import com.github.sftwnd.crayfish.amqp.message.AMQPMessageTag;
import com.github.sftwnd.crayfish.messaging.Message;
import com.github.sftwnd.crayfish.messaging.MessageHandler;

import java.io.IOException;

/**
 * Интерфейс обработчика сообщений. Применяется для нотификации о приходе нового сообщений
 */
public interface AMQPMessageHandler<Payload>  extends MessageHandler<AMQPMessageTag, Payload>{

    void handleMessage(AMQPMessage<Payload> message) throws IOException;

    void handleException(AMQPMessage<IOException> message) throws IOException;

    @Override
    @SuppressWarnings("unchecked")
    default void handleMessage(Message<AMQPMessageTag, Payload> message) throws IOException {
        handleMessage((AMQPMessage)message);
    }

    @Override
    default void handleException(Message<AMQPMessageTag, IOException> message) throws IOException {
        handleException((AMQPMessage<IOException>)message);
    }


}
