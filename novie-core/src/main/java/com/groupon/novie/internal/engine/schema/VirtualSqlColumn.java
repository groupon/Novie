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

public class VirtualSqlColumn extends AbstractSqlColumn {

    private String sqlFormula;

    protected VirtualSqlColumn(SqlTable sqlTable, String sqlFormula, String informationName, ColumnDataType columnType, int order) {
        super(sqlTable, informationName, columnType, order);
        if (StringUtils.isBlank(sqlFormula)) {
            throw new IllegalArgumentException("SqlFormula can't be null or empty");
        }
        this.sqlFormula = sqlFormula;
    }

    @Override
    public String getSqlString() {
        return sqlFormula;
    }

    @Override
    public String getAlias() {
        return getSqlTable().getTableName() + "_virt" + getOrder();
    }

    @Override
    public String getColumnIdentifier() {
        return getAlias();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractSqlColumn)) {
            return false;
        }

        VirtualSqlColumn column = (VirtualSqlColumn) o;

        if (!sqlFormula.equals(column.sqlFormula)) {
            return false;
        }
        if (!this.getSqlTable().equals(column.getSqlTable())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (sqlFormula + this.getSqlTable().getTableName()).hashCode();
    }

    @Override
    public String toString() {
        StringBuilder returnValue = new StringBuilder("VirtualSqlColumn ");
        returnValue.append(getSqlString());
        return returnValue.toString();
    }
}
