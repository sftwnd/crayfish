package com.github.sftwnd.crayfish.spring.amqp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by ashindarev on 05.08.16.
 */
@Configuration(value = "crayfish-spring-amqp")
@ConfigurationProperties(prefix = "com.github.sftwnd.crayfish", ignoreNestedProperties=false)
public class AmqpConnectionFactories extends AmqpConnectionFactoriesConfiguration {

}
