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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.groupon.novie.SchemaDefinition;
import com.groupon.novie.internal.engine.QueryOperator;
import com.groupon.novie.internal.engine.QueryParameter;
import com.groupon.novie.internal.exception.InvalidParameterException;

/**
 * <p>
 * Keeps the data for a to be {@link QueryParameter} constraint.
 * </p>
 *
 * @author ricardo, ttrolez
 */
public class HttpQueryConstraint implements QueryParameterAware {

    /**
     * <p>
     * This pattern assures that all parameter values follow the underlying
     * engine convention and groups each part for easier processing by the next
     * layer.
     * </p>
     * <p/>
     * Examples:
     * <ul>
     * <li>[2013-01-01</li>
     * <li>!other-deal</li>
     * <li>[2013-01-01]</li>
     * </ul>
     */
    private static final Pattern VALUE_PATTERN = Pattern.compile("([!\\]\\[])?([^\\[\\]]*)([\\]\\[])?");

    private static final Pattern DIM_MEASURE_PATTERN = Pattern.compile("(\\w+)(?:\\.(\\w+))?");

    private SchemaDefinition schemaDef;
    private String key;
    private String valuesStr;

    /**
     * @param key      the left part of the http query parameter
     * @param valueStr the right part of the http query parameter
     * @throws InvalidParameterException
     */
    public HttpQueryConstraint(SchemaDefinition schemaDef, String key, String valueStr) throws InvalidParameterException {
        this.schemaDef = schemaDef;
        this.key = key;
        this.valuesStr = valueStr;
    }

    @Override
    public void addToQuery(QueryParameter parameter) throws InvalidParameterException {
        Matcher matcher = DIM_MEASURE_PATTERN.matcher(key);
        if (!matcher.matches()) {
            throw new InvalidParameterException("Invalid parameter " + key.toUpperCase());
        }

        String dimension = matcher.group(1);
        String info = matcher.group(2);

        List<Pair<QueryOperator, String>> constraints = new ArrayList<Pair<QueryOperator, String>>();
        for (String constraint : valuesStr.split(",")) {
            if (StringUtils.isNotBlank(constraint)) {
                matcher = VALUE_PATTERN.matcher(constraint);
                if (!matcher.matches()) {
                    throw new InvalidParameterException("Invalid value for parameter " + key.toUpperCase());

                }
                if (matcher.group(1) != null && matcher.group(3) != null) {
                    throw new InvalidParameterException("Invalid value for parameter " + key.toUpperCase());
                }

                Pair<QueryOperator, String> operatorConstraint = createConstraint(matcher);
                if (schemaDef.isMandatoryDimension(dimension)
                        && ((operatorConstraint.getKey() == QueryOperator.LIKE) || (operatorConstraint.getKey() == QueryOperator.NOT_LIKE))
                        && "%".equals(operatorConstraint.getValue())) {
                    throw new InvalidParameterException("Invalid value for mandatory constraint " + dimension.toUpperCase() + ". Can't be a full \"like\".");
                }
                constraints.add(operatorConstraint);

            } else {
                constraints.add(Pair.of(QueryOperator.EQUAL, ""));
            }
        }
        parameter.addConstraint(dimension, info, constraints);
    }

    private Pair<QueryOperator, String> createConstraint(Matcher matcher) throws InvalidParameterException {
        if (matcher.group(1) != null) {
            // Treatment for !,[ or ] at the beginning
            if (matcher.group(1).charAt(0) != '!' && matcher.group(2).contains("*")) {
                throw new InvalidParameterException("Invalid value for parameter " + key.toUpperCase());
            }
            switch (matcher.group(1).charAt(0)) {
                case '[':
                    return Pair.of(QueryOperator.GREATER_THAN_OR_EQUAL, matcher.group(2));
                case ']':
                    return Pair.of(QueryOperator.GREATER_THAN, matcher.group(2));
                case '!':
                    QueryOperator qo = (matcher.group(2).contains("*")) ? QueryOperator.NOT_LIKE : QueryOperator.NOT_EQUAL;
                    // Treatment for not like constraint
                    return Pair.of(qo, matcher.group(2).replaceAll("\\*", "%"));
                default:
                    //Nothing to do case not possible cause the regex
            }
        }
        if (matcher.group(3) != null) {
            // Treatment for [ or ] at the end
            if (matcher.group(2).contains("*")) {
                throw new InvalidParameterException("Invalid value for parameter " + key.toUpperCase());
            }
            switch (matcher.group(3).charAt(0)) {
                case '[':
                    return Pair.of(QueryOperator.LESS_THAN, matcher.group(2));
                case ']':
                    return Pair.of(QueryOperator.LESS_THAN_OR_EQUAL, matcher.group(2));
                default:
                    //Nothing to do case not possible cause the regex
            }

        }
        if (matcher.group(2).contains("*")) {
            // Treatment for like constraint
            return Pair.of(QueryOperator.LIKE, matcher.group(2).replaceAll("\\*", "%"));
        }
        // Because the entry is a matcher the last could only be a =
        return Pair.of(QueryOperator.EQUAL, matcher.group(2));

    }
}
