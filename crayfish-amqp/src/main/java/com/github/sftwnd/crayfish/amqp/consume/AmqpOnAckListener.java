package com.github.sftwnd.crayfish.amqp.consume;

import com.github.sftwnd.crayfish.amqp.message.AMQPMessageTag;

public interface AmqpOnAckListener extends AmqpOnEventListener {

    void onAck(AMQPMessageTag tag);

}
