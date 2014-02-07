package com.groupon.novie.integrationtest.mapping;

import cucumber.api.java.en.Then;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains all mapping relative to a json response
 * Created by thomas on 15/01/2014.
 */
public class NovieJSONResponseImperative {

    private NovieIntegrationContext context;
    private JSONObject responseObject;

    private static Logger _log = LoggerFactory.getLogger(NovieJSONResponseImperative.class);

    public NovieJSONResponseImperative(NovieIntegrationContext context) {
        this.context = context;
        if (context.getHttpResponse().contentType.equals("application/json")) {
            try {
                responseObject = new JSONObject(context.getHttpResponse().content);
            } catch (JSONException e) {
                _log.error("Error when loading JSON response.", e);
                Assert.fail("The content type is not JSON");
            }
        } else {
            Assert.fail("The content type is not JSON");
        }
    }

    @Then("^verify, in json, that the total number of record is (\\d*)")
    public void verifyTotalNumberOfRecords(int expectedTotal) {
        try {
            Assert.assertEquals("Total number of records", expectedTotal, responseObject.getInt("total"));
        } catch (JSONException e) {
            _log.error("Error when manipulating JSON response.", e);
            Assert.fail("Error when manipulating JSON response.");
        }
    }
}
