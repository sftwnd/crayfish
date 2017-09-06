package com.github.sftwnd.crayfish.amqp.message;

import com.github.sftwnd.crayfish.messaging.impl.DefaultMessage;

/**
 * Created by ashindarev on 05.08.16.
 */
public class DefaultAMQPMessage<Payload> extends DefaultMessage<AMQPMessageTag, Payload> implements AMQPMessage<Payload> {

    public DefaultAMQPMessage(AMQPMessageTag tag, Payload payload) {
        super(tag, payload);
    }

}
