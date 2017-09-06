package com.github.sftwnd.crayfish.amqp.consume;

import com.github.sftwnd.crayfish.amqp.message.AMQPMessageTag;

/**
 * Created by ashindarev on 01.08.16.
 */
public interface AMQPMessageConverter<Payload> extends MessageConverter<AMQPMessageTag, Payload> {

}