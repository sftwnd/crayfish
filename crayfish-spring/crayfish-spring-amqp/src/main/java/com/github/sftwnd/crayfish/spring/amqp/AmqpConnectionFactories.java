package com.github.sftwnd.crayfish.spring.amqp;

import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ashindarev on 05.08.16.
 */
@Configuration(value = "crayfish-spring-amqp")
@Profile(value = {"crayfish-defaults", "crayfish-spring-amqp"})
@ConfigurationProperties(prefix = "com.github.sftwnd.crayfish", ignoreNestedProperties=false)
public class AmqpConnectionFactories implements BeanFactoryAware {

    private static final Logger logger = LoggerFactory.getLogger(AmqpConnectionFactories.class);
    public static final String AMQP_BEAN_PREFIX = "crayfish-amqp";
    private static final Pattern AMQP_BEAN_PATTERN = Pattern.compile(new StringBuilder(AMQP_BEAN_PREFIX).append("\\.(.+)").toString());

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private BeanFactory beanFactory;

    public Map<String, ConnectionFactory> connectionFactories = new HashMap<>();

    public Map<String, ConnectionFactory> getAmqp() {
        return this.connectionFactories;
    }

    @PostConstruct
    public void configure() {
        logger.info("Amqp connection factories has been registered: {}", String.valueOf(connectionFactories.keySet()));
        Assert.state(beanFactory instanceof ConfigurableBeanFactory, "wrong bean factory type");
        ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
        for (Map.Entry<String, ConnectionFactory> entry : connectionFactories.entrySet()) {
            if (logger.isTraceEnabled()) {
                logger.trace("Register named amqp connection: {}[{}], clientProperties: {}", AMQP_BEAN_PREFIX, entry.getKey(), entry.getValue().getClientProperties());
            } else {
                logger.debug("Register named amqp connection: {}[{}]", AMQP_BEAN_PREFIX, entry.getKey());
            }
            entry.getValue().setTopologyRecoveryEnabled(true);
            entry.getValue().setAutomaticRecoveryEnabled(true);
            if (applicationContext.containsBean(getBeanName(entry.getKey()))) {
                logger.trace("Bean `{}` already exists.", getBeanName(entry.getKey()));
            } else {
                configurableBeanFactory.registerSingleton(getBeanName(entry.getKey()), entry.getValue());
                logger.trace("Bean `{}` has been created.", getBeanName(entry.getKey()));

            }
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private static String getBeanName(String name) {
        return new StringBuilder(AMQP_BEAN_PREFIX).append('.').append(name).toString();
    }

    public static String getName(String beanName) {
        Matcher matcher = AMQP_BEAN_PATTERN.matcher(beanName);
        return matcher.matches() ? matcher.group(1) : beanName;
    }

}
