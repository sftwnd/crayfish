package com.github.sftwnd.crayfish.spring.feign.cloud.oauth2;

import feign.Feign;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;

@Configuration
@ConditionalOnClass({ Feign.class, OAuth2FeignRequestInterceptor.class })
@ConditionalOnProperty(value = "com.github.sftwnd.crayfish.spring.feign.cloud.authorization", havingValue = "oauth2", matchIfMissing = false)
public class OAuth2FeignAutoConfiguration {

    @Bean
    public RequestInterceptor oauth2FeignRequestInterceptor(
                                   @Value("${com.github.sftwnd.crayfish.spring.feign.cloud.authorization.oauth2.authorizationHeader:Authorization}") String authorizationHeader
                                  ,@Value("${com.github.sftwnd.crayfish.spring.feign.cloud.authorization.oauth2.tokenType:*}")                       String tokenType
                                  ,@Value("${com.github.sftwnd.crayfish.spring.feign.cloud.authorization.oauth2.uri:}")                              String uri
                                  ,@Value("${com.github.sftwnd.crayfish.spring.feign.cloud.authorization.oauth2.username:}")                         String username
                                  ,@Value("${com.github.sftwnd.crayfish.spring.feign.cloud.authorization.oauth2.password:}")                         String password
                                  ,@Value("${com.github.sftwnd.crayfish.spring.feign.cloud.authorization.oauth2.clientId:}")                         String clientId
                                  ,@Value("${com.github.sftwnd.crayfish.spring.feign.cloud.authorization.oauth2.clientSecret:}")                     String clientSecret
                              )
    {
        return new OAuth2FeignRequestInterceptor(
                       new DefaultOAuth2ClientContext()
                      ,getOAuth2ProtectedResourceDetails(uri, username, password, clientId, clientSecret)
                      ,authorizationHeader
                      ,tokenType
                   );
    }

    private OAuth2ProtectedResourceDetails getOAuth2ProtectedResourceDetails (
                String uri, String username, String password, String clientId, String clientSecret
           )
    {
            ResourceOwnerPasswordResourceDetails resourceDetails = new ResourceOwnerPasswordResourceDetails();
            if (uri != null && !uri.trim().isEmpty()) {
                resourceDetails.setAccessTokenUri(uri.trim());
            }
            if (username != null && !username.trim().isEmpty()) {
                resourceDetails.setUsername(username.trim());
            }
            if (password != null) {
                resourceDetails.setPassword(password);
            }
            if (clientId != null && !clientId.trim().isEmpty()) {
                resourceDetails.setClientId(clientId.trim());
            }
            if (clientSecret != null) {
                resourceDetails.setClientSecret(clientSecret);
            }
            return resourceDetails;
    }


}