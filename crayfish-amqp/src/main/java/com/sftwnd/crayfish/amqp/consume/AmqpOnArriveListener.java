package com.sftwnd.crayfish.amqp.consume;

import com.sftwnd.crayfish.amqp.message.AMQPMessage;

public interface AmqpOnArriveListener<Payload> extends AmqpOnEventListener {

    void onArrive(AMQPMessage<Payload> message);

}
