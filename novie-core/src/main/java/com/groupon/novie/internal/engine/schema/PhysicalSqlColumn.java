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

import org.apache.commons.lang3.StringUtils;

import com.groupon.novie.engine.ColumnDataType;

public class PhysicalSqlColumn extends AbstractSqlColumn {
    /**
     *
     */
    String columnName;

    protected PhysicalSqlColumn(SqlTable sqlTable, String columnName, ColumnDataType columnType, int order) {
        super(sqlTable, columnName, columnType, order);
        if (StringUtils.isBlank(columnName)) {
            throw new IllegalArgumentException("Column name can't be null or blank");
        }
        this.columnName = columnName;
    }

    protected PhysicalSqlColumn(SqlTable sqlTable, String columnName, String informationName, ColumnDataType columnType, int order) {
        super(sqlTable, informationName, columnType, order);
        if (StringUtils.isBlank(columnName)) {
            throw new IllegalArgumentException("Column name can't be null or blank");
        }
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    @Override
    public String getSqlString() {
        return this.getSqlTable().getTableName() + '.' + columnName;
    }

    @Override
    public String getAlias() {
        return this.getSqlTable().getTableName() + '_' + columnName;
    }

    public void addForeignKey(AbstractSqlColumn referencedColumn) {
        getSqlTable().addForeignKey(this, referencedColumn);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractSqlColumn)) {
            return false;
        }

        PhysicalSqlColumn column = (PhysicalSqlColumn) o;

        if (!columnName.equals(column.columnName)) {
            return false;
        }
        if (!this.getSqlTable().getTableName().equals(column.getSqlTable().getTableName())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (columnName + this.getSqlTable().getTableName()).hashCode();
    }

    @Override
    public String toString() {
        StringBuilder returnValue = new StringBuilder("PhysicalSqlColumn ");
        returnValue.append(getSqlString());
        return returnValue.toString();
    }

    @Override
    public String getColumnIdentifier() {
        return columnName;
    }

}
