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
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.groupon.novie.SchemaDefinition;
import com.groupon.novie.internal.engine.QueryParameter;
import com.groupon.novie.internal.engine.schema.AbstractSqlColumn;
import com.groupon.novie.internal.engine.schema.DimensionTable;
import com.groupon.novie.internal.engine.schema.SqlTable;

/**
 *
 */
public final class GroupByElement extends SqlQueryBuilderElement {

    /*
     * List<Pair<GroupingColumn, ResultCoulumn>>
     */
    List<Pair<AbstractSqlColumn, List<AbstractSqlColumn>>> groupingColumns;
    Set<SqlTable> tables = new HashSet<SqlTable>();
    private static final Logger LOG = LoggerFactory.getLogger(GroupByElement.class);

    private GroupByElement(SqlQueryBuilderAccess queryBuilderAccess) {
        super(queryBuilderAccess);
        groupingColumns = Lists.newArrayList();
        tables = Sets.newHashSet();
    }

    private void addDimension(DimensionTable dim, String informationName) {
        AbstractSqlColumn informationColumn = null;
        if (informationName != null) {
            informationColumn = dim.getSqlTableColumnByInformationName(informationName);
        }
        if (informationColumn == null) {
            groupingColumns.add(Pair.of(dim.getDefaultGroupByColumn(), dim.getInformationColumns()));
        } else {
            List<AbstractSqlColumn> info = new ArrayList<AbstractSqlColumn>(1);
            info.add(informationColumn);
            groupingColumns.add(Pair.of(informationColumn, info));
        }
        tables.add(dim);
    }

    @Override
    public Collection<? extends SqlTable> getSqlTables() {
        return tables;
    }

    public StringBuilder getSqlString() {

        StringBuilder sb = new StringBuilder();
        for (Pair<AbstractSqlColumn, List<AbstractSqlColumn>> groupingColumn : groupingColumns) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(groupingColumn.getLeft().getSqlString());
        }
        return sb;
    }

    public List<Pair<AbstractSqlColumn, List<AbstractSqlColumn>>> getGroupingColumns() {
        return Collections.unmodifiableList(groupingColumns);
    }

    public static Optional<GroupByElement> createGroupByElement(SqlQueryBuilderAccess queryBuilderAccess) {
        final QueryParameter queryParameter = queryBuilderAccess.getQueryParameter();
        if (queryParameter.getGroups().isEmpty()) {
            return Optional.absent();
        }
        GroupByElement groupByElement = new GroupByElement(queryBuilderAccess);
        final SchemaDefinition starSchemaConfig = queryBuilderAccess.getStarSchemaConfig();
        for (Pair<String, String> group : queryParameter.getGroups()) {
            if (!StringUtils.isBlank(group.getLeft())) {
                DimensionTable dim = starSchemaConfig.getDimensionByName(group.getLeft(), queryParameter.getTimezoneName());
                if (dim != null) {
                    groupByElement.addDimension(dim, group.getRight());

                } else {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Dimension named " + group.getLeft() + " not exist.");
                    }
                }
            }
        }
        if (groupByElement.groupingColumns.isEmpty()) {
            throw new IllegalArgumentException("No valid groups");
        }
        return Optional.of(groupByElement);
    }

}
