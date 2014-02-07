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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;

import com.groupon.novie.internal.engine.QuerySortConstraint;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.groupon.novie.SchemaDefinition;
import com.groupon.novie.engine.ColumnDataType;
import com.groupon.novie.internal.engine.OrderByDirection;
import com.groupon.novie.internal.engine.QueryParameter;
import com.groupon.novie.internal.engine.schema.DimensionTable;
import com.groupon.novie.internal.engine.schema.FactTable;


public class OrderByElementTest {

    @Mocked
    SqlQueryBuilderAccess queryBuilderAccess;

    @Mocked
    QueryParameter queryParameter;

    @Mocked
    SchemaDefinition starSchemaConfig;

    @Test
    public void testCreateOrderByElementAbsent() throws Exception {

        new Expectations() {
            {
                queryBuilderAccess.getQueryParameter();
                result = queryParameter;

            }
        };

        Optional<OrderByElement> orderByElement = OrderByElement.createOrderByElement(queryBuilderAccess);
        assertFalse(orderByElement.isPresent());
    }

    @Test
    public void testCreateOrderByElementWithDimension() throws Exception {
        final DimensionTable dimensionTable = new DimensionTable("testDimension", "testDimensionName");
        dimensionTable.addTableColumn("testColumn", ColumnDataType.STRING, "TESTINFONAME");
        final SelectElement selectElement = new SelectElement(queryBuilderAccess);
        new Expectations(selectElement) {
            {
                queryBuilderAccess.getQueryParameter();
                result = queryParameter;
                queryParameter.getOrders();
                result = Lists.newArrayList(new QuerySortConstraint(OrderByDirection.DESC, "TESTDIMENSIONNAME", "TESTINFONAME"));
                times = 2;
                queryBuilderAccess.getStarSchemaConfig();
                result = starSchemaConfig;
                starSchemaConfig.isValidDimension("TESTDIMENSIONNAME");
                result = true;
                queryBuilderAccess.getStarSchemaConfig();
                result = starSchemaConfig;
                queryParameter.getTimezoneName();
                result = "UTC";
                starSchemaConfig.getDimensionByName("TESTDIMENSIONNAME", "UTC");
                result = dimensionTable;
                // queryBuilderAccess.getInformationColumnFromName(dimensionTable,
                // "TESTINFONAME", false);
                // result =
                // dimensionTable.getSqlTableColumnByInformationName("TESTINFONAME");
                queryBuilderAccess.getSelectElement();
                result = selectElement;
                selectElement.getSqlTableColumns();
                result = Collections.singletonMap("testColumn", dimensionTable.getSqlTableColumnByInformationName("TESTINFONAME"));
            }
        };

        Optional<OrderByElement> orderByElement = OrderByElement.createOrderByElement(queryBuilderAccess);
        assertTrue(orderByElement.isPresent());
        assertEquals(1, orderByElement.get().getSqlTables().size());
        assertEquals("testDimension_testColumn DESC", orderByElement.get().getSqlString().toString());

    }

    @Test
    public void testCreateOrderByElementWith2Dimensions() throws Exception {
        final DimensionTable dimensionTable = new DimensionTable("testDimension", "testDimensionName");
        dimensionTable.addTableColumn("testColumn", ColumnDataType.STRING, "TESTINFONAME1.1");
        dimensionTable.addTableColumn("testColumn2", ColumnDataType.STRING, "TESTINFONAME1.2");
        final DimensionTable dimensionTable2 = new DimensionTable("testDimension2", "testDimensionName2");
        dimensionTable2.addTableColumn("testColumn", ColumnDataType.STRING, "TESTINFONAME2.1");
        dimensionTable2.addTableColumn("testColumn2", ColumnDataType.STRING, "TESTINFONAME2.2");
        final SelectElement selectElement = new SelectElement(queryBuilderAccess);
        new NonStrictExpectations() {
            {
                queryBuilderAccess.getQueryParameter();
                result = queryParameter;

                queryParameter.getOrders();
                result = Lists.newArrayList(new QuerySortConstraint(OrderByDirection.DESC, "TESTDIMENSIONNAME", "TESTINFONAME1.1"),
                        (new QuerySortConstraint(OrderByDirection.ASC, "TESTDIMENSIONNAME2", "TESTINFONAME2.2")));

                queryBuilderAccess.getStarSchemaConfig();
                result = starSchemaConfig;

                starSchemaConfig.isValidDimension("TESTDIMENSIONNAME");
                result = true;

                starSchemaConfig.isValidDimension("TESTDIMENSIONNAME2");
                result = true;

                queryParameter.getTimezoneName();
                result = "UTC";

                starSchemaConfig.getDimensionByName("TESTDIMENSIONNAME", "UTC");
                result = dimensionTable;

                starSchemaConfig.getDimensionByName("TESTDIMENSIONNAME2", "UTC");
                result = dimensionTable2;

                // queryBuilderAccess.getInformationColumnFromName(dimensionTable,
                // "TESTINFONAME1.1", false);
                // result =
                // dimensionTable.getSqlTableColumnByInformationName("TESTINFONAME1.1");
                //
                // queryBuilderAccess.getInformationColumnFromName(dimensionTable2,
                // "TESTINFONAME2.2", false);
                // result =
                // dimensionTable2.getSqlTableColumnByInformationName("TESTINFONAME2.2");

            }
        };

        new NonStrictExpectations(selectElement) {
            {
                queryBuilderAccess.getSelectElement();
                result = selectElement;

                selectElement.getSqlTableColumns();
                result = Collections.singletonMap("testColumn", dimensionTable.getSqlTableColumnByInformationName("TESTINFONAME1.1"));
            }
        };

        Optional<OrderByElement> orderByElement = OrderByElement.createOrderByElement(queryBuilderAccess);
        assertTrue(orderByElement.isPresent());
        assertEquals(1, orderByElement.get().getSqlTables().size());
        assertEquals("testDimension_testColumn DESC", orderByElement.get().getSqlString().toString());
        final Map resultMap = Maps.newHashMap();
        resultMap.put("testColumn", dimensionTable.getSqlTableColumnByInformationName("TESTINFONAME1.1"));
        resultMap.put("testColumn2", dimensionTable2.getSqlTableColumnByInformationName("TESTINFONAME2.2"));
        new NonStrictExpectations(selectElement) {
            {
                selectElement.getSqlTableColumns();
                result = resultMap;
            }
        };

        Optional<OrderByElement> orderByElement2 = OrderByElement.createOrderByElement(queryBuilderAccess);
        assertTrue(orderByElement2.isPresent());
        assertEquals(2, orderByElement2.get().getSqlTables().size());
        assertEquals("testDimension_testColumn DESC, testDimension2_testColumn2 ASC", orderByElement2.get().getSqlString().toString());

    }

