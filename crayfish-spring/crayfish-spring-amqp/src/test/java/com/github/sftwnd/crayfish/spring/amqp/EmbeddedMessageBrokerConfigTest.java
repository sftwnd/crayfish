package com.github.sftwnd.crayfish.spring.amqp;

import com.github.sftwnd.crayfish.Application;
import com.github.sftwnd.crayfish.embedded.amqp.qpid.EmbeddedMessageBroker;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.ClosedChannelException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by ashindarev on 12.03.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
@DependsOn(value = "embeddedMessageBroker")
public class EmbeddedMessageBrokerConfigTest {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedMessageBrokerConfigTest.class);

    @Autowired
    EmbeddedMessageBroker embeddedMessageBroker;

    @Value("${qpid.amqp_port:5672}")
    int qpidAmqpPort;

    @Test
    public void checkConnectionTest() throws URISyntaxException, TimeoutException, IOException, NoSuchAlgorithmException, KeyManagementException, InterruptedException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setPort(qpidAmqpPort);
        connectionFactory.setConnectionTimeout(30000);
        try (Connection connection = connectionFactory.newConnection()) {
            final String message = "Hello, World!!!";
            final StringBuilder received = new StringBuilder();
            Channel channel = connection.createChannel();
            try {
                String queueName = channel.queueDeclare().getQueue();
                CountDownLatch consumeLatch = new CountDownLatch(1);
                CountDownLatch messageLatch = new CountDownLatch(1);
                channel.basicConsume(queueName, new DefaultConsumer(channel) {
                    public void handleConsumeOk(String consumerTag) {
                        consumeLatch.countDown();
                    }

                    @Override
                    public void handleDelivery(String consumerTag,
                                               Envelope envelope,
                                               AMQP.BasicProperties properties,
                                               byte[] body)
                            throws IOException
                    {
                        received.append(new String(body));
                        messageLatch.countDown();
                        logger.info("Meaage received: '{}'", received.toString());
                    }
                });
                try {
                    consumeLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException iex) {
                    logger.warn("Consume latch was not countDowned: {}", iex.getLocalizedMessage());
                }
                channel.basicPublish("", queueName, null, message.getBytes());
                try {
                    messageLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException iex) {
                    logger.warn("HandleDelivery latch was not countDowned: {}", iex.getLocalizedMessage());
                }
                Assert.assertEquals("Message does not received.", message, received.toString());
            } finally {
                if (channel != null && channel.isOpen()) {
                    try {
                        channel.close();
                        channel = null;
                    } catch (ClosedChannelException ccex) {
                        logger.error("Channel closing error: {}", ccex.getLocalizedMessage());
                    }
                }
            }
        }
    }

    @After
    public void shutDown() {
        embeddedMessageBroker.shutDown();
    }

}