package com.groupon.novie.integrationtest.mapping;

import cucumber.api.java.en.Then;
import org.junit.Assert;

/**
 * Created by thomas on 09/01/2014.
 */
public class NovieHttpResponseImperative {

    private NovieIntegrationContext context;

    public NovieHttpResponseImperative(NovieIntegrationContext declarative) {
        this.context = declarative;
    }

    @Then("^verify http code is (\\d*)")
    public void verifyHTTPCode(int expectedCode) {
        Assert.assertEquals("HTTP response code", expectedCode, context.getHttpResponse().code);
    }

    @Then("^verify http error message is \"(.*)\"")
    public void verifyHTTPMsg(String expectedMsg) {
        Assert.assertEquals("HTTP error message", expectedMsg, context.getHttpResponse().errorMessage);
    }

    @Then("^verify that the Content-Type is \"(.*)\"")
    public void verifyContentType(String expectedContentType) {
        Assert.assertEquals("Response content-type ", expectedContentType, context.getHttpResponse().contentType);
    }
}
