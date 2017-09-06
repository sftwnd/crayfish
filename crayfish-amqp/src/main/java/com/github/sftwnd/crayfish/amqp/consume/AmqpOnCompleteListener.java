package com.github.sftwnd.crayfish.amqp.consume;

import com.github.sftwnd.crayfish.amqp.message.AMQPMessageTag;

public interface AmqpOnCompleteListener<Payload> extends AmqpOnEventListener {

    void onComplete(AMQPMessageTag tag);

}
