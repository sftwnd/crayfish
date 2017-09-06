package com.github.sftwnd.crayfish.embedded.amqp.qpid.junit;

import com.google.common.collect.ImmutableMap;
import com.github.sftwnd.crayfish.embedded.amqp.qpid.EmbeddedMessageBroker;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.lang3.RandomUtils;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static org.springframework.util.SocketUtils.findAvailableTcpPort;

/**
 * A JUnit resource that starts a Apache Qpid Message Queue Broker.
 *
 * <pre><code>
 * public class MyTest {
 *    {@literal @}ClassRule
 *     public static com.github.sftwnd.crayfish.amqp.qpid.EmbeddedMessageQueueResource mqBroker = new com.github.sftwnd.crayfish.amqp.qpid.EmbeddedMessageQueueResource();
 *
 *     private Connection connection;
 *
 *    {@literal @}BeforeClass
 *     public static void setupClass() {
 *        ConnectionFactory connectionFactory = new ConnectionFactory();
 *        connectionFactory.setHost("localhost");
 *        connectionFactory.setPort(mqBroker.getAmqpPort());
 *        connectionFactory.setVirtualHost(mqBroker.getVirtualHost());
 *        connectionFactory.setUsername(mqBroker.getUsername());
 *        connectionFactory.setPassword(mqBroker.getPassword());
 *
 *        Connection connection = connectionFactory.newConnection();
 *        // ...
 *     }
 *
 *    {@literal @}AfterClass
 *     public static void afterClass() {
 *        this.connection.close();
 *        // ...
 *     }
 *
 *    {@literal @}Test
 *     public testSomething(){
 *        Channel channel = connection.createChannel();
 *        // ...
 *     }
 *  }
 * </code></pre>
 */
public class EmbeddedMessageQueueResource extends ExternalResource {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedMessageQueueResource.class);

    public static final Map<String, Object> DEFAULT_VHOST_CONFIG = ImmutableMap.of("type", "Memory");

    private EmbeddedMessageBroker embeddedMessageBroker;

    public static final EmbeddedMessageQueueResource constructEmbeddedMessageQueueResource() {
        try {
            return new EmbeddedMessageQueueResource();
        } catch (IOException ioex) {
            logger.error("Unable to construct EmbeddedMessageQueueResource", ioex);
            return null;
        }
    }

    public EmbeddedMessageQueueResource() throws IOException {
        this(RandomUtils.nextInt(40000, 45000), RandomUtils.nextInt(50000, 55000));
    }

    public EmbeddedMessageQueueResource(Supplier<Integer> supplier) throws IOException {
        this(supplier.get());
    }

    public EmbeddedMessageQueueResource(int minPort, int maxPort) throws IOException {
        this(findAvailableTcpPort(Math.min(minPort, maxPort), Math.max(minPort, maxPort)));
    }

    public EmbeddedMessageQueueResource(int port) throws IOException {
        embeddedMessageBroker = new EmbeddedMessageBroker (
                null, port, null, "default", "guest", "guest",
                DEFAULT_VHOST_CONFIG, getTemporaryFolder()
        );
    }

    private static Path getTemporaryFolder() throws IOException {
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        return temporaryFolder.getRoot().toPath();
    }

    /**
     * A constructor, d'uh.
     */
    public EmbeddedMessageQueueResource(int amqpPort, int httpPort, String virtualHost, String username, String password,
                                        Map<String, Object> virtualHostConfig, Path temporaryFolder) throws IOException {
        embeddedMessageBroker = new EmbeddedMessageBroker("qpid", amqpPort, httpPort, virtualHost, username, password, virtualHostConfig, temporaryFolder);
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        embeddedMessageBroker.startUp();
    }

    @Override
    protected void after() {
        try {
            embeddedMessageBroker.tearDown();
        } finally {
            super.after();
        }
    }

    public int getAmqpPort() {
        return embeddedMessageBroker.getAmqpPort();
    }

    public String getPassword() {
        return embeddedMessageBroker.getPassword();
    }

    public String getUsername() {
        return embeddedMessageBroker.getUsername();
    }

    public String getVirtualHost() {
        return embeddedMessageBroker.getVirtualHost();
    }

    public String getUri() throws URISyntaxException {
        return embeddedMessageBroker.getUri();
    }

    public ConnectionFactory getConnectionFactory() throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException {
        return embeddedMessageBroker.getConnectionFactory();
    }

    private List<Connection> connections = new ArrayList<>();

    public Connection newConnection() throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException, IOException, TimeoutException {
        return embeddedMessageBroker.newConnection();
    }

    public Connection newConnection(int tries) throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException, IOException, TimeoutException {
        return embeddedMessageBroker.newConnection(tries);
    }

    public void closeConnection(Connection connection) throws IOException {
        embeddedMessageBroker.closeConnection(connection);
    }

}