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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.groupon.novie.internal.engine.constraint.Constraint;
import com.groupon.novie.internal.engine.constraint.ConstraintPair;
import com.groupon.novie.internal.engine.constraint.ConstraintPair.BinaryOperator;
import com.groupon.novie.internal.engine.constraint.InnerJoinConstraint;
import com.groupon.novie.internal.engine.schema.AbstractSqlColumn;
import com.groupon.novie.internal.engine.schema.DimensionTable;
import com.groupon.novie.internal.engine.schema.SqlTable;

/**
 * @author damiano
 * @since 6 July 2013
 */
public class FromElement extends SqlQueryBuilderElement {

    private static final Logger LOG = LoggerFactory.getLogger(FromElement.class);

    private Set<SqlTable> sqlTables;

    public FromElement(SqlQueryBuilderAccess queryBuilderAccess) {
        super(queryBuilderAccess);
        this.sqlTables = new HashSet<SqlTable>();
    }

    @Override
    public Collection<SqlTable> getSqlTables() {
        return Collections.unmodifiableSet(sqlTables);
    }

    public StringBuilder getSqlString() {
        StringBuilder sb = new StringBuilder();
        for (SqlTable sqlTable : sqlTables) {
            if (sb.length() > 0) {
                sb.append(" , ");
            }
            sb.append(sqlTable.getTableName());
        }
        return sb;
    }

    public void addTable(SqlTable table) {
        if (table != null) {
            sqlTables.add(table);
            if (DimensionTable.class.isAssignableFrom(table.getClass()) && !this.queryBuilderAccess.getStarSchemaConfig().isLinkedToFactTable(table)) {
                // It's a DimensionTable let's check if it's linked to The
                // factTable
                Collection<SqlTable> joinedTables = table.getLinkedTables();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Table " + table.getTableName() + " is not linked to the factTable. Add the linked table.");
                }
                if (joinedTables.size() == 1) {
                    SqlTable t = joinedTables.iterator().next();
                    sqlTables.add(t);
                } else if (joinedTables.isEmpty() && LOG.isWarnEnabled()) {
                    String msg = "Dimension table " + table.getTableName() + " is a standalone table - Risk of cartezian.";
                    LOG.warn(msg);
                } else {
                    String msg = "Dimension table " + table.getTableName() + " is a leaf of the snowflake but have more than one linked table.";
                    LOG.error(msg);
                    throw new IllegalArgumentException(msg);
                }
            }
        }

    }

    public void addTables(Collection<? extends SqlTable> tables) {
        for (SqlTable table : tables) {
            // Do not use addAll cause of the treatment in addTable
            addTable(table);
        }
    }

    public Constraint getConstraints() {
        Constraint returnValue = null;
        List<SqlTable> workingList = new ArrayList<SqlTable>(sqlTables);
        for (int i = 0; i < workingList.size(); i++) {
            SqlTable table = workingList.get(i);
            for (int j = i + 1; j < workingList.size(); j++) {
                SqlTable t = workingList.get(j);
                if (queryBuilderAccess.getStarSchemaConfig().areLinked(table, t)) {
                    for (Entry<AbstractSqlColumn, AbstractSqlColumn> join : table.findForeignKeyForTable(t).entrySet()) {
                        if (returnValue == null) {
                            returnValue = new InnerJoinConstraint(join.getKey(), join.getValue());
                        } else {
                            returnValue = new ConstraintPair(returnValue, BinaryOperator.AND, new InnerJoinConstraint(join.getKey(), join.getValue()));
                        }

                    }
                }
            }
        }
        return returnValue;
    }

}
