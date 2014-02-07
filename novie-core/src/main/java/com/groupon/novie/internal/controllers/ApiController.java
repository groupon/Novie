/*
Copyright (c) 2013, Groupon, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

Neither the name of GROUPON nor the names of its contributors may be
used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.groupon.novie.internal.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.groupon.novie.SchemaDefinition;
import com.groupon.novie.internal.GenericSchemaService;
import com.groupon.novie.internal.SchemaDefinitionRepository;
import com.groupon.novie.internal.exception.InvalidParameterException;
import com.groupon.novie.internal.exception.InvalidSchemaException;
import com.groupon.novie.internal.exception.ServiceException;
import com.groupon.novie.internal.response.Report;
import com.groupon.novie.internal.response.ReportRecord;
import com.groupon.novie.internal.validation.HttpQueryConstraint;
import com.groupon.novie.internal.validation.PageNumberConstraint;
import com.groupon.novie.internal.validation.PageSizeConstraint;
import com.groupon.novie.internal.validation.QueryParameterAware;
import com.groupon.novie.internal.validation.QueryParameterEnvelope;
import com.groupon.novie.internal.validation.SystemParameter;

/**
 * <p>
 * Generic Controller with the purpose of dispatching every request to the
 * desired {@link SchemaDefinition} implementation.
 * </p>
 * <p>
 * The controller performs runtime validation of parameters based on the config
 * parameter.
 * </p>
 * <p>
 * The controller has one handler for each of the following Accept Headers:
 * <ul>
 * <li>application/json</li>
 * <li>application/xml</li>
 * <li>text/csv</li>
 * </ul>
 * </p>
 *
 * @author ricardo
 * @author damiano
 * @author thomas
 * @since July 1, 2013
 */
@Controller
@RequestMapping(method = RequestMethod.GET)
public class ApiController {

    /**
     * <p>
     * Repository of available Schemas.
     * </p>
     * <p/>
     * <p>
     * A repository is used as the schema is selected at runtime.
     * </p>
     */
    @Autowired
    private SchemaDefinitionRepository schemaRepository;

    /**
     * <p>
     * Generic Schema Service that will call a SQL Generation Engine, based on
     * supplied constraints, and aggregate the result.
     * </p>
     */
    @Autowired
    private GenericSchemaService schemaService;

    /**
     * <p>
     * Logger object for ApiController Class.
     * </p>
     */
    private static final Logger LOG = LoggerFactory.getLogger(ApiController.class);

    /**
     * <p>
     * List of mandatory domain specific parameters.
     * </p>
     * <p/>
     * <p>
     * Avoids using <a href="http://en.wikipedia.org/wiki/Magic_number_(programming)">magic numbers</a>.
     * </p>
     */
    private static final Set<String> DOMAIN_PARAMETERS;

    /**
     * <p/>
     * Specific mandatory field for every Star Schema.
     * <p/>
     */
    private static final String[] MANDATORY_FIELDS = {"group"};

    static {
        DOMAIN_PARAMETERS = new HashSet<String>();
        DOMAIN_PARAMETERS.add("group");
        DOMAIN_PARAMETERS.add("sort");
        DOMAIN_PARAMETERS.add("timezone");
        DOMAIN_PARAMETERS.add("pageSize");
        DOMAIN_PARAMETERS.add("page");
        DOMAIN_PARAMETERS.add("pageKey");
    }

    /**
     * <p>
     * Handles requests that accept application/json.
     * </p>
     */
    @RequestMapping(value = {"/{schema:[^\\.]+}", "/{schema}.json"}, produces = "application/json", headers = {"Accept=application/json"})
    @ResponseBody
    public Report processJsonRequest(@PathVariable String schema, HttpServletRequest request) throws ServiceException, InvalidParameterException,
            InvalidSchemaException {
        LOG.info("Processing JSON request for parameters " + request.getQueryString());
        return processRequest(schema, request);
    }

    /**
     * <p>
     * Handles requests that accept application/xml.
     * </p>
     */
    @RequestMapping(value = {"/{schema:[^\\.]+}", "/{schema}.xml"}, produces = "application/xml", headers = {"Accept=application/xml, text/xml"})
    @ResponseBody
    public Report processXmlRequest(@PathVariable String schema, HttpServletRequest request) throws ServiceException, InvalidParameterException,
            InvalidSchemaException {
        LOG.info("Processing XML request for parameters " + request.getQueryString());
        return processRequest(schema, request);
    }

    /**
     * <p>
     * Handles requests that accept text/csv.
     * </p>
     */
    @RequestMapping(value = {"/{schema}:[^\\.]+", "/{schema}.csv"}, produces = "text/csv", headers = {"Accept=text/csv"})
    @ResponseBody
    public
    Report processCsvRequest(@PathVariable String schema, HttpServletRequest request) throws ServiceException, InvalidParameterException,
            InvalidSchemaException {
        LOG.info("Processing CSV request for parameters " + request.getQueryString());
        return processRequest(schema, request);
    }

