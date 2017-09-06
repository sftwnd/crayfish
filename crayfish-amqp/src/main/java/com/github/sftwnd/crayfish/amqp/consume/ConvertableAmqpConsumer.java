package com.github.sftwnd.crayfish.amqp.consume;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.List;

/**
 * Created by ashindarev on 02.03.17.
 */
public class ConvertableAmqpConsumer<Payload> extends DefaultAmqpConsumer<Payload> {

    AMQPMessageConverter<Payload> messageConverter;

    public ConvertableAmqpConsumer(Connection connection, String queue, int prefetchSize, int ackSize, AMQPMessageConverter<Payload> messageConverter, List<AmqpOnEventListener> listeners) throws IOException {
        super(connection, queue, prefetchSize, ackSize, listeners);
        assert messageConverter != null;
        this.messageConverter = messageConverter;
        start(ConvertableAmqpConsumer.class);
    }

    public ConvertableAmqpConsumer(Connection connection, String queue, int prefetchSize, int ackSize, AMQPMessageConverter<Payload> messageConverter) throws IOException {
        this(connection, queue, prefetchSize, ackSize, messageConverter, null);
    }

    @Override
    public Payload payload(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        return messageConverter.payload(consumerTag, envelope, properties, body);
    }

}
