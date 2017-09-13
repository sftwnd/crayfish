package com.github.sftwnd.crayfish.spring.datasource.ucp;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Created by ashindarev on 21.02.17.
 */
@Configuration
@ConfigurationProperties(prefix = "spring.datasource", ignoreNestedProperties=false)
@ConditionalOnProperty(prefix = "spring.datasource.ucp", name = "u-r-l")
public class UcpSpringDatasourceFactoryConfiguration extends UcpDatasourceFactoryConfiguration {

}
