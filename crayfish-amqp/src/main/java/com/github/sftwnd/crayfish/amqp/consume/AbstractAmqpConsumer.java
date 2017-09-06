package com.github.sftwnd.crayfish.amqp.consume;

import com.github.sftwnd.crayfish.amqp.message.AMQPMessage;
import com.github.sftwnd.crayfish.amqp.message.DefaultAMQPMessage;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Абстрактная заготовка для создания подписчика на заданную очередь.
 * Один объект реализуется для одной очереди.
 * Возможно создавать несколько подписчиков на очередь.
 */
public abstract class AbstractAmqpConsumer<Payload>
              extends DefaultConsumer
           implements AMQPMessageHandler<Payload>
                     ,AMQPMessageConverter<Payload>
{

    private static Logger logger = LoggerFactory.getLogger(AbstractAmqpConsumer.class);

    private final int ackSize;
    private final int prefetchSize;

    /**
     *  Создаём новый канал, а в случае исключения - закрываем его
     */
    public AbstractAmqpConsumer(Connection connection, String queue, int prefetchSize, int ackSize) throws IOException {
        super(connection.createChannel());
        this.ackSize = Math.max(1, ackSize);
        this.prefetchSize = prefetchSize <= 0 ? prefetchSize : Math.max(ackSize, prefetchSize);
        try {
            getChannel().basicQos(this.prefetchSize);
            getChannel().basicConsume(queue, false, this);
            logger.trace("Consumed to the queue: {} with new channel: {} [ack:{}, prefetch: {}]", queue, getChannel(), this.ackSize, this.prefetchSize);
        } catch (IOException ioex) {
            if (logger.isDebugEnabled()) {
                logger.error("Unable to consume on queue: '{}'", queue);
            }
            if (getChannel() != null && getChannel().isOpen()) {
                try {
                    getChannel().close();
                    logger.trace("Channel is closed");
                } catch (TimeoutException tmex) {
                    logger.trace("Unable to close channel for the broken connection.");
                }
            }
            throw ioex;
        } finally {
            start(AbstractAmqpConsumer.class);
        }
    }

    public int getAckSize() {
        return this.ackSize;
    }

    public int getPrefetchSize() {
        return prefetchSize;
    }

    @Override
    public void handleDelivery(String consumerTag,
                               Envelope envelope,
                               AMQP.BasicProperties properties,
                               byte[] body) throws IOException {
        awaitConstructor();
        try {
            super.handleDelivery(consumerTag, envelope, properties, body);
            handleMessage(convert(consumerTag, envelope, properties, body));
        } catch (IOException ioex) {
            AMQPMessage<IOException> message = new DefaultAMQPMessage<>(tag(consumerTag, envelope, properties), ioex);
            handleException(message);
        }
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        super.handleConsumeOk(consumerTag);
        logger.trace("handleConsumeOk(consumerTag:{})", consumerTag);
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        super.handleCancelOk(consumerTag);
        logger.trace("handleCancelOk(consumerTag:{})", consumerTag);
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        super.handleCancel(consumerTag);
        logger.trace("handleCancel(consumerTag:{})", consumerTag);
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        super.handleShutdownSignal(consumerTag, sig);
        logger.trace("handleShutdownSignal(consumerTag:{}, sig:{})", consumerTag, sig.getLocalizedMessage());
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        super.handleRecoverOk(consumerTag);
        logger.trace("handleRecoverOk(consumerTag:{})", consumerTag);
    }

    /**
     * Метод преобразует входящее сообщение в Message<Long, Payload> для дальнейшей обработки.
     */
    @Override
    public abstract AMQPMessage<Payload> convert(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException;

    public void start() {
        start(this.getClass());
    }

    protected final void awaitConstructor() {
        if (started == null || !started.equals(awaitClass())) {
            synchronized (this) {
                while (started == null || !started.equals(awaitClass())) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        logger.error("awaitConstructor method has been interrupted.", e);
                        return;
                    }
                }
            }
        }
    }

    private Class<? extends DefaultConsumer> started;

    protected synchronized final void start(Class<? extends DefaultConsumer> clazz) {
        this.started = clazz;
        this.notifyAll();
    }

    protected Class<? extends DefaultConsumer> awaitClass() {
        return (Class<? extends DefaultConsumer>)(this.getClass());
    }

}
