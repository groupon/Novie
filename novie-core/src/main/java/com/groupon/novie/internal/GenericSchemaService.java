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
package com.groupon.novie.internal;

import com.groupon.novie.SchemaDefinition;
import com.groupon.novie.internal.exception.InvalidParameterException;
import com.groupon.novie.internal.exception.ServiceException;
import com.groupon.novie.internal.response.Report;
import com.groupon.novie.internal.validation.QueryParameterEnvelope;

/**
 * <p>
 * Generic interface definition for the star schema service.
 * </p>
 *
 * @author ricardo
 * @since July 1, 2013
 */
public interface GenericSchemaService {

    /**
     * <p>
     * Produces a report based on a specific input input parameters.
     * </p>
     *
     * @param config Current Novie configuration
     * @param parametersEnvelope the HTTP query parameters parsed and formated
     * @return A report object that includes all the summaries and all the records
     * @throws ServiceException
     * @throws InvalidParameterException
     */
    public Report generateReport(SchemaDefinition config, QueryParameterEnvelope parametersEnvelope) throws ServiceException,
            InvalidParameterException;

}
