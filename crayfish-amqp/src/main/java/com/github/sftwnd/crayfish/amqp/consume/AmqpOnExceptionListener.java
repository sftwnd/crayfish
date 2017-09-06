package com.github.sftwnd.crayfish.amqp.consume;

import com.github.sftwnd.crayfish.amqp.message.AMQPMessageTag;
import com.github.sftwnd.crayfish.messaging.Message;

import java.io.IOException;

public interface AmqpOnExceptionListener extends AmqpOnEventListener {

    void onException(Message<AMQPMessageTag, IOException> message);

}
