package com.sftwnd.crayfish.spring.feign.cloud.hastoken;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Created by ashindarev on 05.08.2017.
 */
public class HasTokenResponse {

    private String  token;
    private Integer errorCode;
    private String  errorMessage;

    public HasTokenResponse() {
        super();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public synchronized void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return this.errorCode == null
            && this.token != null;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(HasTokenResponse.class, ToStringStyle.JSON_STYLE)
                           .append("token", getToken())
                           .append("errorCode", getErrorCode())
                           .append("errorMessage", getErrorMessage())
                         .toString();
    }

}
