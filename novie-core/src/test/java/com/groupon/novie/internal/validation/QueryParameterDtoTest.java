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

import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.junit.Assert;
import org.junit.Test;

import com.groupon.novie.SchemaDefinition;
import com.groupon.novie.internal.exception.InvalidParameterException;

public class QueryParameterDtoTest {

    @Mocked
    SchemaDefinition config;

    @Test(expected = InvalidParameterException.class)
    public void groupConstraintCreationTest() throws InvalidParameterException {

        new NonStrictExpectations() {
            {
                config.isValidDimension(anyString, anyString);
                result = true;
            }
        };

        DomainParserAware parameter = SystemParameter.valueOf("group");
        QueryParameterEnvelope queryParameterDto = new QueryParameterEnvelope();
        parameter.parseValue("campaign.name|affiliate.id", config, queryParameterDto);
        Assert.assertTrue(queryParameterDto.getAggregations().size() == 2);

        // should raise exception because of duplicate dimension
        parameter.parseValue("campaign.name|campaign", config, queryParameterDto);
    }

    @Test
    public void orderConstraintCreationTest() throws InvalidParameterException {

        final OrderConstraint affiliateOrder = new OrderConstraint("-", "affiliate", null);
        final OrderConstraint campaignOrder = new OrderConstraint(null, "campaign", "name");
        QueryParameterEnvelope queryParameterDto = new QueryParameterEnvelope();
        new NonStrictExpectations() {
            {
                config.isValidDimension(anyString, anyString);
                result = true;
            }
        };
        DomainParserAware parameter = SystemParameter.valueOf("sort");
        parameter.parseValue("campaign.name|-affiliate", config, queryParameterDto);
        Assert.assertTrue(queryParameterDto.getAggregations().size() == 2);
        Assert.assertTrue(queryParameterDto.getAggregations().contains(affiliateOrder));
        Assert.assertTrue(queryParameterDto.getAggregations().contains(campaignOrder));
    }
}
