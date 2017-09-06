package com.github.sftwnd.crayfish.spring.feign.cloud.hastoken;

import feign.*;
import feign.codec.ErrorDecoder;
import feign.httpclient.ApacheHttpClient;
import feign.sax.SAXDecoder;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by Andrey D. Shindarev on 05.08.2017.
 */
public class HasTokenHolder implements ErrorDecoder, Retryer, RequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(HasTokenResponse.class);

    private int priority = 1000;

    private HasTokenService hasTokenService;
    private          String             login;
    private          String             password;
    private volatile String             token;
    private final    long               refreshInterval;
    public           ErrorDecoder       errorDecoder;
    private          long               expireAt = 0L;
    private          Retryer            retryer;
    private          boolean            active = false;

    public HasTokenHolder(HttpClient httpClient, String url, String login, String password, long refreshInterval) {
        this.refreshInterval = refreshInterval > 0L ? refreshInterval : 0L;
        if ( url != null && login != null && password != null &&
             !url.isEmpty() && !login.isEmpty() && !password.isEmpty()
           ) {
            this.hasTokenService = Feign.builder()
                    .decoder(SAXDecoder.builder().registerContentHandler(HasTokenHandler.class).build())
                    .client(new ApacheHttpClient(httpClient))
                    .target(HasTokenService.class, url);
            this.login = login;
            this.password = password;
            this.errorDecoder = new ErrorDecoder.Default() {
                @Override
                public Exception decode(String methodKey, Response response) {
                    if (response.status() == 401) {
                        if (response != null && response.request() != null &&
                                response.request().headers() != null && response.request().headers().containsKey("authToken")) {
                            invalidate(String.valueOf(response.request().headers().get("authToken")));
                        }
                        return new RetryableException(response.toString(), new Date());
                    } else {
                        return super.decode(methodKey, response);
                    }
                }
            };
            retryer = new Retryer.Default(20L, 100L, 2);
            active = true;
        }
    }

    public String getToken() {
        String result = token;
        if (result == null || (this.refreshInterval > 0L && System.currentTimeMillis() >= this.expireAt) ) {
            synchronized (this) {
                if (this.token == null || System.currentTimeMillis() >= this.expireAt) {
                    this.token = null;
                    try {
                        HasTokenResponse response = hasTokenService.requestToken(login, password);
                        if (response != null && response.isValid()) {
                            this.token = response.getToken();
                            // Принудительно раз в 15 минут перезапрашиваем токен
                            if (refreshInterval > 0) {
                                this.expireAt = System.currentTimeMillis() + refreshInterval;
                            }
                            logger.debug("New token has been received: {}.",
                                    logger.isTraceEnabled() ? response.getToken() : new StringBuilder(response.getToken().substring(0,8)).append("..").toString()
                            );
                        } else {
                            logger.error("Unable to get token. Response: {}", response);
                        }
                    } catch (Exception ex) {
                        logger.error("Unable to request token for login: {}", login);
                        throw ex;
                    }

                }
                result = this.token;
            }
        }
        return result;
    }

    // Если задали в качестве токена null или текущий совпадает с переданным, то инфалидируем токен (синхронный метод)
    public void invalidate(String token) {
        if (token != null && !token.equals(this.token)) {
            return;
        }
        synchronized (this) {
            if (token == null || token.equals(this.token)) {
                this.token = null;
            }
        }
    }

    public void apply(RequestTemplate template) {
        String token = this.getToken();
        if (token != null && !token.trim().isEmpty()) {
            template.header("authToken", token);
        }
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        return errorDecoder == null ? null : errorDecoder.decode(methodKey, response);
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        if (retryer != null) {
            retryer.continueOrPropagate(e);
        }
    }

    @Override
    public Retryer clone() {
        return retryer == null ? null : retryer.clone();
    }


}
