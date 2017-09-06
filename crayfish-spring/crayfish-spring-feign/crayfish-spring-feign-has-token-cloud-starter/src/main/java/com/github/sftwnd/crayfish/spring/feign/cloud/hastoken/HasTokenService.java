package com.github.sftwnd.crayfish.spring.feign.cloud.hastoken;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

/**
 * Created by Andrey D. Shindarev on 05.08.2017.
 */
@Headers( value = {"Accept: application/xml", "charset: utf-8"} )
public interface HasTokenService {

      @RequestLine("GET ?login={login}&password={password}")
      HasTokenResponse requestToken(@Param("login") String login, @Param("password") String password);

}
