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

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.groupon.novie.engine.ColumnDataType;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Object representing an Sql Table should be immutable
 */
public class SqlTable implements SqlElement {

    private String tableName;

    /**
     * Map<Column identifier, Column>
     */
    private Map<String, AbstractSqlColumn> sqlTableColumns;
    /**
     * Map<ForeignTable,Map<Local column,Foreign column>>
     */
    private Map<SqlTable, Map<AbstractSqlColumn, AbstractSqlColumn>> sqlForeignKeys;

    public SqlTable(String tableName) {
        this.tableName = tableName;
        sqlTableColumns = Maps.newLinkedHashMap();
        sqlForeignKeys = Maps.newHashMap();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Checks if the give column exist (by name)
     *
     * @param columnName    The name of the {@link AbstractSqlColumn SqlColumn}
     */
    public final boolean containsColumn(String columnName) {
        if (StringUtils.isNotBlank(columnName)) {
            return sqlTableColumns.containsKey(columnName);
        }
        return false;
    }

    @Override
    public String getSqlString() {
        return tableName;
    }

    public Optional<AbstractSqlColumn> getColumnByName(String columnName) {
        return Optional.fromNullable(sqlTableColumns.get(columnName));
    }

    void addForeignKey(AbstractSqlColumn localColumn, AbstractSqlColumn foreignColumn) {
        if (!sqlForeignKeys.containsKey(foreignColumn.getSqlTable())) {
            sqlForeignKeys.put(foreignColumn.getSqlTable(), new HashMap<AbstractSqlColumn, AbstractSqlColumn>());
        }
        sqlForeignKeys.get(foreignColumn.getSqlTable()).put(localColumn, foreignColumn);
    }

    public Map<AbstractSqlColumn, AbstractSqlColumn> findForeignKeyForTable(final SqlTable sqlTable) {
        if (sqlForeignKeys.containsKey(sqlTable)) {
            return sqlForeignKeys.get(sqlTable);
        }
        return Collections.emptyMap();
    }

    public List<AbstractSqlColumn> getListOfColumn() {
        return Collections.unmodifiableList(Lists.newArrayList(sqlTableColumns.values()));
    }

    public List<SqlTable> getLinkedTables() {
        List<SqlTable> listForeignTables = Lists.newArrayList(sqlForeignKeys.keySet());
        return Collections.unmodifiableList(listForeignTables);
    }

    public final PhysicalSqlColumn addTableColumn(String columnName, ColumnDataType type) {
        PhysicalSqlColumn returnValue = new PhysicalSqlColumn(this, columnName, type, getNextColumnOrder());
        addTableColumn(returnValue);
        return returnValue;
    }

    protected final void addTableColumn(AbstractSqlColumn col) {
        sqlTableColumns.put(col.getColumnIdentifier(), col);
    }

    protected final int getNextColumnOrder() {
        return sqlTableColumns.size();
    }

    @Override
    public String toString() {
        StringBuilder returnValue = new StringBuilder("Table ");
        returnValue.append(this.getTableName());
        return returnValue.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SqlTable sqlTable = (SqlTable) o;

        if (!sqlTableColumns.equals(sqlTable.sqlTableColumns)) {
            return false;
        }
        if (!tableName.equals(sqlTable.tableName)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = tableName != null ? tableName.hashCode() : 0;
        result = 31 * result + (sqlTableColumns != null ? sqlTableColumns.hashCode() : 0);
        return result;
    }

}
