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
package com.groupon.novie.utils;

import java.util.Collection;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.groupon.novie.engine.ColumnDataType;
import com.groupon.novie.internal.engine.schema.DimensionTable;
import com.groupon.novie.internal.engine.schema.FactTable;

public class SchemaConfigImplTest {

    private SchemaDefinitionImpl ssci;

    @Before
    public void init() {
        ssci = SchemaDefinitionImpl.createInstance("");
        DimensionTable dt = new DimensionTable("test_table", "myDimension");
        dt.addTableColumn("col_id", ColumnDataType.INTEGER);
        dt.addTableColumn("col1", ColumnDataType.STRING, "info1");
        dt.addTableColumn("col2", ColumnDataType.DATE, "info2");
        dt.setDefaultSearchColumn("col2");
        ssci.addDimension(dt, false);
        DimensionTable dt2 = new DimensionTable("test_table2", "myDimension2");
        dt2.addTableColumn("col_id", ColumnDataType.INTEGER);
        dt2.setDefaultSearchColumn("col_id");
        ssci.addDimension(dt2, true);
        FactTable factTable = new FactTable("fact_table");
        factTable.addTableColumn("col1", ColumnDataType.INTEGER);
        factTable.addTableColumn("col2", ColumnDataType.INTEGER);
        ssci.setFactTable(factTable);
        ssci.addTZAliases("CST", "America/Chicago");

    }

    @Test
    public void testIsValidDimension() {
        Assert.assertFalse(ssci.isValidDimension(null, null));
        Assert.assertFalse(ssci.isValidDimension("", null));
        Assert.assertFalse(ssci.isValidDimension(" ", null));
        Assert.assertTrue(ssci.isValidDimension("myDimension", null));
        Assert.assertTrue(ssci.isValidDimension("myDimension", ""));
        Assert.assertTrue(ssci.isValidDimension("myDimension", " "));
        Assert.assertTrue(ssci.isValidDimension("myDimension", "info1"));
        Assert.assertFalse(ssci.isValidDimension("otherDimension", null));
        Assert.assertFalse(ssci.isValidDimension("otherDimension", "non info"));
    }

    @Test
    public void testGetInformationTypeValidDimension() {
        Assert.assertEquals(ssci.getInformationType("myDimension", "info1"), ColumnDataType.STRING);
        Assert.assertEquals(ssci.getInformationType("myDimension", null), ColumnDataType.DATE);
        try {
            ssci.getInformationType("", null);
            Assert.fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            ssci.getInformationType("myDimension", "non info");
            Assert.fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            ssci.getInformationType("otherDimension", null);
            Assert.fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testGetDimensionByName() {

        DimensionTable dtAlternate = new DimensionTable("test_table2", "myDimension2_CST");
        dtAlternate.addTableColumn("col_id", ColumnDataType.INTEGER);
        dtAlternate.setDefaultSearchColumn("col_id");
        ssci.addTZAlternateDimension("myDimension2", dtAlternate, "CST");

        try {
            ssci.getDimensionByName("an other dimension", null);
            Assert.fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }
        Assert.assertNotNull(ssci.getDimensionByName("myDimension", null));
        // Non supported TZ
        Assert.assertNotNull(ssci.getDimensionByName("myDimension", "A-TZ"));
        // UnSupported TZ Alias
        Assert.assertNotNull(ssci.getDimensionByName("myDimension", "America"));
        // Supported TZ
        Assert.assertNotNull(ssci.getDimensionByName("myDimension", "UTC"));
        // Supported TZ Alias
        Assert.assertNotNull(ssci.getDimensionByName("myDimension", "America/Chicago"));

    }

    @Test
    public void testGetInformationColumn() {
        DimensionTable dt = ssci.getDimensionByName("myDimension", null);
        Assert.assertNotNull("Dimension myDimension must exist.", dt);
        try {
            ssci.getInformationColumn(dt, "info");
            Assert.fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }
        Assert.assertEquals(ssci.getInformationColumn(dt, "info1").getBusinessName(), "info1");
    }

    @Test
    public void testGetMandatoryDimension() {
        Assert.assertEquals("Expected one mandatory dimension", ssci.getMandatoryDimension().size(), 1);
        Assert.assertTrue("myDimension2 must be present in mandatory list.", ssci.getMandatoryDimension().contains("mydimension2".toUpperCase()));
        Assert.assertFalse("myDimension must not be present in mandatory list.", ssci.getMandatoryDimension().contains("mydimension".toUpperCase()));
    }

    @Test
    public void testBuildAndValidateConfig() throws Exception {
        ssci.addSqlForeignKey(ImmutablePair.of("fact_table", "col1"), ImmutablePair.of("test_table", "col2"));

        final Collection<Error> errors = ssci.buildAndValidateConfig();
        Assert.assertEquals(errors.size(), 1);
        Assert.assertEquals(errors.iterator().next().getMessage(), "No default Timezone configured");
        Assert.assertEquals(ssci.predecessorRelation.size(), 2);
        Assert.assertTrue(ssci.predecessorRelation.containsKey("test_table"));
        Assert.assertTrue(ssci.predecessorRelation.get("test_table").containsKey("fact_table"));
        Assert.assertTrue(ssci.predecessorRelation.containsKey("fact_table"));
        Assert.assertTrue(ssci.predecessorRelation.get("fact_table").containsKey("test_table"));
        Assert.assertTrue(ssci.isLinkedToFactTable(ssci.getDimensionByName("myDimension", null)));
    }
}
