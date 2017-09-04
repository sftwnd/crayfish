package com.sftwnd.crayfish.amqp.consume;

import com.sftwnd.crayfish.messaging.Message;
import com.sftwnd.crayfish.messaging.impl.DefaultMessage;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

/**
 * Created by ashindarev on 01.08.16.
 */
public interface MessageConverter<Tag extends Comparable <Tag>, Payload> {

    Tag tag(String consumerTag, Envelope envelope, AMQP.BasicProperties properties);

    Payload payload(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException;

    default Message<Tag, Payload> convert(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        return new DefaultMessage<Tag, Payload>(
                       tag(consumerTag, envelope, properties)
                      ,payload(consumerTag, envelope, properties, body)
                   );
    }

}