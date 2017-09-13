package com.github.sftwnd.crayfish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicReference;

/**
 *  Класс, используемый для запуска приложений
 */
@SpringBootApplication
// @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ComponentScan
@EnableAutoConfiguration
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {

        String hostName = null;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
            System.setProperty("crayfish-hostname", hostName);
            logger.debug("HostName: {}", hostName);
        } catch (UnknownHostException e) {
            logger.trace("Unable to identify hostname by cause: {}", e.getLocalizedMessage());
        }

        if (hostName != null) {
            try {
                InetAddress[] inetAddresses = InetAddress.getAllByName(hostName);
                if (inetAddresses != null && inetAddresses.length > 0) {
                    StringBuilder sb = new StringBuilder("");
                    String symbol = "[";
                    for (InetAddress ipAddress : inetAddresses) {
                        sb.append(symbol).append(ipAddress.toString());
                    }
                    sb.append(']');
                    System.setProperty("crayfish-ipaddresses", sb.toString());
                    logger.debug("IP Adresses: {}", sb.toString());
                }
            } catch (UnknownHostException e) {
                logger.trace("Unable to identify ip addresses by cause: {}", e.getLocalizedMessage());
            }
        }

        SpringApplication.run(Application.class, args);
        logger.debug("Application has been started.");

    }

}
