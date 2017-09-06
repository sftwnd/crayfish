package com.github.sftwnd.crayfish.spring.feign.cloud.trustedchannel;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrustedChannelFeignRequestInterceptor implements RequestInterceptor {

    private static final String LOGIN_PARAMETER     = "LOGIN";
    private static final String APPL_CODE_PARAMETER = "APPL_CODE";

    private static final Logger LOGGER = LoggerFactory.getLogger(TrustedChannelFeignRequestInterceptor.class);

    // TODO: Подумать над возможностью модификации в дальнейшем
    private final String login;
    private final String applCode;

    public TrustedChannelFeignRequestInterceptor(
            String login
           ,String applCode )
    {
        this.login = login == null || login.trim().length() == 0 ? null : login.trim();
        this.applCode = applCode == null || applCode.trim().length() == 0 ? null : applCode.trim();
    }

    @Override
    public void apply(RequestTemplate template) {

        if (login != null) {
            template.query(LOGIN_PARAMETER, login);
        }
        if (applCode != null) {
            template.query(APPL_CODE_PARAMETER, applCode);
        }

    }


}
