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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.groupon.novie.internal.engine.QueryOperator;
import com.groupon.novie.internal.engine.QueryParameter;
import com.groupon.novie.internal.engine.constraint.ColumnConstraint;
import com.groupon.novie.internal.engine.constraint.Constraint;
import com.groupon.novie.internal.engine.constraint.ConstraintPair;
import com.groupon.novie.internal.engine.constraint.RawConstraint;
import com.groupon.novie.internal.engine.constraint.ConstraintPair.BinaryOperator;
import com.groupon.novie.internal.engine.schema.AbstractSqlColumn;
import com.groupon.novie.internal.engine.schema.DimensionTable;
import com.groupon.novie.internal.engine.schema.SqlTable;

public final class WhereElement extends SqlQueryBuilderElement {

    private Constraint rootConstraint;

    private Set<SqlTable> tables = new HashSet<SqlTable>();

    private static final Logger LOG = LoggerFactory.getLogger(WhereElement.class);

    private WhereElement(SqlQueryBuilderAccess queryBuilderAccess) {
        super(queryBuilderAccess);
    }

    private void addDimensionConstraint(DimensionTable dimensionTable, RawConstraint<?> rc) {
        if (dimensionTable != null) {
            tables.add(dimensionTable);
            AbstractSqlColumn columnFromName = null;
            if (StringUtils.isBlank(rc.getInformationName())) {
                columnFromName = dimensionTable.getDefaultSearchColumn();
            } else {
                AbstractSqlColumn searchColumn = dimensionTable.getSqlTableColumnByInformationName(rc.getInformationName());
                if (searchColumn != null) {
                    columnFromName = searchColumn;
                } else {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Dimension " + dimensionTable.getDimensionName() + " don't have information named: " + rc.getInformationName()
                                + "return default.");
                    }
                    columnFromName = dimensionTable.getDefaultSearchColumn();
                }
            }

            Constraint columnConstraint = null;
            if (rc.getOperatorValues().size() >= 1) {
                for (Pair<QueryOperator, ?> p : rc.getOperatorValues()) {
                    if (columnConstraint != null) {
                        columnConstraint = new ConstraintPair(columnConstraint, BinaryOperator.OR, new ColumnConstraint(columnFromName,
                                new ColumnRefValueSqlElement(columnFromName, p.getRight()), p.getLeft()));
                    } else {
                        columnConstraint = new ColumnConstraint(columnFromName, new ColumnRefValueSqlElement(columnFromName, p.getRight()),
                                p.getLeft());
                    }
                }
            } else {
                LOG.error("Constraint on column " + columnFromName.getSqlString() + " do not have any operator/value pair. No contraint added.");
            }

            if (columnConstraint != null) {
                if (rootConstraint == null) {
                    rootConstraint = columnConstraint;
                } else {
                    rootConstraint = new ConstraintPair(rootConstraint, BinaryOperator.AND, columnConstraint);
                }
            }
        }
    }

    public static WhereElement createWhereElement(SqlQueryBuilderAccess queryBuilderAccess) {
        WhereElement whereElement = new WhereElement(queryBuilderAccess);
        final QueryParameter queryParameter = queryBuilderAccess.getQueryParameter();
        for (RawConstraint<?> rc : queryParameter.getConstraints()) {
            DimensionTable dim = queryBuilderAccess.getStarSchemaConfig().getDimensionByName(rc.getDimensionName(), queryParameter.getTimezoneName());
            if (dim != null) {
                whereElement.addDimensionConstraint(dim, rc);
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Dimension named " + rc.getDimensionName() + " not exist.");
                }
            }
        }
        return whereElement;
    }

    public Constraint getConstraints() {
        return rootConstraint;
    }

    @Override
    public Collection<? extends SqlTable> getSqlTables() {
        return tables;
    }

}
