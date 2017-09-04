package com.sftwnd.crayfish.amqp.consume;

import com.sftwnd.crayfish.amqp.message.AMQPMessageTag;

public interface AmqpOnCompleteListener<Payload> extends AmqpOnEventListener {

    void onComplete(AMQPMessageTag tag);

}
