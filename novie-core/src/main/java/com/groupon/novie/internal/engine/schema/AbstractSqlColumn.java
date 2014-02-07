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

public abstract class AbstractSqlColumn implements SqlElement {

    /**
     *
     */
    private final SqlTable sqlTable;
    private ColumnDataType columnType;
    private String businessName;
    private int order;

    protected AbstractSqlColumn(SqlTable sqlTable, String businessName, ColumnDataType columnType, int order) {
        if (sqlTable == null) {
            throw new IllegalArgumentException("Table can't be null");
        }
        if (columnType == null) {
            throw new IllegalArgumentException("Data type can't be null");
        }
        if (StringUtils.isBlank(businessName)) {
            throw new IllegalArgumentException("Information name can't be null or blank");
        }
        this.sqlTable = sqlTable;
        this.columnType = columnType;
        this.businessName = businessName;
        this.order = order;
    }

    @Override
    public abstract String getSqlString();

    public abstract String getAlias();

    public SqlTable getSqlTable() {
        return this.sqlTable;
    }

    public ColumnDataType getColumnType() {
        return columnType;
    }

    public String getBusinessName() {
        return businessName;
    }

    public int getOrder() {
        return order;
    }

    /**
     * Return the column unique identifier at a table level. Typically he column
     * name for real SqlColumn
     */
    public abstract String getColumnIdentifier();

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();
}