    /**
     * <p>
     * Generic processor of all types of requests.
     * </p>
     * <p>
     * Validates schema availability, mandatory fields for the specified schema
     * and creates chains of {@link QueryParameterAware} to be processed by the
     * underlying {@link GenericSchemaService}.
     * </p>
     *
     * @param schema  Schema from where to extract the information.
     * @param request Request in generic form so the parameters can be dynamically
     *                extract.
     *
     * @return Returns a {@link Report} Object delivered by the {@link GenericSchemaService}.
     *
     * @throws ServiceException          Exception generated by the underlying Service.
     * @throws InvalidParameterException Exception generated by the controller due to an invalid
     *                                   parameter for the chosen schema.
     * @throws InvalidSchemaException    Exception generated by the controller when an invalid schema
     *                                   is requested.
     */
    @SuppressWarnings("unchecked")
    private Report processRequest(String schema, HttpServletRequest request) throws ServiceException, InvalidParameterException,
            InvalidSchemaException {

        long begin = System.currentTimeMillis();

        SchemaDefinition config = schemaRepository.getStarSchemaConfig(schema);
        if (config == null) {
            LOG.warn("Invalid endpoint \"" + schema + "\"");
            throw new InvalidSchemaException("Invalid endpoint");
        }

        Map<String, String[]> params = request.getParameterMap();

        for (String mandatory : MANDATORY_FIELDS) {
            if (!params.containsKey(mandatory)) {
                // mandatory fields are case sensitive
                LOG.error("Mandatory field \"" + mandatory + "\" missing");
                throw new InvalidParameterException("Missing mandatory field " + mandatory);
            }
        }

        QueryParameterEnvelope queryParameterDto = new QueryParameterEnvelope();

        for (String key : params.keySet()) {

            if (DOMAIN_PARAMETERS.contains(key)) {
                String domainParameter = params.get(key)[0];
                final SystemParameter systemParameter = SystemParameter.valueOf(key);
                systemParameter.parseValue(domainParameter, config, queryParameterDto);

            } else {

                Pair<String, String> pair = splitKey(key);
                if (!config.isValidDimension(pair.getLeft(), pair.getRight())) {
                    LOG.warn("Invalid parameter \"" + key + "\" for endpoint \"" + schema + "\".");
                    throw new InvalidParameterException("Invalid parameter " + key + " for endpoint " + schema + "");
                }

                String[] multiParam = request.getParameterValues(key);
                for (String param : multiParam) {
                    queryParameterDto.addConstraints(new HttpQueryConstraint(config, key, param));
                }
            }
        }
        final Report report = schemaService.generateReport(config, queryParameterDto);
        report.setRecords(inMemoryPagination(report.getRecords(), queryParameterDto));
        if (LOG.isInfoEnabled()) {
            LOG.info("Query successfully processed for schema " + schema + " in " + (System.currentTimeMillis() - begin) + "s.");
        }
        return report;
    }

    /**
     * <p>
     * Handles {@link ServiceException} generated by the underlying layer.
     * </p>
     *
     * @param exception of type {@link ServiceException}
     *
     * @return A {@link ResponseEntity} with the {@link HttpStatus#INTERNAL_SERVER_ERROR}
     *         code and a message describing the error itself.
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public ResponseEntity<String> handleServiceException(ServiceException exception) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<String>(exception.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * <p>
     * Handles {@link InvalidParameterException} generated by the controller
     * itself when receives an invalid parameter.
     * </p>
     *
     * @param exception of type {@link InvalidParameterException}
     * @return A {@link ResponseEntity} with the {@link HttpStatus#BAD_REQUEST}
     * code and a message describing the error itself.
     */
    @ExceptionHandler(InvalidParameterException.class)
    @ResponseBody
    public ResponseEntity<String> handleControllerException(InvalidParameterException exception) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<String>(exception.getMessage(), headers, HttpStatus.BAD_REQUEST);
    }

    /**
     * <p>
     * Handles {@link InvalidSchemaException} generated by the controller itself
     * when receives a request to a non existing schema configuration.
     * </p>
     *
     * @param exception of type {@link InvalidSchemaException}
     * @return A {@link ResponseEntity} with the {@link HttpStatus#NOT_FOUND}
     * code and a message describing the error itself.
     */
    @ExceptionHandler(InvalidSchemaException.class)
    @ResponseBody
    public ResponseEntity<String> handleInvalidSchemaException(InvalidSchemaException exception) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<String>(exception.getMessage(), headers, HttpStatus.NOT_FOUND);
    }

    /**
     * <p>
     * Creates a {@link Pair} composed of Dimension and Information, required by
     * the {@link GenericSchemaService}.
     * </p>
     *
     * @param key
     * @return
     */
    private static Pair<String, String> splitKey(String key) {
        String dimension = null;
        String info = null;

        if (!key.contains(".")) {
            dimension = key;
        } else {
            dimension = key.substring(0, key.indexOf('.'));
            info = key.substring(key.indexOf('.') + 1, key.length());
        }
        return Pair.of(dimension, info);
    }

    /**
     * TODO to be removed
     *
     * @param recordList
     * @param queryParameterEnvelope
     * @return
     */
    private List<ReportRecord> inMemoryPagination(final List<ReportRecord> recordList, QueryParameterEnvelope queryParameterEnvelope) {
        final QueryParameterAware pageSize = queryParameterEnvelope.getPageSize();
        final QueryParameterAware pageNumber = queryParameterEnvelope.getPageNumber();
        if (pageSize != null && pageNumber != null) {
            final Integer pageSizeValue = ((PageSizeConstraint) pageSize).getPageSizeValue();
            final Integer pageValue = ((PageNumberConstraint) pageNumber).getPageNumberValue() - 1;
            int offset = pageValue * pageSizeValue;
            return recordList.subList(offset > recordList.size() ? recordList.size() : offset, offset + pageSizeValue > recordList.size()
                    ? recordList.size() : offset + pageSizeValue);
        }
        return recordList;
    }
}
