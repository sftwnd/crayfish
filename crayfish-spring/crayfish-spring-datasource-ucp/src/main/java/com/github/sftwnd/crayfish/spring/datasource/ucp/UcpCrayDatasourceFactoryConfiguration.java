package com.github.sftwnd.crayfish.spring.datasource.ucp;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by ashindarev on 21.02.17.
 */
@Configuration
@ConfigurationProperties(prefix = "crayfish.datasource")
@ConditionalOnProperty(prefix = "crayfish.datasource.ucp", name = "u-r-l")
public class UcpCrayDatasourceFactoryConfiguration extends UcpDatasourceFactoryConfiguration {

}
