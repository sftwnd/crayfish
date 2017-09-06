package com.github.sftwnd.crayfish.spring.feign.cloud.hastoken;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.http.impl.client.HttpClientBuilder;

public class HasTokenFeignRequestInterceptor implements RequestInterceptor {

    private static final String DEFAULT_TOKEN_HEADER_NAME = "authToken";

    private HasTokenHolder hasTokenHolder;
    private String tokenHeaderName;

    public HasTokenFeignRequestInterceptor(String uri, String username, String password, long refreshInterval, String headerName) {
        hasTokenHolder = new HasTokenHolder(HttpClientBuilder.create().build(), uri, username, password, refreshInterval);
        this.tokenHeaderName = headerName == null || headerName.trim().length() == 0 ? DEFAULT_TOKEN_HEADER_NAME : headerName.trim();
    }

    @Override
    public void apply(RequestTemplate template) {
        String token = hasTokenHolder.getToken();
        if (token != null && !token.trim().isEmpty()) {
            template.header(tokenHeaderName, token);
        }
    }

}
