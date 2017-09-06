package com.github.sftwnd.crayfish.spring.feign.cloud.hastoken;

import feign.sax.SAXDecoder;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by Andrey D. Shindarev on 05.08.2017.
 */
public class HasTokenHandler extends DefaultHandler implements SAXDecoder.ContentHandlerWithResult<HasTokenResponse> {

        private StringBuilder currentText = new StringBuilder();

        private HasTokenResponse hasTokenResponse;

        @Override
        public HasTokenResponse result() {
            return hasTokenResponse;
        }

        @Override
        @SuppressWarnings("fallthrough")
        public void endElement(String uri, String name, String qName) {
            if (hasTokenResponse == null) {
                this.hasTokenResponse = new HasTokenResponse();
            }
            switch (qName) {
                case ("SESSION_ID")    : this.hasTokenResponse.setToken(currentText.toString().trim());
                                         break;
                case ("ERROR_ID")      : this.hasTokenResponse.setErrorCode(Integer.parseInt(currentText.toString().trim()));
                                         break;
                case ("ERROR_MESSAGE") : this.hasTokenResponse.setErrorMessage(currentText.toString().trim());
                               default :
            }
            currentText = new StringBuilder();
        }

        @Override
        public void characters(char ch[], int start, int length) {
            currentText.append(ch, start, length);
        }

}
