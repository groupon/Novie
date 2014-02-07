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
package com.groupon.novie.internal.engine.constraint;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.groupon.novie.internal.engine.QueryOperator;
import com.groupon.novie.internal.engine.builder.ReferenceSqlConstraint;
import com.groupon.novie.internal.engine.schema.AbstractSqlColumn;

/**
 * @author damiano
 * @since 6 June 2013
 */

public class ColumnConstraint extends ConstraintOperator<AbstractSqlColumn, ReferenceSqlConstraint<?>> {

    public ColumnConstraint(AbstractSqlColumn firstOperand, ReferenceSqlConstraint<?> secondOperand, QueryOperator operator) {
        super(firstOperand, secondOperand, operator);
    }

    @Override
    public StringBuilder generateConstraint(MapSqlParameterSource mapSqlParameterSource) {
        secondOperand.setDelta(mapSqlParameterSource.getValues().size());
        mapSqlParameterSource.addValue(secondOperand.getReference(), secondOperand.getValue(), firstOperand.getColumnType().getSqlType());
        return new StringBuilder(firstOperand.getSqlString()).append(getOperatorString()).append(secondOperand.getSqlString());
    }

}
