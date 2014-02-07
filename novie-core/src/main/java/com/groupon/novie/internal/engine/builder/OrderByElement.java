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
import java.util.List;
import java.util.Set;

import com.groupon.novie.internal.engine.QuerySortConstraint;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.groupon.novie.internal.engine.OrderByDirection;
import com.groupon.novie.internal.engine.QueryParameter;
import com.groupon.novie.internal.engine.schema.AbstractSqlColumn;
import com.groupon.novie.internal.engine.schema.DimensionTable;
import com.groupon.novie.internal.engine.schema.SqlTable;

public final class OrderByElement extends SqlQueryBuilderElement {

    private List<Pair<AbstractSqlColumn, OrderByDirection>> orderByConstraint;
    private Set<SqlTable> tables;
    private static final Logger LOG = LoggerFactory.getLogger(OrderByElement.class);

    private OrderByElement(SqlQueryBuilderAccess queryBuilderAccess) {
        super(queryBuilderAccess);
        orderByConstraint = Lists.newArrayList();
        tables = Sets.newHashSet();
    }

    private void addOrderByConstraint(AbstractSqlColumn column, OrderByDirection orderByDirection) {
        if (queryBuilderAccess.getSelectElement().getSqlTableColumns().containsKey(column.getColumnIdentifier())) {
            tables.add(column.getSqlTable());
            orderByConstraint.add(ImmutablePair.of(column, orderByDirection == null ? OrderByDirection.ASC : orderByDirection));
        } else {
            LOG.warn("Column named {} is not in the select element", column.getSqlString());
        }
    }

    public static Optional<OrderByElement> createOrderByElement(final SqlQueryBuilderAccess queryBuilderAccess) {
        final QueryParameter queryParameter = queryBuilderAccess.getQueryParameter();
        if (queryParameter.getOrders().isEmpty()) {
            return Optional.absent();
        }
        OrderByElement orderByElement = new OrderByElement(queryBuilderAccess);

        for (QuerySortConstraint querySortConstraint : queryParameter.getOrders()) {

            if (queryBuilderAccess.getStarSchemaConfig().isValidDimension(querySortConstraint.getDimensionName())) {
                // Valid dimenison
                final DimensionTable dimension = queryBuilderAccess.getStarSchemaConfig().getDimensionByName(querySortConstraint.getDimensionName(),
                        queryParameter.getTimezoneName());
                AbstractSqlColumn columnFromName = null;
                if (StringUtils.isBlank(querySortConstraint.getInformationName())) {
                    columnFromName = dimension.getDefaultSortColumn().isPresent() ? dimension.getDefaultSortColumn().get() : dimension
                            .getDefaultGroupByColumn();
                } else {
                    AbstractSqlColumn informationColumn = dimension.getSqlTableColumnByInformationName(querySortConstraint.getInformationName());
                    if (informationColumn != null) {
                        columnFromName = informationColumn;
                    } else {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Dimension " + dimension.getDimensionName() + " don't have information named: " + querySortConstraint.getInformationName()
                                    + "return default.");
                        }
                        columnFromName = dimension.getDefaultSortColumn().isPresent() ? dimension.getDefaultSortColumn().get() : dimension
                                .getDefaultGroupByColumn();
                    }
                }
                orderByElement.addOrderByConstraint(columnFromName, querySortConstraint.getOrderByDirection());

            } else {
                final Optional<AbstractSqlColumn> measure = queryBuilderAccess.getStarSchemaConfig().getMeasuresByName(querySortConstraint.getDimensionName());
                if (measure.isPresent()) {
                    orderByElement.addOrderByConstraint(measure.get(), querySortConstraint.getOrderByDirection());
                } else {
                    LOG.warn("Dimension or measure named " + querySortConstraint.getDimensionName() + " does not exist.");
                }
            }
        }
        if (orderByElement.orderByConstraint.isEmpty()) {
            return Optional.absent();
        }
        return Optional.of(orderByElement);
    }

    public StringBuilder getSqlString() {
        StringBuilder sb = new StringBuilder();
        for (Pair<AbstractSqlColumn, OrderByDirection> element : orderByConstraint) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(element.getLeft().getAlias()).append(" ").append(element.getRight());
        }
        return sb;
    }

    @Override
    public Collection<? extends SqlTable> getSqlTables() {
        return tables;
    }

}
