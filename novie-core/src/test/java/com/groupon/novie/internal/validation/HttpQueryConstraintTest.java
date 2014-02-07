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

import java.util.List;

import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.junit.Assert;
import org.junit.Test;

import com.groupon.novie.SchemaDefinition;
import com.groupon.novie.internal.engine.QueryOperator;
import com.groupon.novie.internal.engine.QueryParameter;
import com.groupon.novie.internal.engine.constraint.RawConstraint;
import com.groupon.novie.internal.exception.InvalidParameterException;

public class HttpQueryConstraintTest {

    @Mocked
    SchemaDefinition schemaDef;

    @Test
    public void testEqual() throws Exception {

        new NonStrictExpectations() {
            {
                schemaDef.isMandatoryDimension(anyString);
                result = false;
            }
        };

        QueryParameter qp = new QueryParameter();
        HttpQueryConstraint c = new HttpQueryConstraint(schemaDef, "dim", "value");
        c.addToQuery(qp);
        List<RawConstraint<?>> constraints = qp.getConstraints();
        Assert.assertEquals(constraints.size(), 1);
        Assert.assertEquals(constraints.get(0).getDimensionName(), "DIM");
        Assert.assertNull(constraints.get(0).getInformationName());
        Assert.assertEquals(constraints.get(0).getOperatorValues().size(), 1);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getKey(), QueryOperator.EQUAL);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getValue(), "value");

    }

    @Test
    public void testEqualEmpty() throws Exception {

        new NonStrictExpectations() {
            {
                schemaDef.isMandatoryDimension(anyString);
                result = false;
            }
        };

        QueryParameter qp = new QueryParameter();
        HttpQueryConstraint c = new HttpQueryConstraint(schemaDef, "dim", "");
        c.addToQuery(qp);
        List<RawConstraint<?>> constraints = qp.getConstraints();
        Assert.assertEquals(constraints.size(), 1);
        Assert.assertEquals(constraints.get(0).getDimensionName(), "DIM");
        Assert.assertNull(constraints.get(0).getInformationName());
        Assert.assertEquals(constraints.get(0).getOperatorValues().size(), 1);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getKey(), QueryOperator.EQUAL);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getValue(), "");

    }

    @Test
    public void testNegative() throws Exception {
        new NonStrictExpectations() {
            {
                schemaDef.isMandatoryDimension(anyString);
                result = false;
            }
        };
        QueryParameter qp = new QueryParameter();
        HttpQueryConstraint c = new HttpQueryConstraint(schemaDef, "dim", "!value");
        c.addToQuery(qp);
        List<RawConstraint<?>> constraints = qp.getConstraints();
        Assert.assertEquals(constraints.size(), 1);
        Assert.assertEquals(constraints.get(0).getDimensionName(), "DIM");
        Assert.assertNull(constraints.get(0).getInformationName());
        Assert.assertEquals(constraints.get(0).getOperatorValues().size(), 1);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getKey(), QueryOperator.NOT_EQUAL);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getValue(), "value");

    }

    @Test
    public void testLike() throws Exception {
        new NonStrictExpectations() {
            {
                schemaDef.isMandatoryDimension(anyString);
                result = false;
            }
        };
        QueryParameter qp = new QueryParameter();
        HttpQueryConstraint c = new HttpQueryConstraint(schemaDef, "dim", "val*ue");
        c.addToQuery(qp);
        List<RawConstraint<?>> constraints = qp.getConstraints();
        Assert.assertEquals(constraints.size(), 1);
        Assert.assertEquals(constraints.get(0).getDimensionName(), "DIM");
        Assert.assertNull(constraints.get(0).getInformationName());
        Assert.assertEquals(constraints.get(0).getOperatorValues().size(), 1);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getKey(), QueryOperator.LIKE);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getValue(), "val%ue");

    }

    @Test
    public void testNotLike() throws Exception {
        new NonStrictExpectations() {
            {
                schemaDef.isMandatoryDimension(anyString);
                result = false;
            }
        };
        QueryParameter qp = new QueryParameter();
        HttpQueryConstraint c = new HttpQueryConstraint(schemaDef, "dim", "!val*ue");
        c.addToQuery(qp);
        List<RawConstraint<?>> constraints = qp.getConstraints();
        Assert.assertEquals(constraints.size(), 1);
        Assert.assertEquals(constraints.get(0).getDimensionName(), "DIM");
        Assert.assertNull(constraints.get(0).getInformationName());
        Assert.assertEquals(constraints.get(0).getOperatorValues().size(), 1);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getKey(), QueryOperator.NOT_LIKE);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getValue(), "val%ue");
    }

    @Test
    public void testGreater() throws Exception {
        new NonStrictExpectations() {
            {
                schemaDef.isMandatoryDimension(anyString);
                result = false;
            }
        };
        QueryParameter qp = new QueryParameter();
        HttpQueryConstraint c = new HttpQueryConstraint(schemaDef, "dim", "]value");
        c.addToQuery(qp);
        List<RawConstraint<?>> constraints = qp.getConstraints();
        Assert.assertEquals(constraints.size(), 1);
        Assert.assertEquals(constraints.get(0).getDimensionName(), "DIM");
        Assert.assertNull(constraints.get(0).getInformationName());
        Assert.assertEquals(constraints.get(0).getOperatorValues().size(), 1);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getKey(), QueryOperator.GREATER_THAN);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getValue(), "value");

    }

    @Test
    public void testGreaterOrEqual() throws Exception {
        new NonStrictExpectations() {
            {
                schemaDef.isMandatoryDimension(anyString);
                result = false;
            }
        };
        QueryParameter qp = new QueryParameter();
        HttpQueryConstraint c = new HttpQueryConstraint(schemaDef, "dim", "[value");
        c.addToQuery(qp);
        List<RawConstraint<?>> constraints = qp.getConstraints();
        Assert.assertEquals(constraints.size(), 1);
        Assert.assertEquals(constraints.get(0).getDimensionName(), "DIM");
        Assert.assertNull(constraints.get(0).getInformationName());
        Assert.assertEquals(constraints.get(0).getOperatorValues().size(), 1);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getKey(), QueryOperator.GREATER_THAN_OR_EQUAL);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getValue(), "value");

    }

    @Test
    public void testLess() throws Exception {
        new NonStrictExpectations() {
            {
                schemaDef.isMandatoryDimension(anyString);
                result = false;
            }
        };
        QueryParameter qp = new QueryParameter();
        HttpQueryConstraint c = new HttpQueryConstraint(schemaDef, "dim", "value[");
        c.addToQuery(qp);
        List<RawConstraint<?>> constraints = qp.getConstraints();
        Assert.assertEquals(constraints.size(), 1);
        Assert.assertEquals(constraints.get(0).getDimensionName(), "DIM");
        Assert.assertNull(constraints.get(0).getInformationName());
        Assert.assertEquals(constraints.get(0).getOperatorValues().size(), 1);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getKey(), QueryOperator.LESS_THAN);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getValue(), "value");

    }

    @Test
    public void testLessOrEqual() throws Exception {
        QueryParameter qp = new QueryParameter();
        HttpQueryConstraint c = new HttpQueryConstraint(schemaDef, "dim.info", "value]");
        c.addToQuery(qp);
        List<RawConstraint<?>> constraints = qp.getConstraints();
        Assert.assertEquals(constraints.size(), 1);
        Assert.assertEquals(constraints.get(0).getDimensionName(), "DIM");
        Assert.assertEquals(constraints.get(0).getInformationName(), "INFO");
        Assert.assertEquals(constraints.get(0).getOperatorValues().size(), 1);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getKey(), QueryOperator.LESS_THAN_OR_EQUAL);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getValue(), "value");

    }

    @Test
    public void testEmptyValueOnDim() throws Exception {
        new NonStrictExpectations() {
            {
                schemaDef.isMandatoryDimension(anyString);
                result = true;
            }
        };
        QueryParameter qp = new QueryParameter();
        HttpQueryConstraint c = new HttpQueryConstraint(schemaDef, "dim", "");
        c.addToQuery(qp);
        List<RawConstraint<?>> constraints = qp.getConstraints();
        Assert.assertEquals(constraints.size(), 1);
        Assert.assertEquals(constraints.get(0).getDimensionName(), "DIM");
        Assert.assertEquals(constraints.get(0).getOperatorValues().size(), 1);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getKey(), QueryOperator.EQUAL);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getValue(), "");
    }

    @Test
    public void testNotEmptyValueOnDim() throws Exception {
        new NonStrictExpectations() {
            {
                schemaDef.isMandatoryDimension(anyString);
                result = true;
            }
        };
        QueryParameter qp = new QueryParameter();
        HttpQueryConstraint c = new HttpQueryConstraint(schemaDef, "dim", "!");
        c.addToQuery(qp);
        List<RawConstraint<?>> constraints = qp.getConstraints();
        Assert.assertEquals(constraints.size(), 1);
        Assert.assertEquals(constraints.get(0).getDimensionName(), "DIM");
        Assert.assertEquals(constraints.get(0).getOperatorValues().size(), 1);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getKey(), QueryOperator.NOT_EQUAL);
        Assert.assertEquals(constraints.get(0).getOperatorValues().get(0).getValue(), "");
    }

    @Test(expected = InvalidParameterException.class)
    public void testInvalideValue() throws Exception {
        new NonStrictExpectations() {
            {
                schemaDef.isMandatoryDimension(anyString);
                result = false;
            }
        };
        QueryParameter qp = new QueryParameter();
        HttpQueryConstraint c = new HttpQueryConstraint(schemaDef, "dim", "[[value");
        c.addToQuery(qp);
    }

    @Test(expected = InvalidParameterException.class)
    public void testBadLike() throws Exception {
        new NonStrictExpectations() {
            {
                schemaDef.isMandatoryDimension(anyString);
                result = false;
            }
        };
        QueryParameter qp = new QueryParameter();
        HttpQueryConstraint c = new HttpQueryConstraint(schemaDef, "dim", "[val*ue");
        c.addToQuery(qp);
    }

    @Test(expected = InvalidParameterException.class)
    public void testBadLike2() throws Exception {
        new NonStrictExpectations() {
            {
                schemaDef.isMandatoryDimension(anyString);
                result = false;
            }
        };
        QueryParameter qp = new QueryParameter();
        HttpQueryConstraint c = new HttpQueryConstraint(schemaDef, "dim", "val*ue[");
        c.addToQuery(qp);
    }

    @Test(expected = InvalidParameterException.class)
    public void testBadValue() throws Exception {
        new NonStrictExpectations() {
            {
                schemaDef.isMandatoryDimension(anyString);
                result = false;
            }
        };
        QueryParameter qp = new QueryParameter();
        HttpQueryConstraint c = new HttpQueryConstraint(schemaDef, "dim", "[value]");
        c.addToQuery(qp);
    }

    @Test(expected = InvalidParameterException.class)
    public void testBadDimInfo() throws Exception {
        new NonStrictExpectations() {
            {
                schemaDef.isMandatoryDimension(anyString);
                result = false;
            }
        };
        QueryParameter qp = new QueryParameter();
        HttpQueryConstraint c = new HttpQueryConstraint(schemaDef, "dim-ingo", "value");
        c.addToQuery(qp);
    }

    @Test(expected = InvalidParameterException.class)
    public void testFullLikeMandatoryDim() throws Exception {
        new NonStrictExpectations() {
            {
                schemaDef.isMandatoryDimension(anyString);
                result = true;
            }
        };
        QueryParameter qp = new QueryParameter();
        HttpQueryConstraint c = new HttpQueryConstraint(schemaDef, "dim", "*");
        c.addToQuery(qp);
    }

    @Test
    public void testLikeMandatoryDim() throws Exception {
        new NonStrictExpectations() {
            {
                schemaDef.isMandatoryDimension(anyString);
                result = true;
            }
        };
        QueryParameter qp = new QueryParameter();
        HttpQueryConstraint c = new HttpQueryConstraint(schemaDef, "dim", "T*");
        c.addToQuery(qp);
    }
}
