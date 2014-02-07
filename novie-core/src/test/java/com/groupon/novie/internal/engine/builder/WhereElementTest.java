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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mockit.Expectations;
import mockit.Mocked;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.groupon.novie.SchemaDefinition;
import com.groupon.novie.engine.ColumnDataType;
import com.groupon.novie.internal.engine.QueryOperator;
import com.groupon.novie.internal.engine.QueryParameter;
import com.groupon.novie.internal.engine.constraint.RawConstraint;
import com.groupon.novie.internal.engine.schema.DimensionTable;

public class WhereElementTest {

    @Mocked
    SqlQueryBuilderAccess queryBuilderAccess;

    @Mocked
    MapSqlParameterSource mapSqlParameterSource;

    @Mocked
    QueryParameter queryParameter;

    @Mocked
    SchemaDefinition starSchemaConfig;

    @SuppressWarnings("unchecked")
    @Test
    public void createWhereElementTestSingleLikeConstraint() throws Exception {

        final DimensionTable dimensionTable = new DimensionTable("testDimension", "testDimensionName");
        dimensionTable.addTableColumn("testColumn", ColumnDataType.STRING, "TESTINFONAME");
        final RawConstraint<Object> rawConstraint = new RawConstraint<Object>("testDimensionName", "TESTINFONAME", QueryOperator.LIKE, "test%");
        new Expectations() {
            {
                queryBuilderAccess.getQueryParameter();
                result = queryParameter;
                queryParameter.getConstraints();
                result = Lists.newArrayList(rawConstraint);
                queryBuilderAccess.getStarSchemaConfig();
                result = starSchemaConfig;
                queryParameter.getTimezoneName();
                result = "UTC";
                starSchemaConfig.getDimensionByName("testDimensionName", "UTC");
                result = dimensionTable;
                // queryBuilderAccess.getInformationColumnFromName(dimensionTable,
                // rawConstraint.getInformationName(), true);
                // result =
                // dimensionTable.getSqlTableColumnByInformationName(rawConstraint.getInformationName());
                mapSqlParameterSource.getValues();
                result = Maps.newHashMap();
                mapSqlParameterSource.addValue("testDimension_testColumn_0", "test%", ColumnDataType.STRING.getSqlType());
            }
        };

        WhereElement whereElement = WhereElement.createWhereElement(queryBuilderAccess);
        Assert.assertEquals(whereElement.getSqlTables().size(), 1);
        StringBuilder stringBuilder = whereElement.getConstraints().generateConstraint(mapSqlParameterSource);
        Assert.assertEquals(stringBuilder.toString(), "testDimension.testColumn like :testDimension_testColumn_0");

    }

