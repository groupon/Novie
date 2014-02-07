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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.groupon.novie.engine.ColumnDataType;
import com.groupon.novie.internal.engine.QueryParameter;
import com.groupon.novie.internal.engine.schema.AbstractSqlColumn;
import com.groupon.novie.internal.engine.schema.DimensionTable;
import com.groupon.novie.internal.engine.schema.SqlAggrega;
import com.groupon.novie.internal.response.ReportMeasure;
import com.groupon.novie.internal.response.ReportRecord;
import com.groupon.novie.utils.SchemaDefinitionImpl;

/**
 * @author damiano
 * @since 6/28/13
 */
public class SqlQueryBuilderTest {

    @Mocked
    SchemaDefinitionImpl starSchemaConfig;

    @Mocked
    QueryParameter parameters;

    @Mocked
    ResultSet resultSet;

    @SuppressWarnings("unchecked")
    @Test
    public void testMapRowByGroup() throws Exception {

        final DimensionTable testTable = new DimensionTable("testTable", "Campaign");
        testTable.addTableColumn("c_id", ColumnDataType.INTEGER);
        testTable.addTableColumn("c_name", ColumnDataType.STRING, "name");
        testTable.addTableColumn("c_date", ColumnDataType.DATE, "day");
        testTable.addTableColumn("c_datetime", ColumnDataType.DATETIME);
        testTable.addTableColumn("c_type", ColumnDataType.STRING);
        testTable.addTableColumn("c_int", ColumnDataType.INTEGER);
        testTable.addTableColumn("c_decimal", ColumnDataType.DECIMAL);
        testTable.addTableColumn("c_iso_code", ColumnDataType.STRING, "country");
        testTable.addTableColumn("currency", ColumnDataType.STRING, "currency");
        testTable.setDefaultGroupByKey("c_id");

        new NonStrictExpectations() {
            {
                parameters.getTimezoneName();
                result = "UTC";
                parameters.getGroups();
                result = Lists.newArrayList(Pair.of("AFFILIATE", null));
                starSchemaConfig.getDimensionByName(anyString, anyString);
                result = testTable;
            }
        };

        final SqlQueryBuilder<ReportRecord> sqlQueryBuilder = new SqlQueryBuilder<ReportRecord>(starSchemaConfig, ReportRecord.class, parameters);
        sqlQueryBuilder.buildQuery();
        final GroupByElement groupByElement = sqlQueryBuilder.getGroupByElement().get();// GroupByElement.createGroupByElement(sqlQueryBuilder).get();
        final ArrayList<Pair<AbstractSqlColumn, ArrayList<AbstractSqlColumn>>> sqlTableColumns = Lists.newArrayList(Pair.of(testTable
                .getColumnByName("c_id").get(), Lists.newArrayList(testTable.getColumnByName("c_name").get(), testTable.getColumnByName("c_date")
                .get(), testTable.getColumnByName("c_datetime").get(), testTable.getColumnByName("c_int").get(),
                testTable.getColumnByName("c_decimal").get())));
        new NonStrictExpectations(sqlQueryBuilder, groupByElement) {
            {
                groupByElement.getGroupingColumns();
                result = sqlTableColumns;
                sqlQueryBuilder.getGroupByElement();
                result = Optional.of(groupByElement);
                resultSet.getTimestamp(testTable.getColumnByName("c_datetime").get().getAlias(), withAny(Calendar.getInstance()));
                result = new Timestamp(1372430820387l);
                resultSet.getTimestamp(testTable.getColumnByName("c_date").get().getAlias(), withAny(Calendar.getInstance()));
                result = new Timestamp(1372430820387l);
                resultSet.getString(testTable.getColumnByName("c_name").get().getAlias());
                result = "testStringResult";
                resultSet.getInt(testTable.getColumnByName("c_int").get().getAlias());
                result = 23;
                resultSet.getDouble(testTable.getColumnByName("c_decimal").get().getAlias());
                result = 10256.5667;
            }

        };

        final ReportRecord measuresRecord = sqlQueryBuilder.mapRow(resultSet, 1);
        Assert.assertEquals(measuresRecord.getGroup().size(), 1);
        Assert.assertEquals(measuresRecord.getGroup().get(0).getInformations().get("name"), "testStringResult");
        Assert.assertEquals(measuresRecord.getGroup().get(0).getInformations().get("day"), "2013-06-28+0000");
        Assert.assertEquals(measuresRecord.getGroup().get(0).getInformations().get("c_datetime"), "2013-06-28T14:47+0000");
        Assert.assertEquals(measuresRecord.getGroup().get(0).getInformations().get("c_int"), "23");
        Assert.assertEquals(measuresRecord.getGroup().get(0).getInformations().get("c_decimal"), "10256.5667");

    }

    @Test
    public void testMapRowByMeasure() throws Exception {

        final DimensionTable testTable = new DimensionTable("testTable", "Campaign");
        testTable.addTableColumn("c_id", ColumnDataType.INTEGER);
        testTable.addTableColumn("c_name", ColumnDataType.STRING, "name");
        testTable.addTableColumn("c_date", ColumnDataType.DATE);
        testTable.addTableColumn("c_datetime", ColumnDataType.DATETIME);
        testTable.addTableColumn("c_type", ColumnDataType.STRING);
        testTable.addTableColumn("c_iso_code", ColumnDataType.STRING, "country");
        testTable.addTableColumn("currency", ColumnDataType.STRING, "currency");

        new NonStrictExpectations() {
            {
                parameters.getTimezoneName();
                result = "UTC";

            }
        };

        final SqlQueryBuilder<ReportMeasure> sqlQueryBuilder = new SqlQueryBuilder<ReportMeasure>(starSchemaConfig, ReportMeasure.class, parameters);
        final SelectElement selectElement = new SelectElement(sqlQueryBuilder);

        final List<AbstractSqlColumn> measures = Lists.newArrayList();
        measures.add(new SqlAggrega(testTable, "testColumnName1", "testInfoMeasuresName1", ColumnDataType.INTEGER, 0));
        measures.add(new SqlAggrega(testTable, "testColumnName2", "testInfoMeasuresName2", ColumnDataType.INTEGER, 1));

        new NonStrictExpectations(sqlQueryBuilder, selectElement) {
            {
                sqlQueryBuilder.getGroupByElement();
                result = Optional.absent();
                selectElement.getMeasures();
                result = measures;
                sqlQueryBuilder.getSelectElement();
                result = selectElement;

                resultSet.getLong("fact_testColumnName1");
                result = 2l;
                resultSet.getLong("fact_testColumnName2");
                result = 4l;
            }

        };

        final ReportMeasure measuresRecord = sqlQueryBuilder.mapRow(resultSet, 1);
        Assert.assertEquals(measuresRecord.getMeasures().size(), 2);
        Assert.assertEquals(measuresRecord.getMeasures().get("testInfoMeasuresName1"), 2l);
        Assert.assertEquals(measuresRecord.getMeasures().get("testInfoMeasuresName2"), 4l);

    }
}
