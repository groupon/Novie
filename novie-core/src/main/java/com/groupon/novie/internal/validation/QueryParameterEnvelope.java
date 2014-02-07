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
package com.groupon.novie.internal.validation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.groupon.novie.internal.GenericSchemaService;

/**
 * <p>
 * Wraps all the {@link QueryParameterAware} objects properly separated to be
 * delivered to the {@link GenericSchemaService}.
 * </p>
 *
 * @author damiano
 * @author ricardo
 */
public class QueryParameterEnvelope {

    /**
     *
     */
    private Set<QueryParameterAware> constraints;

    /**
     *
     */
    private Set<QueryParameterAware> aggregations;

    /**
     *
     */
    private QueryParameterAware timeZoneConstraint;

    /**
     *
     */
    private QueryParameterAware pageSize;

    /**
     *
     */
    private QueryParameterAware pageNumber;

    public QueryParameterEnvelope() {
        this.constraints = Sets.newLinkedHashSet();
        this.aggregations = Sets.newLinkedHashSet();
        this.timeZoneConstraint = new TimeZoneConstraint("UTC");
    }

    public Set<QueryParameterAware> getConstraints() {
        return constraints;
    }

    public void addConstraints(QueryParameterAware constraint) {
        this.constraints.add(constraint);
    }

    public Set<QueryParameterAware> getAggregations() {
        return aggregations;
    }

    public void addAggregations(QueryParameterAware aggregation) {
        aggregations.add(aggregation);
    }

    public QueryParameterAware getTimeZoneConstraint() {
        return timeZoneConstraint;
    }

    public void setTimeZoneConstraint(TimeZoneConstraint timeZoneConstraint) {
        this.timeZoneConstraint = timeZoneConstraint;
    }

    public QueryParameterAware getPageSize() {
        return pageSize;
    }

    public void setPageSize(QueryParameterAware pageSize) {
        this.pageSize = pageSize;
    }

    public QueryParameterAware getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(QueryParameterAware pageNumber) {
        this.pageNumber = pageNumber;
    }
}
