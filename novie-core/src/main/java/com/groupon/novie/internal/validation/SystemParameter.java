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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.groupon.novie.SchemaDefinition;
import com.groupon.novie.internal.exception.InvalidParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Defines domain specific parameters common to all start schemas.
 * </p>
 * <p/>
 * <p>
 * The {@link DomainParserAware} and {@link SchemaAware} Interfaces allow each
 * type to parse and validate itself against a {@link SchemaDefinition}
 * </p>
 *
 * The enum name are not in uppercase because we need to retrieve them from the HTTP query parameter and it's
 * a way faster to access an enum using its name.
 *
 * @author ricardo
 * @author damiano
 * @since July 3, 2013
 */

public enum SystemParameter implements DomainParserAware {

    group {
        /**
         * <p>
         * Returns a {@link Set} of {@link QueryParameterAware} with an
         * {@link AggregationConstraint} implementation.
         * </p>
         *
         */
        @Override
        public void parseValue(String value, SchemaDefinition config, QueryParameterEnvelope queryParameterDto) throws InvalidParameterException {

            for (String agg : value.split("\\|")) {
                Matcher matcher = GROUP_PATTERN.matcher(agg);
                if (!matcher.matches()) {
                    throw new InvalidParameterException("Invalid dimension: " + agg);
                } else if (!config.isValidDimension(matcher.group(1), matcher.group(3))) {
                    throw new InvalidParameterException("Invalid dimension: " + agg);
                }
                QueryParameterAware constraint = new AggregationConstraint(matcher.group(1), matcher.group(3));

                if (queryParameterDto.getAggregations().contains(constraint)) {
                    throw new InvalidParameterException("Invalid multiple groups over dimension: " + agg);
                }
                queryParameterDto.addAggregations(constraint);
            }
        }
    },
    sort {
        /**
         * <p>
         * Returns a {@link Set} of {@link QueryParameterAware} with an
         * {@link OrderConstraint} implementation.
         * </p>
         *
         * <p>
         * The Set assures that no duplicates are allowed and the concrete
         * implementation knows how to generate the query parameter.
         * </p>
         *
         */
        @Override
        public void parseValue(String value, SchemaDefinition config, QueryParameterEnvelope queryParameterDto) throws InvalidParameterException {

            for (String agg : value.split("\\|")) {
                Matcher matcher = ORDER_PATTERN.matcher(agg);
                if (!matcher.matches()) {
                    throw new InvalidParameterException("Invalid dimension: " + agg);
                } else if (!(config.isValidDimension(matcher.group(2), matcher.group(4)) || config.isValidMeasure(matcher.group(2)))) {
                    throw new InvalidParameterException("Invalid dimension: " + agg);
                }
                QueryParameterAware constraint = new OrderConstraint(matcher.group(1), matcher.group(2), matcher.group(4));
                if (queryParameterDto.getAggregations().contains(constraint)) {
                    throw new InvalidParameterException("Invalid sorts over the same dimension " + agg);
                }
                queryParameterDto.addAggregations(constraint);
            }

        }

    },
    timezone {
        @Override
        public void parseValue(String value, SchemaDefinition config, QueryParameterEnvelope queryParameterDto) throws InvalidParameterException {

            if (config.isTimeZoneSupported(value)) {
                queryParameterDto.setTimeZoneConstraint(new TimeZoneConstraint(value));
            } else {
                throw new InvalidParameterException("Timezone " + value + " not supported.");
            }

        }

    },
    pageSize {
        public void parseValue(String value, SchemaDefinition config, QueryParameterEnvelope queryParameterDto) throws InvalidParameterException {

            try {
                final Integer pageSize = Integer.valueOf(value);
                if (pageSize <= 0) {
                    throw new InvalidParameterException("pageSize " + value + " is not valid.");
                }
                queryParameterDto.setPageSize(new PageSizeConstraint(pageSize));
            } catch (NumberFormatException e) {
                LOG.error("pageSize " + value + " is not valid.",e);
                throw new InvalidParameterException("pageSize " + value + " is not valid.");
            }
        }

    },
    page {
        public void parseValue(String value, SchemaDefinition config, QueryParameterEnvelope queryParameterDto) throws InvalidParameterException {
            try {
                final Integer page = Integer.valueOf(value);
                if (page <= 0) {
                    throw new InvalidParameterException("Page Number " + value + " is not valid.");
                }
                queryParameterDto.setPageNumber(new PageNumberConstraint(page));
            } catch (NumberFormatException e) {
                LOG.error("Page Number " + value + " is not valid.",e);
                throw new InvalidParameterException("Page Number " + value + " is not valid.");
            }
        }

    };

    private static final Logger LOG = LoggerFactory.getLogger(SystemParameter.class);

    private static final Pattern GROUP_PATTERN = Pattern.compile("(\\w+)(\\.(\\w+))?");

    private static final Pattern ORDER_PATTERN = Pattern.compile("(-)?(\\w+)(\\.(\\w+))?");

}
