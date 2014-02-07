package com.groupon.novie.integrationtest.mapping;

import com.groupon.novie.integrationtest.utils.URLConnector;

/**
 * This class represents the context foe each senario.
 * <p/>
 * Created by thomas on 20/01/2014.
 */
public class NovieIntegrationContext {

    public static enum RequestedResponseType {
        JSON, XML, CSV;
    }

    ;

    private RequestedResponseType responseType;

    private URLConnector.HTTPResponse httpResponse;

    public RequestedResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(RequestedResponseType responseType) {
        this.responseType = responseType;
    }

    public URLConnector.HTTPResponse getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(URLConnector.HTTPResponse httpResponse) {
        this.httpResponse = httpResponse;
    }
}
