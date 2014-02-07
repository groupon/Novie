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
package com.groupon.novie.internal.engine.builder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;

import com.groupon.novie.internal.exception.NovieRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.google.common.base.Optional;
import com.groupon.novie.SchemaDefinition;
import com.groupon.novie.internal.engine.QueryParameter;
import com.groupon.novie.internal.engine.constraint.Constraint;
import com.groupon.novie.internal.engine.constraint.ConstraintPair;
import com.groupon.novie.internal.engine.constraint.ConstraintPair.BinaryOperator;
import com.groupon.novie.internal.engine.schema.AbstractSqlColumn;
import com.groupon.novie.internal.engine.schema.DimensionTable;
import com.groupon.novie.internal.response.GroupDisplayingRecord;
import com.groupon.novie.internal.response.MeasureAppender;
import com.groupon.novie.internal.response.ReportRecord;

public class SqlQueryBuilder<T extends MeasureAppender> implements RowMapper<T>, SqlQueryBuilderAccess {

    private static final Logger LOG = LoggerFactory.getLogger(SqlQueryBuilder.class);

    private FromElement fromTables;
    private SelectElement selectElement;
    private SchemaDefinition starSchemaConfig;
    private Optional<GroupByElement> groupByElement;
    private WhereElement whereElement;

    private Optional<OrderByElement> orderByElement;
    private Class<T> resultType;
    private QueryParameter queryParameter;

    private MapSqlParameterSource sqlParameters = new MapSqlParameterSource();
    private TimeZone tz;

    public SqlQueryBuilder(SchemaDefinition starSchemaConfig, Class<T> resultType, QueryParameter queryParameter) {
        // TODO sanity check + throw exception if case of failure
        selectElement = new SelectElement(this);
        fromTables = new FromElement(this);
        this.starSchemaConfig = starSchemaConfig;
        this.resultType = resultType;
        this.queryParameter = queryParameter;
        String tzName = queryParameter.getTimezoneName();
        if (tzName == null) {
            tzName = starSchemaConfig.getDefaultTimezone();
            if (LOG.isDebugEnabled()) {
                LOG.debug("No timeZone specified for the query, use the config default one (" + tzName + ").");
            }
        }
        tz = TimeZone.getTimeZone(tzName);
        if (LOG.isInfoEnabled()) {
            LOG.info("Use timezone: " + tz.getDisplayName() + ".");
        }

    }

    public void buildQuery() throws NovieRuntimeException {
        if (!queryParameter.getGroups().isEmpty() && !ReportRecord.class.isAssignableFrom(resultType)) {
            throw new NovieRuntimeException("Return type can't handle grouping.");
        }
        groupByElement = GroupByElement.createGroupByElement(this);
        whereElement = WhereElement.createWhereElement(this);
        selectElement.addMeasures(starSchemaConfig.getMeasuresColumn());
        if (groupByElement.isPresent()) {
            for (Pair<AbstractSqlColumn, List<AbstractSqlColumn>> v : groupByElement.get().getGroupingColumns()) {
                selectElement.addTableColumns(v.getRight());
            }
        }
        fromTables.addTables(selectElement.getSqlTables());
        fromTables.addTables(whereElement.getSqlTables());

        orderByElement = OrderByElement.createOrderByElement(this);

    }

    public String getQueryString() {

        StringBuilder returnValue = new StringBuilder();
        returnValue.append("select ");
        returnValue.append(selectElement.getSqlString());
        returnValue.append(" from ");
        returnValue.append(fromTables.getSqlString());
        Constraint tablesContraint = fromTables.getConstraints();
        Constraint functionalConstraint = whereElement.getConstraints();
        if (tablesContraint != null && functionalConstraint != null) {
            returnValue.append(" where ");
            returnValue.append(new ConstraintPair(tablesContraint, BinaryOperator.AND, functionalConstraint).generateConstraint(sqlParameters));
        } else if (tablesContraint != null) {
            returnValue.append(" where ");
            returnValue.append(tablesContraint.generateConstraint(sqlParameters));
        } else if (functionalConstraint != null) {
            returnValue.append(" where ");
            returnValue.append(functionalConstraint.generateConstraint(sqlParameters));
        }
        if (groupByElement.isPresent()) {
            returnValue.append(" group by ");
            returnValue.append(groupByElement.get().getSqlString());
        }
        if (orderByElement.isPresent()) {
            returnValue.append(" order by ");
            returnValue.append(orderByElement.get().getSqlString());
        }

        return returnValue.toString();
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {

        try {
            T returnValue = resultType.newInstance();
            if (getGroupByElement().isPresent() && ReportRecord.class.isAssignableFrom(resultType)) {
                ReportRecord rr = (ReportRecord) returnValue;
                GroupDisplayingRecord currentGroup = null;
                for (Pair<AbstractSqlColumn, List<AbstractSqlColumn>> v : groupByElement.get().getGroupingColumns()) {
                    if (v.getLeft().getSqlTable() instanceof DimensionTable) {
                        DimensionTable dimTable = (DimensionTable) v.getLeft().getSqlTable();
                        currentGroup = rr.addOrRetrieveGroup(dimTable.getDimensionName());
                        for (AbstractSqlColumn col : v.getRight()) {
                            String groupValueStr = col.getColumnType().mapResult(col.getAlias(), rs, tz);
                            currentGroup.addInformation(col.getBusinessName(), groupValueStr);
                            if (v.getRight().size() == 1) {
                                // Only one information => selected by the user
                                currentGroup.setDisplay(groupValueStr);
                            }
                        }

                        if (currentGroup.getDisplay() == null) {
                            // In case of there is multiple returned
                            // informations
                            if (StringUtils.isBlank(dimTable.getDisplayTemplate())) {
                                StringBuilder buff = new StringBuilder();
                                for (Entry<String, String> e : currentGroup.getInformations().entrySet()) {
                                    if (buff.length() > 0) {
                                        buff.append(" ");
                                    }
                                    buff.append(e.getValue());
                                }
                                currentGroup.setDisplay(buff.toString());
                            } else {
                                String display = new String(dimTable.getDisplayTemplate());
                                for (Entry<String, String> e : currentGroup.getInformations().entrySet()) {
                                    display = display.replace("%" + e.getKey() + "%", e.getValue() == null ? "null" : e.getValue());
                                }
                                currentGroup.setDisplay(display);
                            }
                        }
                    }
                }
            }
            // **
            // Process the measure

            for (AbstractSqlColumn col : getSelectElement().getMeasures()) {

                switch (col.getColumnType()) {
                    case DECIMAL:
                        returnValue.addMeasure(col.getBusinessName(), rs.getDouble(col.getAlias()));
                        break;
                    case INTEGER:
                        returnValue.addMeasure(col.getBusinessName(), rs.getLong(col.getAlias()));
                        break;
                    default:
                        throw new SQLException("Col " + col.getBusinessName() + " has a non supported format for a Measure.");

                }

            }

            return returnValue;
        } catch (InstantiationException e) {
            throw new SQLException("Can instanciate result object.", e);
        } catch (IllegalAccessException e) {

            throw new SQLException("Can instanciate result object.", e);
        }
    }

    public MapSqlParameterSource getMapSqlParameterSource() {
        return sqlParameters;
    }

    @Override
    public QueryParameter getQueryParameter() {
        return queryParameter;
    }

    @Override
    public SchemaDefinition getStarSchemaConfig() {
        return starSchemaConfig;
    }

    @Override
    public SelectElement getSelectElement() {
        return selectElement;
    }

    public Optional<GroupByElement> getGroupByElement() {
        return groupByElement;
    }
}
