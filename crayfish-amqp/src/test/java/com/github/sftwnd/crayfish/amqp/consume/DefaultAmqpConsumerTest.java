package com.github.sftwnd.crayfish.amqp.consume;

import com.github.sftwnd.crayfish.amqp.message.AMQPMessage;
import com.github.sftwnd.crayfish.amqp.message.AMQPMessagePayload;
import com.github.sftwnd.crayfish.amqp.message.TransportAMQPMessage;
import com.github.sftwnd.crayfish.embedded.amqp.qpid.junit.EmbeddedMessageQueueResource;
import com.github.sftwnd.crayfish.messaging.Message;
import com.github.sftwnd.crayfish.amqp.message.AMQPMessageTag;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Envelope;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ashindarev on 08.02.17.
 */
public class DefaultAmqpConsumerTest {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAmqpConsumerTest.class);

    @ClassRule
    public static EmbeddedMessageQueueResource embeddedMessageQueueResource = EmbeddedMessageQueueResource.constructEmbeddedMessageQueueResource();

    private static MessageConverter<AMQPMessageTag, AMQPMessagePayload> messageConverter = new DefaultAMQPMessageConverter();
    private static Connection connection;

    @BeforeClass
    public static void setUp() throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException {
        connection = embeddedMessageQueueResource.newConnection();
    }

    private Channel channel = null;

    @Before
    public void openChannel() throws IOException {
        channel = connection.createChannel();
    }

    @After
    public void closeChannel() throws IOException, TimeoutException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
    }

    @Test
    public void handleMessageTest() throws Exception {
        final int prefetchSize = 8;
        final int ackSize = 4;
        String queueName = channel.queueDeclare().getQueue();
        String originalMessage = new RandomStringGenerator.Builder().withinRange('a', 'z').build().generate(128);

        final List<TransportAMQPMessage> messages = new ArrayList<>();

        DefaultAmqpConsumer<AMQPMessagePayload> consumer = new DefaultAmqpConsumer<AMQPMessagePayload>(connection, queueName, prefetchSize, ackSize) {

            @Override
            public AMQPMessagePayload payload(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                return messageConverter.payload(consumerTag, envelope, properties, body);
            }

            @Override
            public synchronized void handleMessage(AMQPMessage<AMQPMessagePayload> message) throws IOException {
                synchronized (messages) {
                    messages.add(new TransportAMQPMessage(message.getTag(), message.getPayload()));
                    messages.notify();
                }
            }

            @Override
            public void handleException(Message<AMQPMessageTag, IOException> message) throws IOException {
                throw message.getPayload();
            }
        };
        consumer.start();
        channel.basicPublish("", queueName, null, originalMessage.getBytes());
        synchronized (messages) {
            if (messages.size() == 0) {
                messages.wait(3000L);
            }
        }
        Assert.assertTrue("Message has to be arrived.", messages.size() > 0);
        TransportAMQPMessage message = messages.get(0);
        Assert.assertNotNull("Message is null", message.getPayload());
        Assert.assertNotNull("Message body is null", message.getPayload().getBody());
        Assert.assertEquals("Message was changed diring AMQP transfer", originalMessage, new String(message.getPayload().getBody()));
    }

    @Test
    public void acknowledgeTest() throws Exception {
        final int prefetchSize = 327;
        final int ackSize = 193;
        final int messageCnt = RandomUtils.nextInt(9000, 19000);
        String queueName = channel.queueDeclare().getQueue();
        String originalMessage = new RandomStringGenerator.Builder().withinRange('a', 'z').build().generate(128);;
        AtomicInteger val = new AtomicInteger(0);
        int sum = 0;
        int received = 0;
        final List<Message<AMQPMessageTag, Integer>> messages = new ArrayList<>();
        DefaultAmqpConsumer<?> consumer = new DefaultAmqpConsumer<Integer>(connection, queueName, prefetchSize, ackSize) {

            @Override
            public Integer payload(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                @SuppressWarnings("unchecked")
                Integer result = new Integer(new String(body));
                return result;
            }

            @Override
            public void onArrive(AMQPMessage<Integer> message) {
                synchronized (messages) {
                    messages.add(message);
                    logger.debug("Message received: {}", message);
                }
            }

            @Override
            public void onException(AMQPMessage<IOException> message) {
                logger.error("Exception for message {} has been received: {}", message.getTag(), message.getPayload());
                try {
                    this.messageComplete(message.getTag());
                } catch (IOException ioex) {
                    logger.warn("Unable to complete message.", ioex);
                }
            }

        };
        consumer.start();
        for (int i=0; i<messageCnt; i++) {
            int intVal = RandomUtils.nextInt(1, 1000);
            sum += intVal;
            channel.basicPublish("", queueName, null, Integer.toString(intVal).getBytes());
        }
        int  lastReceived = -1;
        long lastTick = -1L;
        try {
            while (received < messageCnt) {
                int sz = 0;
                synchronized (messages) {
                    sz = messages.size();
                    if (sz == 0) {
                        messages.wait(100L);
                        sz = messages.size();
                    }
                }
                if (sz > 0) {
                    int id = RandomUtils.nextInt(0, sz);
                    Message<AMQPMessageTag, Integer> message;
                    synchronized (messages) {
                        message = messages.remove(id);
                    }
                    received++;
                    val.addAndGet(message.getPayload());
                    logger.debug("Complete Message: {}", message);
                    consumer.messageComplete(message.getTag());
                }
                if (received != lastReceived) {
                    lastReceived = received;
                    lastTick = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - lastTick >= 3500L) {
                    Assert.assertEquals("Unable to receive all AMQP messages", messageCnt, received);
                }
            }
            Assert.assertEquals("Wrong message result", sum, val.get());
        } finally {
            logger.debug("Messages generated: {}, messages received: {}, generated sum: {}, result sum: {}", messageCnt, received, sum, val.get());
        }
    }

    @AfterClass
    public static void tearDown() throws IOException {
        try {
            embeddedMessageQueueResource.closeConnection(connection);
        } finally {
            connection = null;
        }
    }

}