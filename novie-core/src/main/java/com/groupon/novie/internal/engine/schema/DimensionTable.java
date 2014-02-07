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
package com.groupon.novie.internal.engine.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.groupon.novie.engine.ColumnDataType;

public class DimensionTable extends SqlTable {

    private String dimensionName;
    private String displayTemplate;
    private AbstractSqlColumn defaultGroupByColumn;
    private AbstractSqlColumn defaultSearchColumn;
    private Optional<AbstractSqlColumn> defaultSortColumn;
    private Map<String, AbstractSqlColumn> informationNameToColumn;

    public DimensionTable(String tableName, String dimensionName) {
        super(tableName);
        this.dimensionName = dimensionName;
        informationNameToColumn = Maps.newLinkedHashMap();
        defaultSortColumn = Optional.absent();
    }

    public String getDisplayTemplate() {
        return displayTemplate;
    }

    public void setDisplayTemplate(String displayTemplate) {
        this.displayTemplate = displayTemplate;
    }

    public String getDimensionName() {
        return dimensionName;
    }

    public AbstractSqlColumn getDefaultGroupByColumn() {
        return defaultGroupByColumn;
    }

    public void setDefaultGroupByKey(String groupByColumnName) {
        if (!containsColumn(groupByColumnName)) {
            throw new IllegalArgumentException("Column " + groupByColumnName + " is not defined in table " + getTableName() + ".");
        }
        this.defaultGroupByColumn = getColumnByName(groupByColumnName).get();
    }

    public AbstractSqlColumn getDefaultSearchColumn() {
        return defaultSearchColumn;
    }

    public Optional<AbstractSqlColumn> getDefaultSortColumn() {
        return defaultSortColumn;
    }

    public void setDefaultSearchColumn(String defaultSearchColumnName) {
        Optional<AbstractSqlColumn> defaultSearch = getColumnByName(defaultSearchColumnName);
        if (!defaultSearch.isPresent()) {
            throw new IllegalArgumentException("Error while setting default search column: Column " + defaultSearchColumnName + " is not present in "
                    + this.getTableName() + ".");
        }
        this.defaultSearchColumn = getColumnByName(defaultSearchColumnName).get();
    }

    public void setDefaultSortColumn(String defaultSortColumnName) {
        Optional<AbstractSqlColumn> defaultSearch = getColumnByName(defaultSortColumnName);
        if (!defaultSearch.isPresent()) {
            throw new IllegalArgumentException("Error while setting default search column: Column " + defaultSortColumnName + " is not present in "
                    + this.getTableName() + ".");
        }
        this.defaultSortColumn = getColumnByName(defaultSortColumnName);
    }

    public void addTableColumn(String columnName, ColumnDataType type, String functionalName) {
        // TODO Sanity checks + exception
        PhysicalSqlColumn col = new PhysicalSqlColumn(this, columnName, functionalName, type, getNextColumnOrder());
        informationNameToColumn.put(functionalName.toUpperCase(), col);
        super.addTableColumn(col);
    }

    public AbstractSqlColumn getSqlTableColumnByInformationName(String functionalName) {
        return informationNameToColumn.get(functionalName.toUpperCase());
    }

    public List<AbstractSqlColumn> getInformationColumns() {
        List<AbstractSqlColumn> returnValue = new ArrayList<AbstractSqlColumn>(informationNameToColumn.size());
        for (Entry<String, AbstractSqlColumn> e : informationNameToColumn.entrySet()) {
            returnValue.add(e.getValue());
        }
        return Collections.unmodifiableList(returnValue);
    }

    @Override
    public String toString() {
        StringBuilder returnValue = new StringBuilder("Dimension ");
        returnValue.append(dimensionName);
        returnValue.append(" ");
        returnValue.append(this.getTableName());
        return returnValue.toString();
    }

    /**
     * Checks is the specified dimension is equivalent. Equivalent means same
     * informations (including type), same defaults, same links
     *
     * @param otherDim  The {@link DimensionTable} to compare for equivalence against
     * @param errors    An {@link Error} collection that will be populated with any
     *                  additional {@link Error}s
     * @return true if equivalent false otherwise
     */
    public boolean isEquivalent(DimensionTable otherDim, Collection<Error> errors) {
        if (this.informationNameToColumn.size() != otherDim.informationNameToColumn.size()) {
            errors.add(new Error("[" + this.getDimensionName() + "-" + otherDim.getDimensionName() + "] - Not the same number of informations."));
            return false;
        }
        for (Entry<String, AbstractSqlColumn> entry : this.informationNameToColumn.entrySet()) {
            AbstractSqlColumn otherCol = otherDim.informationNameToColumn.get(entry.getKey());
            if (otherCol == null) {
                errors.add(new Error("[" + this.getDimensionName() + "-" + otherDim.getDimensionName() + "] - Information named " + entry.getKey()
                        + " not found in " + otherDim.getDimensionName() + "."));
                return false;
            }
            if (!otherCol.getBusinessName().equals(entry.getValue().getBusinessName())) {
                errors.add(new Error("[" + this.getDimensionName() + "-" + otherDim.getDimensionName() + "] - Information named " + entry.getKey()
                        + " have not the same name."));
                return false;
            }
            if (otherCol.getColumnType() != entry.getValue().getColumnType()) {
                errors.add(new Error("[" + this.getDimensionName() + "-" + otherDim.getDimensionName() + "] - Information named " + entry.getKey()
                        + " are not the same type."));
                return false;
            }
        }
        if (!otherDim.getDefaultSearchColumn().getBusinessName().equals(this.getDefaultSearchColumn().getBusinessName())) {
            errors.add(new Error("[" + this.getDimensionName() + "-" + otherDim.getDimensionName() + "] - \"default search\" are not the same."));
            return false;
        }
        if (!otherDim.getDefaultGroupByColumn().getBusinessName().equals(this.getDefaultGroupByColumn().getBusinessName())) {
            errors.add(new Error("[" + this.getDimensionName() + "-" + otherDim.getDimensionName() + "] - \"default group by\" are not the same."));
            return false;
        }
        if (!otherDim.getLinkedTables().equals(this.getLinkedTables())) {
            errors.add(new Error("[" + this.getDimensionName() + "-" + otherDim.getDimensionName() + "] - Have not the same linked tables."));
            return false;
        }
        return true;
    }
}
