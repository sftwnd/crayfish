package com.sftwnd.crayfish.amqp.consume;

import com.sftwnd.crayfish.amqp.message.AMQPMessage;
import com.sftwnd.crayfish.amqp.message.AMQPMessageTag;
import com.sftwnd.crayfish.amqp.message.DefaultAMQPMessage;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

/**
 * Created by ashindarev on 01.08.16.
 */
public abstract class BaseAmqpConsumer<Payload> extends AbstractAmqpConsumer<Payload> {

    public BaseAmqpConsumer(Connection connection, String queue, int prefetchSize, int ackSize) throws IOException {
        super(connection, queue, prefetchSize, ackSize);
        start(BaseAmqpConsumer.class);
    }

    @Override
    public AMQPMessage<Payload> convert(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        return envelope == null
               ? null
               : new DefaultAMQPMessage<>(new AMQPMessageTag(consumerTag, envelope.getDeliveryTag()), payload(consumerTag, envelope, properties, body));
    }

    public abstract Payload payload(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException;

}
