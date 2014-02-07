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
package com.groupon.novie.internal.engine;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.groupon.novie.internal.engine.constraint.RawConstraint;


/**
 * This class is the whole query constraints/parameters bean.
 * It contains all the required information, decoded from the HTTP query, needed to build the SQL query.
 *
 * @author thomas
 * @author ricardo
 * @author damiano
 */
public class QueryParameter {

    public static enum QueryParameterKind {
        CONSTRAINTS, ORDERS, GROUPS, TIMEZONE, PAGE,
    }

    private List<RawConstraint<?>> rawConstraints;
    // List of Order,DimensionName/Measure,InfoName. InfoName can be null
    private List<QuerySortConstraint> orders;
    // List of DimensionName,InfoName. InfoName can be null
    private List<Pair<String, String>> groups;

    // Timezone name. if null, use the default one
    private String timezoneName = null;

    private int page;
    private int pageSize;

    public QueryParameter() {
        rawConstraints = Lists.newArrayList();
        orders = Lists.newArrayList();
        groups = Lists.newArrayList();
    }

    public <T> void addConstraint(final String dimensionName, final String informationName, final List<Pair<QueryOperator, T>> operatorValues) {
        if (dimensionName == null) {
            throw new IllegalArgumentException("dimensionName can not be null");
        }
        rawConstraints.add(new RawConstraint<T>(dimensionName.toUpperCase(), (informationName == null) ? null : informationName.toUpperCase(),
                operatorValues));
    }

    public void addConstraint(final String dimensionName, final String informationName, final QueryOperator op, final Object value) {
        if (dimensionName == null) {
            throw new IllegalArgumentException("dimensionName can not be null");
        }
        rawConstraints.add(new RawConstraint<Object>(dimensionName.toUpperCase(), (informationName == null) ? null : informationName.toUpperCase(),
                op, value));
    }

    /**
     * Add list of grouping dimension which use the default grouping info
     *
     * @param dimensionName VarArgs list of the dimension names
     */
    public void addGroups(final String... dimensionName) {
        for (String dn : dimensionName) {
            addGroup(dn, null);
        }
    }

    public void addGroup(final String dimensionName) {
        addGroup(dimensionName, null);
    }

    public void addGroup(final String dimensionName, final String informationName) {
        if (dimensionName == null) {
            throw new IllegalArgumentException("dimensionName can not be null");
        }
        groups.add(Pair.of(dimensionName.toUpperCase(), (informationName == null) ? null : informationName.toUpperCase()));
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    // Use unmodifiable list to force to use add method
    public List<Pair<String, String>> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    // Use unmodifiable list to force to use add method
    public List<RawConstraint<?>> getConstraints() {
        return Collections.unmodifiableList(rawConstraints);
    }

    // Use unmodifiable list to force to use add method
    public List<QuerySortConstraint> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    public String getTimezoneName() {
        return timezoneName;
    }

    public void setTimezoneName(String timezoneName) {
        this.timezoneName = timezoneName;
    }

    public void addOrderByConstraint(final OrderByDirection orderByDirection, final String dimensionName) {
        addOrderByConstraint(orderByDirection, dimensionName, null);
    }

    public void addOrderByConstraint(final OrderByDirection orderByDirection, final String dimensionName, final String informationName) {
        orders.add(new QuerySortConstraint(orderByDirection, dimensionName, informationName));
    }

    /**
     * Return a QueryParameter which contains reference to the original elements except for those from the exception list.
     *
     * @param excludedElements list of QueryParameterKind which will NOT be cloned
     * @return
     */
    public QueryParameter partialCopy(final QueryParameterKind... excludedElements) {
        List<QueryParameterKind> excludedList = Arrays.asList(excludedElements);
        QueryParameter returnValue = new QueryParameter();
        if (!excludedList.contains(QueryParameterKind.CONSTRAINTS)) {
            returnValue.rawConstraints = this.rawConstraints;
        }
        if (!excludedList.contains(QueryParameterKind.GROUPS)) {
            returnValue.groups = this.groups;
        }
        if (!excludedList.contains(QueryParameterKind.ORDERS)) {
            returnValue.orders = this.orders;
        }
        if (!excludedList.contains(QueryParameterKind.PAGE)) {
            returnValue.pageSize = this.pageSize;
            returnValue.page = this.page;
        }
        if (!excludedList.contains(QueryParameterKind.TIMEZONE)) {
            returnValue.timezoneName = this.timezoneName;
        }

        return returnValue;
    }

}
