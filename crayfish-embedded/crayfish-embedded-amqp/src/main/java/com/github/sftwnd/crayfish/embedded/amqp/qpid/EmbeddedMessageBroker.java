package com.github.sftwnd.crayfish.embedded.amqp.qpid;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
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
 * A resource that starts a Apache Qpid Message Queue Broker.
 *
 */
public class EmbeddedMessageBroker {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedMessageBroker.class);

    public static final Map<String, Object> DEFAULT_VHOST_CONFIG = ImmutableMap.of("type", "Memory");

    private final int amqpPort;
    private final int httpPort;
    private final String brokerName;
    private final String virtualHost;
    private final String username;
    private final String password;
    private final Map<String, Object> virtualHostConfig;

    private Path rootFolder;

    private Broker mqBroker;

    public EmbeddedMessageBroker() throws IOException {
        this(AMQP.PROTOCOL.PORT);
    }

    public EmbeddedMessageBroker(Supplier<Integer> supplier) throws IOException  {
        this(supplier.get());
    }

    public EmbeddedMessageBroker(int minPort, int maxPort) throws IOException {
        this(findAvailableTcpPort(Math.min(minPort, maxPort), Math.max(minPort, maxPort)));
    }

    public EmbeddedMessageBroker(int port) throws IOException {
        this("qpid", port, null, "default", "guest", "guest", DEFAULT_VHOST_CONFIG, null);
    }

    /**
     * A constructor, d'uh.
     */
    public EmbeddedMessageBroker(String brokerName, Integer amqpPort, Integer httpPort, String virtualHost, String username, String password,
                                 Map<String, Object> virtualHostConfig, Path rootFolder) throws IOException {
        this.brokerName = brokerName == null || brokerName.trim().length() == 0 ? "qpid" : brokerName.trim();
        this.amqpPort = amqpPort == null || amqpPort.intValue() <= 0 ? AMQP.PROTOCOL.PORT : amqpPort;
        this.httpPort = httpPort == null || httpPort.intValue() <= 0 ? 8080 : httpPort.intValue();
        this.virtualHost = virtualHost;
        this.virtualHostConfig = virtualHostConfig;
        this.username = username;
        this.password = password;
        this.rootFolder = rootFolder == null ? Files.createTempDirectory("qpid-") : rootFolder;
    }

    public void startUp() throws Exception {

        final File homeDirectory = new File(rootFolder.toFile(), "home");
        final File workDirectory = new File(rootFolder.toFile(), "wordkir");
        final File etcDirectory  = new File(homeDirectory, "etc");

        for (File file: new File[] {this.rootFolder.toFile(), homeDirectory, workDirectory, etcDirectory}) {
            if (!file.exists()) {
                file.deleteOnExit();
                file.mkdir();
            }
        }

        String config = Resources.toString(Resources.getResource("apache-qpid/conf.json"), Charsets.UTF_8);
        try (OutputStream os = new FileOutputStream(new File(homeDirectory, "conf.json"))) {
            os.write(config.getBytes());
        }
        String passwd;
        try {
            passwd = Resources.toString(Resources.getResource("apache-qpid/passwd"), Charsets.UTF_8);
        } catch (Exception ex) {
            logger.warn("Unable to find file \"apache-qpid/passwd\". Default file has been created");
            passwd = this.username + ":" + this.password;
        }
        try (OutputStream os = new FileOutputStream(new File(etcDirectory, "passwd"))) {
            os.write(passwd.getBytes());
        }

        mqBroker = new Broker();

        BrokerOptions options = new BrokerOptions();
        options.setConfigProperty("qpid.broker.brokerName", this.brokerName);
        options.setConfigProperty("qpid.work_dir", workDirectory.getAbsolutePath());
        options.setConfigProperty("qpid.home_dir", homeDirectory.getAbsolutePath());

        options.setConfigProperty("qpid.amqp_port", String.valueOf(this.amqpPort));
        options.setConfigProperty("qpid.http_port", String.valueOf(this.httpPort));

        options.setConfigProperty("qpid.virtual_host", this.virtualHost);
        options.setConfigProperty("qpid.virtual_host_json_config", new ObjectMapper().writeValueAsString(this.virtualHostConfig));

        options.setConfigProperty("queue.deadLetterQueueEnabled", "true");
        // "virtualHostInitialConfiguration" : "{\"type\":\"Memory\",\"brokerName\":\"default\",\"modelVersion\":\"6.1\"}"
        // "virtualHostInitialConfiguration" : "${qpid.initial_config_virtualhost_config}"
        options.setConfigProperty("derby.stream.error.file", "derby.log");

        options.setStartupLoggedToSystemOut(false);

        options.setInitialConfigurationLocation(homeDirectory.getAbsolutePath() + File.separator + "conf.json");

        String derbyLog = System.getProperty("derby.stream.error.file");
        System.setProperty("derby.stream.error.file", new File(workDirectory, "derby.log").getAbsolutePath());

        try {
            mqBroker.startup(options);
        } finally {
            if (derbyLog == null) {
                System.clearProperty("derby.stream.error.file");
            } else {
                System.setProperty("derby.stream.error.file", derbyLog);
            }
        }

        reloadLogbackConfig();

        logger.info("Embedded Message Queue Broker (Apache Qpid) started on port {} using username '{}'", amqpPort, username);
        logger.warn("Apache Qpid isn'conf.json RabbitMQ! Learn more about it: https://www.rabbitmq.com/interoperability.html");
    }

    /**
     * Apache Qpid modifies the Logging configuration manually since it was designed to run as a standalone server.
     *
     * <p>We need to reload the config to go back to the expected settings.</p>
     */
    private void reloadLogbackConfig() throws JoranException {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ContextInitializer ci = new ContextInitializer(loggerContext);
        URL url = ci.findURLOfDefaultConfigurationFile(true);
        if (url == null) {
            logger.error("Could not reload Logback config to be reloaded. This will reduce logging visibility.");
            return;
        }
        loggerContext.reset();
        ci.configureByResource(url);
    }

    public void tearDown() {
        try {
            for (Connection connection : connections) {
                if (connection != null && connection.isOpen()) {
                    try {
                        connection.close();
                    } catch (IOException e) {
                        logger.info("Unable to close AMQP connection.", e);
                    }
                }
            }
            connections.clear();
            if (mqBroker != null) {
                mqBroker.shutdown();
                mqBroker = null;
            }
        } finally {
            rootFolder.toFile().delete();
        }
    }

    public String getBrokerName() {
        return brokerName;
    }

    public int getAmqpPort() {
        return amqpPort;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public String getUri() throws URISyntaxException {
        return new StringBuilder("amqp://localhost:").append(getAmqpPort()).toString();
    }

    public ConnectionFactory getConnectionFactory() throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri(getUri());
        connectionFactory.setUsername(getUsername());
        connectionFactory.setPassword(getPassword());
        connectionFactory.setVirtualHost(getVirtualHost());
        return connectionFactory;
    }

    private List<Connection> connections = new ArrayList<>();

    public Connection newConnection() throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException, IOException, TimeoutException {
        return newConnection(5);
    }

    public Connection newConnection(int tries) throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException, IOException, TimeoutException {
        return _newConnection(Math.max(tries, 1));
    }

    private Connection _newConnection(int tries) throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException, IOException, TimeoutException {
        ConnectionFactory connectionFactory = getConnectionFactory();
        Connection connection = null;
        while (connection == null) {
            try {
                connection = connectionFactory.newConnection();
                connections.add(connection);
                return connection;
            } catch (Exception ex) {
                if (--tries == 0) {
                    logger.error("Unable to open rabbitMq connection to amqp service: {}", connectionFactory.toString(), ex);
                }
            }
        }
        return connection;
    }

    public void closeConnection(Connection connection) throws IOException {
        if (connection != null) {
            try {
                if (connection.isOpen()) {
                    connection.close();
                }
            } finally {
                if (connections.contains(connection)) {
                    connections.remove(connection);
                }
            }
        }
    }

    public void shutDown() {
        tearDown();
    }

}