package com.sftwnd.crayfish.spring.feign.cloud.hastoken;

import feign.Feign;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({ Feign.class, HasTokenFeignRequestInterceptor.class })
@ConditionalOnProperty(value = "com.sftwnd.crayfish.spring.feign.cloud.authorization", havingValue = "has-token", matchIfMissing = false)
public class HasTokenFeignAutoConfiguration {

    @Bean
    public RequestInterceptor hasTokenRequestInterceptor(
                                   @Value("${com.sftwnd.crayfish.spring.feign.cloud.authorization.has-token.uri:}")              String uri
                                  ,@Value("${com.sftwnd.crayfish.spring.feign.cloud.authorization.has-token.username:}")         String username
                                  ,@Value("${com.sftwnd.crayfish.spring.feign.cloud.authorization.has-token.password:}")         String password
                                  ,@Value("${com.sftwnd.crayfish.spring.feign.cloud.authorization.has-token.refreshInterval:0}") long   refreshInterval
                                  ,@Value("${com.sftwnd.crayfish.spring.feign.cloud.authorization.has-token.headerName:}")       String headerName
                              )
    {
        return new HasTokenFeignRequestInterceptor( uri, username, password, refreshInterval, headerName );
    }

}