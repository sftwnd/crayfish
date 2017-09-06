package com.github.sftwnd.crayfish.spring.datasource.ucp;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.springframework.context.annotation.Bean;

/**
 * Created by ashindarev on 21.02.17.
 */
public class UcpDatasourceFactoryConfiguration {

    @Bean(name = "dataSource")
    public PoolDataSource getUcp() {
        return PoolDataSourceFactory.getPoolDataSource();
    }

}