    @SuppressWarnings("unchecked")
    @Test
    public void createWhereElementTestConstraintPair() throws Exception {

        final DimensionTable dimensionTable = new DimensionTable("testDimension", "testDimensionName");
        dimensionTable.addTableColumn("testColumn", ColumnDataType.STRING, "TESTINFONAME");
        final RawConstraint<Object> rawConstraint = new RawConstraint<Object>("testDimensionName", "TESTINFONAME", QueryOperator.LIKE, "test%");
        final RawConstraint<Object> rawConstraint2 = new RawConstraint<Object>("testDimensionName", "TESTINFONAME", QueryOperator.GREATER_THAN,
                "test2");
        new Expectations() {
            {
                queryBuilderAccess.getQueryParameter();
                result = queryParameter;
                queryParameter.getConstraints();
                result = Lists.newArrayList(rawConstraint, rawConstraint2);
                queryBuilderAccess.getStarSchemaConfig();
                result = starSchemaConfig;
                queryParameter.getTimezoneName();
                result = "UTC";
                starSchemaConfig.getDimensionByName("testDimensionName", "UTC");
                result = dimensionTable;
                // queryBuilderAccess.getInformationColumnFromName(dimensionTable,
                // rawConstraint.getInformationName(), true);
                // result =
                // dimensionTable.getSqlTableColumnByInformationName(rawConstraint.getInformationName());
                queryBuilderAccess.getStarSchemaConfig();
                result = starSchemaConfig;
                queryParameter.getTimezoneName();
                result = "UTC";
                starSchemaConfig.getDimensionByName("testDimensionName", "UTC");
                result = dimensionTable;
                // queryBuilderAccess.getInformationColumnFromName(dimensionTable,
                // rawConstraint2.getInformationName(), true);
                // result =
                // dimensionTable.getSqlTableColumnByInformationName(rawConstraint2.getInformationName());

                mapSqlParameterSource.getValues();
                result = Maps.newHashMap();
                mapSqlParameterSource.addValue("testDimension_testColumn_0", "test%", ColumnDataType.STRING.getSqlType());

                mapSqlParameterSource.getValues();
                result = Collections.singletonMap(1, "1");
                mapSqlParameterSource.addValue("testDimension_testColumn_1", "test2", ColumnDataType.STRING.getSqlType());
            }
        };

        WhereElement whereElement = WhereElement.createWhereElement(queryBuilderAccess);
        Assert.assertEquals(whereElement.getSqlTables().size(), 1);
        StringBuilder stringBuilder = whereElement.getConstraints().generateConstraint(mapSqlParameterSource);
        Assert.assertEquals(stringBuilder.toString(),
                "( testDimension.testColumn like :testDimension_testColumn_0 )  AND ( testDimension.testColumn > :testDimension_testColumn_1 ) ");

    }

    @SuppressWarnings("unchecked")
    @Test
    public void createWhereElementTestORConstraint() throws Exception {

        final DimensionTable dimensionTable = new DimensionTable("testDimension", "testDimensionName");
        dimensionTable.addTableColumn("testColumn", ColumnDataType.STRING, "TESTINFONAME");
        List<Pair<QueryOperator, String>> orConstraints = new ArrayList<Pair<QueryOperator, String>>();
        orConstraints.add(Pair.of(QueryOperator.EQUAL, "USD"));
        orConstraints.add(Pair.of(QueryOperator.EQUAL, "EU"));
        final RawConstraint<String> rawConstraint = new RawConstraint<String>("testDimensionName", "TESTINFONAME", orConstraints);

        new Expectations() {
            {
                queryBuilderAccess.getQueryParameter();
                result = queryParameter;
                queryParameter.getConstraints();
                result = Lists.newArrayList(rawConstraint);

                queryBuilderAccess.getStarSchemaConfig();
                result = starSchemaConfig;
                queryParameter.getTimezoneName();
                result = "UTC";

                starSchemaConfig.getDimensionByName("testDimensionName", "UTC");
                result = dimensionTable;

                // queryBuilderAccess.getInformationColumnFromName(dimensionTable,
                // rawConstraint.getInformationName(), true);
                // result =
                // dimensionTable.getSqlTableColumnByInformationName(rawConstraint.getInformationName());

                mapSqlParameterSource.getValues();
                result = Maps.newHashMap();
                mapSqlParameterSource.addValue("testDimension_testColumn_0", "USD", ColumnDataType.STRING.getSqlType());

                mapSqlParameterSource.getValues();
                result = Collections.singletonMap(1, "1");
                mapSqlParameterSource.addValue("testDimension_testColumn_1", "EU", ColumnDataType.STRING.getSqlType());
            }
        };

        WhereElement whereElement = WhereElement.createWhereElement(queryBuilderAccess);
        Assert.assertEquals(whereElement.getSqlTables().size(), 1);
        StringBuilder stringBuilder = whereElement.getConstraints().generateConstraint(mapSqlParameterSource);
        Assert.assertEquals(stringBuilder.toString(),
                "( testDimension.testColumn = :testDimension_testColumn_0 )  OR ( testDimension.testColumn = :testDimension_testColumn_1 ) ");
    }
}
