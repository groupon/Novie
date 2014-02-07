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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.groupon.novie.internal.engine.schema.AbstractSqlColumn;
import com.groupon.novie.internal.engine.schema.SqlTable;

/**
 * @author damiano
 * @since 6 October 13
 */
public class SelectElement extends SqlQueryBuilderElement {

    /**
     * Map<Col identifier, column>
     */
    private final Map<String, AbstractSqlColumn> sqlTableColumns =  Maps.newHashMap();

    private final List<AbstractSqlColumn> measures = new LinkedList<AbstractSqlColumn>();

    public SelectElement(SqlQueryBuilderAccess queryBuilderAccess) {
        super(queryBuilderAccess);
    }

    public void addTableColumns(Collection<AbstractSqlColumn> columns) {
        for (AbstractSqlColumn col : columns) {
            sqlTableColumns.put(col.getColumnIdentifier(), col);
        }
    }

    public void addMeasures(List<AbstractSqlColumn> measures) {
        for (AbstractSqlColumn m : measures) {
            this.measures.add(m);
            sqlTableColumns.put(m.getColumnIdentifier(), m);
        }
    }

    @Override
    public Collection<? extends SqlTable> getSqlTables() {
        Set<SqlTable> returnValue = new HashSet<SqlTable>();
        for (AbstractSqlColumn col : sqlTableColumns.values()) {
            returnValue.add(col.getSqlTable());
        }
        return returnValue;
    }

    public StringBuilder getSqlString() {
        StringBuilder sb = new StringBuilder();
        for (AbstractSqlColumn column : sqlTableColumns.values()) {
            if (sb.length() > 0) {
                sb.append(" , ");
            }
            sb.append(column.getSqlString());
            sb.append(" as ");
            sb.append(column.getAlias());
        }
        return sb;
    }

    public List<AbstractSqlColumn> getMeasures() {
        return Collections.unmodifiableList(measures);
    }

    public Map<String, AbstractSqlColumn> getSqlTableColumns() {
        return Collections.unmodifiableMap(sqlTableColumns);
    }
}
