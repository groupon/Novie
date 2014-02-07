package com.groupon.novie.integrationtest.mapping;

import com.groupon.novie.integrationtest.utils.URLConnector;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import gherkin.formatter.model.DataTableRow;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomas on 09/01/2014.
 */
public class NovieRequestDeclarative {

    private static String novieLocalUrl = "http://localhost:9090/novie/";
    private static String applicationEndPoint = "app";

    private static Logger _log = LoggerFactory.getLogger(NovieRequestDeclarative.class);

    private NovieIntegrationContext context;


    public NovieRequestDeclarative(NovieIntegrationContext context) {
        this.context = context;
    }

    @Given("^a user request Novie with a not existing endpoint$")
    public void executeHTTPRequestBadEndpoint() {
        context.setHttpResponse(URLConnector.httpCall(novieLocalUrl + "notExisting", null, null));
    }

    @Given("^a user request Novie with no parameter$")
    public void executeHTTPRequestEmptyParam() {
        executeHTTPRequest(null);
    }


    @Given("^a user request Novie with following parameters:$")
    public void executeHTTPRequest(DataTable table) {
        executeHTTPRequest(null, table);
    }

    /**
     * Expected a table of two column, first is the paramName, second is the paramValue
     *
     * @param table
     */
    @Given("^a user request Novie in \"(.*)\" with following parameters:$")
    public void executeHTTPRequest(String format, DataTable table) {
        if (format != null && !format.toUpperCase().equals("JSON")
                && !format.toUpperCase().equals("XML")
                && !format.toUpperCase().equals("CSV")) {
            Assert.fail("Only json,xml & csv are supported.");
        }
        if (format == null) {
            _log.info("Format not specified use JSON as default.");
            format = "json";
        }
        context.setResponseType(NovieIntegrationContext.RequestedResponseType.valueOf(format.toUpperCase()));
        if (table != null) {
            List<DataTableRow> rows = table.getGherkinRows();
            //Remove the column headers.
            List<DataTableRow> parameters = new ArrayList<DataTableRow>();
            for (int i = 1; i < rows.size(); i++) {
                parameters.add(rows.get(i));
            }
            context.setHttpResponse(URLConnector.httpCall(novieLocalUrl + applicationEndPoint + (format == null ? "" : ("." + format)), null, parameters));
        } else {
            context.setHttpResponse(URLConnector.httpCall(novieLocalUrl + applicationEndPoint, null, null));
        }
    }


}
