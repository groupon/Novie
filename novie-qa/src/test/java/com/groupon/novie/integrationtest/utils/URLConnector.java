package com.groupon.novie.integrationtest.utils;

import gherkin.formatter.model.DataTableRow;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * Created by thomas on 09/01/2014.
 */
public class URLConnector {

    public static class HTTPResponse {
        public int code;
        public String errorMessage;
        public String contentType;
        public String content;

        public HTTPResponse(int code, String errorMsg, String contentType, String content) {
            this.code = code;
            this.errorMessage = errorMsg;
            this.contentType = contentType;
            this.content = content;
        }
    }

    private static Logger _log = LoggerFactory.getLogger(URLConnector.class);

    /**
     * Execute an url request. Similar to hit an end point with browser.
     * Retrurn the response messsage (html), null if the execution fail.
     *
     * @param url
     * @param headersParam
     * @return String
     */
    public static HTTPResponse httpCall(String url, Map<String, String> headersParam, List<DataTableRow> queryParam) {
        try {
            StringBuilder queryBuilder = null;
            if (queryParam != null) {
                queryBuilder = new StringBuilder();
                for (DataTableRow row : queryParam) {
                    if (row.getCells().size() == 2) {
                        if (queryBuilder.length() > 0) {
                            queryBuilder.append("&");
                        }
                        queryBuilder.append(row.getCells().get(0));
                        queryBuilder.append("=");
                        queryBuilder.append(URLEncoder.encode(row.getCells().get(1), "UTF-8"));
                        _log.debug("Add queryParameter {}={}", row.getCells().get(0), row.getCells().get(1));
                    } else {
                        Assert.fail("The query parameter row hasn't two columns.");
                    }
                }

            }


            URL target;
            if (queryBuilder == null) {
                target = new URL(url);
            } else {
                target = new URL(url + "?" + queryBuilder.toString());
            }
            _log.info("Build HttpURLConnection for {}.", target.toExternalForm());


            HttpURLConnection conn = (HttpURLConnection) target
                    .openConnection();
            _log.debug("Set up header informations");
            if (null != headersParam) {
                for (String key : headersParam.keySet()) {
                    conn.addRequestProperty(key, headersParam.get(key));
                    _log.info(String.format("Add header | %s:%s", key, headersParam.get(key)));
                }
            }
            _log.info("Request URL: " + url);
            String contentString = "";
            if (conn.getResponseCode() == 200 || conn.getResponseCode() == 201) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                contentString = sb.toString();
            }

            String errorString = "";
            if (conn.getErrorStream() != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                errorString = sb.toString();
            }

            HTTPResponse returnValue = new HTTPResponse(conn.getResponseCode(), errorString, conn.getContentType(), contentString);
            _log.info("Response Code = " + returnValue.code);
            return returnValue;
        } catch (Exception e) {
            _log.error("Error while connect url {}.", url, e);
            Assert.fail();
        }
        return null;
    }


}
