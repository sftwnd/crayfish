package com.github.sftwnd.crayfish.amqp.message;

import com.github.sftwnd.crayfish.messaging.Message;

/**
 * Created by ashindarev on 05.08.16.
 */
public interface AMQPMessage<Payload> extends Message<AMQPMessageTag, Payload> {

}