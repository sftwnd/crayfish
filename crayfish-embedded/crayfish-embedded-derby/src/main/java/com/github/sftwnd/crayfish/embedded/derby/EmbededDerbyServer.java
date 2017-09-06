package com.github.sftwnd.crayfish.embedded.derby;

import org.apache.derby.drda.NetworkServerControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

/**
 * Created by ashindarev on 21.02.17.
 */
@Configuration("crayfish-embeded-derby-server")
@Profile("crayfish-embeded-derby-server")
@ConfigurationProperties(prefix = "")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EmbededDerbyServer {

    private static final Logger logger = LoggerFactory.getLogger(EmbededDerbyServer.class);

    private static final String DERBY_PROPERTIES_PREFIX = "derby.";
    private static final String DERBY_SYSTEM_HOME_PROPERTY_SUFFIX = "system.home";
    private static final String DERBY_SYSTEM_HOME_PROPERTY = DERBY_PROPERTIES_PREFIX + DERBY_SYSTEM_HOME_PROPERTY_SUFFIX;
    private static final String DERBY_PROPERTIES_FILE = "derby.properties";

    private Properties derby = new Properties();
    public  Properties getDerby() {
        return derby;
    }

    private NetworkServerControl networkServerControl;

    @Bean(name = "networkServerControl", initMethod="", destroyMethod = "shutdown")
    public NetworkServerControl getDerbyServer() throws Exception {
        return networkServerControl;
    }

    @PostConstruct
    public void postConstruct() {
        try {
            networkServerControl = constructDerbyServer();
        } catch (Exception e) {
            logger.error("Unable to start EmbededDerbyServer", e);
        }
    }

    private NetworkServerControl constructDerbyServer() throws Exception {
        // TODO: Учесть в дальнейшем вариант, когда system переменные не могут переопределить значение из derby.properties (запред переопределения derby.system home)
        // Вычтисляем derby.system.home
        File derbySystemHome = new File( System.getProperties().containsKey(DERBY_SYSTEM_HOME_PROPERTY)
                                         ? System.getProperties().getProperty(DERBY_SYSTEM_HOME_PROPERTY) == null ||
                                           System.getProperties().getProperty(DERBY_SYSTEM_HOME_PROPERTY).trim().length() == 0
                                           ? "."
                                           : System.getProperties().getProperty(DERBY_SYSTEM_HOME_PROPERTY)
                                         : derby.containsKey(DERBY_SYSTEM_HOME_PROPERTY_SUFFIX) &&
                                           derby.getProperty(DERBY_SYSTEM_HOME_PROPERTY_SUFFIX) != null &&
                                           derby.getProperty(DERBY_SYSTEM_HOME_PROPERTY_SUFFIX).trim().length() > 0
                                           ? derby.getProperty(DERBY_SYSTEM_HOME_PROPERTY_SUFFIX)
                                           : "." );
        // Пытаемся создать путь derby.system.home если не существует
        if (!derbySystemHome.exists()) {
            derbySystemHome.mkdirs();
        }
        // Заносим значение derby.system.home в системную переменную
        System.setProperty(DERBY_SYSTEM_HOME_PROPERTY, derbySystemHome.getAbsolutePath());

        logger.debug("{}={}",DERBY_SYSTEM_HOME_PROPERTY,System.getProperty(DERBY_SYSTEM_HOME_PROPERTY));
        // Все derby.* переменные заносим в derby.properties, размещённый в derby.system.home (за исключением derby.system.home переменной)
        if (derby.size() > (derby.containsKey(DERBY_SYSTEM_HOME_PROPERTY_SUFFIX) ? 1 : 0)) {

            File derbyProperties = new File(derbySystemHome, DERBY_PROPERTIES_FILE);
            try (PrintWriter writer = new PrintWriter(new FileOutputStream(derbyProperties))) {
                for (Map.Entry<Object, Object> entry : derby.entrySet()) {
                    if (!DERBY_SYSTEM_HOME_PROPERTY_SUFFIX.equals(entry.getKey())) {
                        logger.debug("{}{}={}",DERBY_PROPERTIES_PREFIX, entry.getKey(),entry.getValue());
                        writer.println(new StringBuilder(DERBY_PROPERTIES_PREFIX).append(entry.getKey()).append('=').append(entry.getValue() == null ? "" : entry.getValue().toString()).toString());
                    }
                }
                writer.flush();
            }

        }

        logger.trace("Start networkServerControl");
        NetworkServerControl networkServerControl = new NetworkServerControl();
        networkServerControl.start(new PrintWriter(new LogOutputStream(logger, Level.INFO)));
        logger.info("Derby embeded server has been started.");
        return networkServerControl;
    }

}
