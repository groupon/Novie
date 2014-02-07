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

import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Resource;

import com.groupon.novie.internal.exception.NovieRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.groupon.novie.SchemaDefinition;
import com.groupon.novie.internal.engine.QueryParameter;
import com.groupon.novie.internal.engine.builder.SqlQueryBuilder;
import com.groupon.novie.internal.engine.QueryParameter.QueryParameterKind;
import com.groupon.novie.internal.response.MeasureAppender;


/**
 * The service in charge of managing calls to the {@link SqlQueryBuilder}.
 *
 * @author thomas
 */
@Service
public class SqlQueryEngine {

    private static final Logger LOG = LoggerFactory.getLogger(SqlQueryEngine.class);

    @Resource(name = "novieEngineJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;


    /**
     * Execute a query to retrieve the summary. This do not have any group by element
     *
     * @param schemaDefinition The schema definition
     * @param resultClazz      The result object
     * @param queryParameter   The Query parameters object
     * @return the summary object if exist. Otherwise throws an exception.
     * @throws com.groupon.novie.internal.exception.NovieRuntimeException
     */
    public <T extends MeasureAppender> T retrieveSummary(SchemaDefinition schemaDefinition, Class<T> resultClazz, QueryParameter queryParameter) throws NovieRuntimeException {
        final SqlQueryBuilder<T> sqlQueryBuilder = new SqlQueryBuilder<T>(schemaDefinition, resultClazz,
                queryParameter.partialCopy(QueryParameterKind.GROUPS, QueryParameterKind.PAGE));
        List<T> result = executeQuery(sqlQueryBuilder);
        if (result.isEmpty()) {
            throw new NovieRuntimeException("Summary doesn't return any result.");
        }
        if (result.size() > 1) {
            throw new NovieRuntimeException("Summary returns more than one result.");
        }
        return result.get(0);
    }

    /**
     * Execute a query to retrieve the records.
     *
     * @param schemaDefinition The schema definition
     * @param resultClazz      The result object
     * @param queryParameter   The Query parameters object
     * @return the summary object if exist. Otherwise throws an exception.
     * @throws com.groupon.novie.internal.exception.NovieRuntimeException
     */
    public <T extends MeasureAppender> List<T> retrieveRecords(SchemaDefinition schemaDefinition, Class<T> resultClazz, QueryParameter queryParameter) throws NovieRuntimeException {
        final SqlQueryBuilder<T> sqlQueryBuilder = new SqlQueryBuilder<T>(schemaDefinition, resultClazz, queryParameter);
        return executeQuery(sqlQueryBuilder);
    }

    /**
     * Private methode called by the public ones to effectively run the query.
     *
     * @param sqlQueryBuilder the QueryBuilder used to run the query
     * @return a list of resultObject
     * @throws NovieRuntimeException
     */
    private <T extends MeasureAppender> List<T> executeQuery(final SqlQueryBuilder<T> sqlQueryBuilder) throws NovieRuntimeException {
        sqlQueryBuilder.buildQuery();
        final String queryString = sqlQueryBuilder.getQueryString();
        LOG.debug(queryString);

        long beforeQuery = System.currentTimeMillis();
        List<T> returnValue = jdbcTemplate.query(queryString, sqlQueryBuilder.getMapSqlParameterSource(), sqlQueryBuilder);

        if (LOG.isInfoEnabled()) {
            LOG.info("SQL query successfully ran in " + (System.currentTimeMillis() - beforeQuery) + "ms.");
        }

        if (LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (Entry<String, Object> e : sqlQueryBuilder.getMapSqlParameterSource().getValues().entrySet()) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(e.getKey());
                sb.append("=");
                sb.append(e.getValue());

            }
            sb.insert(0, "Parameters [");
            sb.append("]");
            LOG.debug(sb.toString());
        }
        return returnValue;
    }

}
