package com.sftwnd.crayfish.amqp.consume;

import com.sftwnd.crayfish.amqp.message.AMQPMessagePayload;
import com.sftwnd.crayfish.amqp.message.AMQPMessageTag;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

/**
 * Created by ashindarev on 05.08.16.
 */
public class DefaultAMQPMessageConverter implements AMQPMessageConverter<AMQPMessagePayload> {

    public static final DefaultAMQPMessageConverter DEFAULT_AMQP_MESSAGE_CONVERTER = new DefaultAMQPMessageConverter();

    @Override
    public AMQPMessageTag tag(String consumerTag, Envelope envelope, AMQP.BasicProperties properties) {
        return new AMQPMessageTag(consumerTag, envelope.getDeliveryTag());
    }

    @Override
    public AMQPMessagePayload payload(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        return new AMQPMessagePayload(envelope, properties, body);
    }

}
