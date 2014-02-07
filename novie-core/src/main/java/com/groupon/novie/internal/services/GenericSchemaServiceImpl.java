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
package com.groupon.novie.internal.services;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.groupon.novie.SchemaDefinition;
import com.groupon.novie.internal.GenericSchemaService;
import com.groupon.novie.internal.engine.QueryParameter;
import com.groupon.novie.internal.engine.builder.SqlQueryBuilder;
import com.groupon.novie.internal.engine.constraint.RawConstraint;
import com.groupon.novie.internal.exception.InvalidParameterException;
import com.groupon.novie.internal.exception.ServiceException;
import com.groupon.novie.internal.response.Report;
import com.groupon.novie.internal.response.ReportMeasure;
import com.groupon.novie.internal.response.ReportRecord;
import com.groupon.novie.internal.validation.QueryParameterAware;
import com.groupon.novie.internal.validation.QueryParameterEnvelope;

/**
 * This is the implementation of the service in charge of generating the report
 * by calling the internal engine - {@link SqlQueryBuilder} - through
 * {@link SqlQueryEngine}.
 *
 * @author ricardo
 * @author thomas
 */

@Service
public class GenericSchemaServiceImpl implements GenericSchemaService {

    private static final Logger LOG = LoggerFactory.getLogger(GenericSchemaServiceImpl.class);

    @Autowired
    private SqlQueryEngine sqlQueryEngine;

    @Override
    public Report generateReport(SchemaDefinition config, QueryParameterEnvelope parametersEnvelope) throws ServiceException,
            InvalidParameterException {

        Report result = new Report();
        QueryParameter parameters = new QueryParameter();
        parametersEnvelope.getTimeZoneConstraint().addToQuery(parameters);

        //Adding search contraints
        for (QueryParameterAware elem : parametersEnvelope.getConstraints()) {
            elem.addToQuery(parameters);
        }

        //Adding Groups
        for (QueryParameterAware elem : parametersEnvelope.getAggregations()) {
            elem.addToQuery(parameters);
        }

        Collection<String> mandatoryDimension = new HashSet<String>(config.getMandatoryDimension());
        for (RawConstraint<?> constraint : parameters.getConstraints()) {
            mandatoryDimension.remove(constraint.getDimensionName().toUpperCase(Locale.ENGLISH));
        }

        if (!mandatoryDimension.isEmpty()) {
            if (mandatoryDimension.size() == 1) {
                throw new InvalidParameterException("Dimension " + StringUtils.join(mandatoryDimension, ",") + " is mandatory.");
            } else {
                throw new InvalidParameterException("Dimensions " + StringUtils.join(mandatoryDimension, ",") + " are mandatory.");
            }
        }

        try {
            result.setSummary(sqlQueryEngine.retrieveSummary(config, ReportMeasure.class, parameters));
        } catch (Exception e) {
            LOG.error("Failed to get summary for input ", e);
            throw new ServiceException(e.getMessage());
        }

        List<ReportRecord> records;
        try {
            records = sqlQueryEngine.retrieveRecords(config, ReportRecord.class, parameters);
        } catch (Exception e) {
            LOG.error("Failed to get engine for input ", e);
            throw new ServiceException(e.getMessage());
        }

        result.setRecords(records);
        result.setTotal(records.size());

        LOG.info("log successful calls to service with parameters passed to the engine and result size");
        return result;

    }
}
