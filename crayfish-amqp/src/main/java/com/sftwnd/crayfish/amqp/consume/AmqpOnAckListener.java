package com.sftwnd.crayfish.amqp.consume;

import com.sftwnd.crayfish.amqp.message.AMQPMessageTag;

public interface AmqpOnAckListener extends AmqpOnEventListener {

    void onAck(AMQPMessageTag tag);

}
