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

import com.groupon.novie.engine.ColumnDataType;

public class SqlAggrega extends PhysicalSqlColumn {

    public static enum Aggrega {

        SUM("sum");

        private String sqlForm;

        private Aggrega(String sqlForm) {
            this.sqlForm = sqlForm;
        }

        public String getSqlForm() {
            return sqlForm;
        }
    }

    private Aggrega agrega;

    public SqlAggrega(SqlTable parentTable, String columnName, String measureName, ColumnDataType sqlType, int order) {
        super(parentTable, columnName, measureName, sqlType, order);
        this.agrega = Aggrega.SUM;
    }

    public SqlAggrega(SqlTable parentTable, String columnName, String measureName, ColumnDataType sqlType, final Aggrega agrega, int order) {
        super(parentTable, columnName, measureName, sqlType, order);
        if (agrega == null) {
            this.agrega = Aggrega.SUM;
        } else {
            this.agrega = agrega;
        }
    }

    @Override
    public String getSqlString() {
        StringBuilder sb = new StringBuilder();
        sb.append(agrega.getSqlForm());
        sb.append("(");
        sb.append(super.getSqlString());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String getAlias() {
        StringBuilder sb = new StringBuilder();
        sb.append("fact_");
        sb.append(super.getColumnName());
        return sb.toString();
    }

    public String toString() {
        StringBuilder returnValue = new StringBuilder("SqlAggrega ");
        returnValue.append(getSqlString());
        return returnValue.toString();
    }
}
