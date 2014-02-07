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

import mockit.Expectations;
import mockit.Mocked;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.groupon.novie.SchemaDefinition;
import com.groupon.novie.engine.ColumnDataType;
import com.groupon.novie.internal.engine.QueryParameter;
import com.groupon.novie.internal.engine.schema.DimensionTable;

public class GroupByElementTest {

    @Mocked
    SqlQueryBuilderAccess queryBuilderAccess;

    @Mocked
    QueryParameter queryParameter;

    @Mocked
    SchemaDefinition starSchemaConfig;

    @Test
    public void testCreateGroupByElementIsAbsent() throws Exception {

        new Expectations() {
            {
                queryBuilderAccess.getQueryParameter();
                result = queryParameter;

            }
        };

        Optional<GroupByElement> groupByElement = GroupByElement.createGroupByElement(queryBuilderAccess);
        assertFalse(groupByElement.isPresent());
    }

    @Test
    public void testCreateGroupByElement() throws Exception {
        final DimensionTable dimensionTable = new DimensionTable("testDimension", "testDimensionName");
        dimensionTable.addTableColumn("testColumn", ColumnDataType.STRING, "TESTINFONAME");
        dimensionTable.addTableColumn("testColumn2", ColumnDataType.STRING, "TESTINFONAME2");
        final SelectElement selectElement = new SelectElement(queryBuilderAccess);
        new Expectations(selectElement) {
            {
                queryBuilderAccess.getQueryParameter();
                result = queryParameter;

                queryParameter.getGroups();
                result = Lists.newArrayList(Pair.of("TESTDIMENSIONNAME", "TESTINFONAME"), Pair.of("TESTDIMENSIONNAME", "TESTINFONAME2"));
                queryBuilderAccess.getStarSchemaConfig();
                result = starSchemaConfig;
                queryParameter.getGroups();
                result = Lists.newArrayList(Pair.of("TESTDIMENSIONNAME", "TESTINFONAME"), Pair.of("TESTDIMENSIONNAME", "TESTINFONAME2"));

                queryParameter.getTimezoneName();
                result = "UTC";

                starSchemaConfig.getDimensionByName("TESTDIMENSIONNAME", "UTC");
                result = dimensionTable;
                queryParameter.getTimezoneName();
                result = "UTC";
                starSchemaConfig.getDimensionByName("TESTDIMENSIONNAME", "UTC");
                result = dimensionTable;
            }
        };

        Optional<GroupByElement> groupByElement = GroupByElement.createGroupByElement(queryBuilderAccess);
        assertTrue(groupByElement.isPresent());
        assertEquals(groupByElement.get().getSqlTables().size(), 1);
        assertEquals(groupByElement.get().getSqlString().toString(), "testDimension.testColumn, testDimension.testColumn2");

    }
}
