package com.github.sftwnd.crayfish.spring.feign.cloud.trustedchannel;

import feign.Feign;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({ Feign.class, TrustedChannelFeignRequestInterceptor.class })
@ConditionalOnProperty(value = "com.github.sftwnd.crayfish.spring.feign.cloud.authorization", havingValue = "trusted-channel", matchIfMissing = false)
public class TrustedChannelFeignAutoConfiguration {

    @Bean
    public RequestInterceptor trustedChannelFeignRequestInterceptor(
                @Value("${com.github.sftwnd.crayfish.spring.feign.cloud.authorization.trusted-channel.login:}")    String login
               ,@Value("${com.github.sftwnd.crayfish.spring.feign.cloud.authorization.trusted-channel.applCode:}") String applCode
           )
    {
        return new TrustedChannelFeignRequestInterceptor(login, applCode);
    }

}