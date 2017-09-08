package com.github.sftwnd.crayfish.embedded.amqp.qpid.junit;


import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ashindarev on 08.02.17.
 */
public class EmbeddedMessageQueueResourceTest {

    private static final String DEFAULT_MESSAGE = "Hello, World!!!";

    @ClassRule
    public  static EmbeddedMessageQueueResource mqBroker = EmbeddedMessageQueueResource.constructEmbeddedMessageQueueResource();

    private static Connection connection;

    @BeforeClass
    public static void setUp() throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException {
        connection = mqBroker.newConnection();
        Assert.assertNotNull("Unable to connect to MQ Brocker", connection);
    }

    @Test
    public void broker_isAlive_test() throws IOException, TimeoutException, InterruptedException {
        Channel channel = connection.createChannel();
        try {
            final AtomicInteger cnt = new AtomicInteger(0);
            String queueName = channel.queueDeclare().getQueue();
            channel.basicConsume(queueName, new DefaultConsumer(channel) {
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body);
                    Assert.assertEquals("Invalid delivered message", DEFAULT_MESSAGE, message);
                    synchronized (cnt) {
                        cnt.incrementAndGet();
                        cnt.notify();
                    }
                }
            });
            channel.basicPublish("", queueName, null, DEFAULT_MESSAGE.getBytes());
            synchronized (cnt) {
                if (cnt.intValue() == 0) {
                    cnt.wait(3000L);
                }
            }
            Assert.assertTrue("Message is not received", cnt.intValue() != 0);
        } finally {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        }
    }

    @AfterClass
    public static void tearDown() throws IOException {
        try {
            mqBroker.closeConnection(connection);
        } finally {
            connection = null;
        }
    }

}