package com.sftwnd.crayfish.amqp.message;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

/**
 * Created by ashindarev on 05.08.16.
 */
public class AMQPMessagePayload {

    private final Envelope envelope;
    private final AMQP.BasicProperties properties;
    private final byte[] body;

    public AMQPMessagePayload(Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        this.envelope = envelope;
        this.properties = properties;
        this.body = body;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public AMQP.BasicProperties getProperties() {
        return properties;
    }

    public byte[] getBody() {
        return body;
    }

}