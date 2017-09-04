package com.sftwnd.crayfish.amqp.consume;

import com.sftwnd.crayfish.amqp.message.AMQPMessageTag;
import com.sftwnd.crayfish.messaging.Message;

import java.io.IOException;

public interface AmqpOnExceptionListener extends AmqpOnEventListener {

    void onException(Message<AMQPMessageTag, IOException> message);

}
