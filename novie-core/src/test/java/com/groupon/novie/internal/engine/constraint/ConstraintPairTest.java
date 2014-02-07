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
package com.groupon.novie.internal.engine.constraint;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.groupon.novie.engine.ColumnDataType;
import com.groupon.novie.internal.engine.QueryOperator;
import com.groupon.novie.internal.engine.builder.ColumnRefValueSqlElement;
import com.groupon.novie.internal.engine.schema.AbstractSqlColumn;
import com.groupon.novie.internal.engine.schema.DimensionTable;

public class ConstraintPairTest {

    @Test
    public void testGenerateConstraint() throws Exception {
        final DimensionTable dimensionTable = new DimensionTable("testDimension", "testDimensionName");
        dimensionTable.addTableColumn("testColumn", ColumnDataType.STRING, "TESTINFONAME");
        dimensionTable.addTableColumn("testColumn2", ColumnDataType.STRING, "TESTINFONAME2");
        dimensionTable.addTableColumn("testColumn3", ColumnDataType.INTEGER, "TESTINFONAME3");
        dimensionTable.addTableColumn("testColumn4", ColumnDataType.DECIMAL, "TESTINFONAME4");
        dimensionTable.addTableColumn("testColumn5", ColumnDataType.DATE, "TESTINFONAME5");
        dimensionTable.addTableColumn("testColumn6", ColumnDataType.DATETIME, "TESTINFONAME6");

        final AbstractSqlColumn testinfoname5 = dimensionTable.getSqlTableColumnByInformationName("TESTINFONAME5");
        final AbstractSqlColumn testinfoname6 = dimensionTable.getSqlTableColumnByInformationName("TESTINFONAME6");
        ConstraintPair left = new ConstraintPair(new ColumnConstraint(testinfoname5, new ColumnRefValueSqlElement(testinfoname5, "test5%"),
                QueryOperator.LESS_THAN_OR_EQUAL), ConstraintPair.BinaryOperator.OR, new ColumnConstraint(testinfoname6,
                new ColumnRefValueSqlElement(testinfoname6, 2), QueryOperator.EQUAL));
        final AbstractSqlColumn testinfoname = dimensionTable.getSqlTableColumnByInformationName("TESTINFONAME");
        final AbstractSqlColumn testinfoname3 = dimensionTable.getSqlTableColumnByInformationName("TESTINFONAME3");
        ConstraintPair left2 = new ConstraintPair(new ColumnConstraint(testinfoname, new ColumnRefValueSqlElement(testinfoname, "test%"),
                QueryOperator.NOT_LIKE), ConstraintPair.BinaryOperator.AND, new ColumnConstraint(testinfoname3, new ColumnRefValueSqlElement(
                testinfoname3, 2), QueryOperator.GREATER_THAN_OR_EQUAL));

        final AbstractSqlColumn testinfoname2 = dimensionTable.getSqlTableColumnByInformationName("TESTINFONAME2");
        final AbstractSqlColumn testinfoname4 = dimensionTable.getSqlTableColumnByInformationName("TESTINFONAME4");
        ConstraintPair right2 = new ConstraintPair(new ColumnConstraint(testinfoname2, new ColumnRefValueSqlElement(testinfoname2, ""),
                QueryOperator.EQUAL), ConstraintPair.BinaryOperator.AND, new ColumnConstraint(testinfoname4, new ColumnRefValueSqlElement(
                testinfoname4, 2.34), QueryOperator.LESS_THAN));

        ConstraintPair right = new ConstraintPair(left2, ConstraintPair.BinaryOperator.OR, right2);

        ConstraintPair roor = new ConstraintPair(left, ConstraintPair.BinaryOperator.AND, right);

        final MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        final StringBuilder stringBuilder = roor.generateConstraint(mapSqlParameterSource);

        Assert.assertEquals(mapSqlParameterSource.getValues().size(), 6);
        Assert.assertEquals(mapSqlParameterSource.getValue("testDimension_testColumn5_0"), "test5%");
        Assert.assertEquals(mapSqlParameterSource.getValue("testDimension_testColumn6_1"), 2);
        Assert.assertEquals(mapSqlParameterSource.getValue("testDimension_testColumn_2"), "test%");
        Assert.assertEquals(mapSqlParameterSource.getValue("testDimension_testColumn3_3"), 2);
        Assert.assertEquals(mapSqlParameterSource.getValue("testDimension_testColumn2_4"), "");
        Assert.assertEquals(mapSqlParameterSource.getValue("testDimension_testColumn4_5"), 2.34);
        Assert.assertEquals(
                stringBuilder.toString(),
                "( ( testDimension.testColumn5 <= :testDimension_testColumn5_0 )  OR ( testDimension.testColumn6 = :testDimension_testColumn6_1 )  )  AND ( ( ( testDimension.testColumn not like :testDimension_testColumn_2 )  AND ( testDimension.testColumn3 >= :testDimension_testColumn3_3 )  )  OR ( ( testDimension.testColumn2 = :testDimension_testColumn2_4 )  AND ( testDimension.testColumn4 < :testDimension_testColumn4_5 )  )  ) ");

    }
}