    @Test
    public void testCreateOrderByElementWithMeasure() throws Exception {

        final SelectElement selectElement = new SelectElement(queryBuilderAccess);
        final FactTable factTable = new FactTable("factTable");
        factTable.addMeasure("TESTMESURECOLUMN", ColumnDataType.INTEGER, "TESTMEASUREName");
        new NonStrictExpectations(selectElement) {
            {
                queryBuilderAccess.getQueryParameter();
                result = queryParameter;
                queryParameter.getOrders();
                result = Lists.newArrayList(new QuerySortConstraint(OrderByDirection.ASC, "TESTMEASUREName", null));
                times = 2;
                queryBuilderAccess.getStarSchemaConfig();
                result = starSchemaConfig;
                starSchemaConfig.isValidDimension("TESTMEASUREName");
                result = false;
                queryBuilderAccess.getStarSchemaConfig();
                result = starSchemaConfig;

                starSchemaConfig.getMeasuresByName("TESTMEASUREName");
                result = Optional.of(factTable.getMeasuresColumn().get("TESTMESURECOLUMN"));
                queryBuilderAccess.getSelectElement();
                result = selectElement;
                selectElement.getSqlTableColumns();
                result = Collections.singletonMap("TESTMESURECOLUMN", factTable.getMeasuresColumn().get("TESTMESURECOLUMN"));

            }
        };

        Optional<OrderByElement> orderByElement = OrderByElement.createOrderByElement(queryBuilderAccess);
        assertTrue(orderByElement.isPresent());
        assertEquals(1, orderByElement.get().getSqlTables().size());
        assertEquals("fact_TESTMESURECOLUMN ASC", orderByElement.get().getSqlString().toString());

    }

    @Test
    public void testCreateOrderByElementWithDimensionAsDefaultSortColumn() throws Exception {
        final DimensionTable dimensionTable = new DimensionTable("testDimension", "testDimensionName");
        dimensionTable.addTableColumn("testColumn", ColumnDataType.STRING);
        final SelectElement selectElement = new SelectElement(queryBuilderAccess);
        dimensionTable.setDefaultSortColumn("testColumn");
        new Expectations(selectElement) {
            {
                queryBuilderAccess.getQueryParameter();
                result = queryParameter;
                queryParameter.getOrders();
                result = Lists.newArrayList(new QuerySortConstraint(OrderByDirection.DESC, "TESTDIMENSIONNAME", null));
                times = 2;
                queryBuilderAccess.getStarSchemaConfig();
                result = starSchemaConfig;
                starSchemaConfig.isValidDimension("TESTDIMENSIONNAME");
                result = true;
                queryBuilderAccess.getStarSchemaConfig();
                result = starSchemaConfig;
                queryParameter.getTimezoneName();
                result = "UTC";
                starSchemaConfig.getDimensionByName("TESTDIMENSIONNAME", "UTC");
                result = dimensionTable;
                queryBuilderAccess.getSelectElement();
                result = selectElement;
                selectElement.getSqlTableColumns();
                result = Collections.singletonMap("testColumn", dimensionTable.getSqlTableColumnByInformationName("TESTINFONAME"));
            }
        };

        Optional<OrderByElement> orderByElement = OrderByElement.createOrderByElement(queryBuilderAccess);
        assertTrue(orderByElement.isPresent());
        assertEquals(1, orderByElement.get().getSqlTables().size());
        assertEquals("testDimension_testColumn DESC", orderByElement.get().getSqlString().toString());

    }

}
