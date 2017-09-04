package com.sftwnd.crayfish.amqp.message;

/**
 * Created by ashindarev on 05.08.16.
 */
public class TransportAMQPMessage extends DefaultAMQPMessage<AMQPMessagePayload> {

    public TransportAMQPMessage(AMQPMessageTag tag, AMQPMessagePayload payload) {
        super(tag, payload);
    }

}
