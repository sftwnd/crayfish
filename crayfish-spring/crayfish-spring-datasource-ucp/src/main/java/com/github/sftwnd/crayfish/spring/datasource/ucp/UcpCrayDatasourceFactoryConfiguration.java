package com.github.sftwnd.crayfish.spring.datasource.ucp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Created by ashindarev on 21.02.17.
 */
@Configuration
//@ConditionalOnClass(PoolDataSourceImpl.class)
@ConfigurationProperties(prefix = "cray.datasource", ignoreNestedProperties=false)
@Profile(value = "crayfish-datasource-ucp")
public class UcpCrayDatasourceFactoryConfiguration extends UcpDatasourceFactoryConfiguration {

}
