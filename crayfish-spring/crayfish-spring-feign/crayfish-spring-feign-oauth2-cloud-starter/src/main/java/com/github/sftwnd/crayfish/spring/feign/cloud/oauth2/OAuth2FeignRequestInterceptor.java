package com.github.sftwnd.crayfish.spring.feign.cloud.oauth2;

import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.Assert;

public class OAuth2FeignRequestInterceptor extends org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String BEARER_TOKEN_TYPE = "Bearer";

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2FeignRequestInterceptor.class);

    private String authorizationHeader;
    private String tokenFormat;
    private String tokenType;
    private OAuth2ClientContext oauth2ClientContext;

    public OAuth2FeignRequestInterceptor(
            OAuth2ClientContext oauth2ClientContext
           ,OAuth2ProtectedResourceDetails oAuth2ProtectedResourceDetails
           ,String authorizationHeader, String tokenType )
    {
        super(oauth2ClientContext, oAuth2ProtectedResourceDetails);
        Assert.notNull(oauth2ClientContext, "Context can not be null");
        this.oauth2ClientContext = oauth2ClientContext;
        this.authorizationHeader = authorizationHeader == null || authorizationHeader.trim().length() == 0 ? "" : authorizationHeader.trim();
        this.tokenType           = tokenType == null ? "" : tokenType.trim();
        this.tokenFormat         = new StringBuilder(this.tokenType.equals("*") ? "%s" : this.tokenType).append(" %s").toString().trim();
    }

    @Override
    public void apply(RequestTemplate template) {

        if (template.headers().containsKey(authorizationHeader)) {
            LOGGER.warn("The Authorization token header {} has been already set", authorizationHeader);
        } else {
            OAuth2AccessToken accessToken = getToken();
            if (accessToken == null) {
                LOGGER.warn("Can not obtain existing token for request, if it is a non secured request, ignore.");
            } else {
                LOGGER.debug("Constructing Header: `{}` for Token type: `{}`", authorizationHeader, accessToken.getTokenType());
                if (tokenType.equals("*")) {
                    template.header(authorizationHeader, String.format(tokenFormat, accessToken.getTokenType(), accessToken.getValue()));
                } else {
                    template.header(authorizationHeader, String.format(tokenFormat, accessToken.getValue()));
                }
            }
        }

    }


}
