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
package com.groupon.novie;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Optional;
import com.groupon.novie.engine.ColumnDataType;
import com.groupon.novie.internal.engine.schema.AbstractSqlColumn;
import com.groupon.novie.internal.engine.schema.DimensionTable;
import com.groupon.novie.internal.engine.schema.SqlTable;

/**
 * This interface represents the definition of a schema. It's accountable to provide the relevant information to the engine to
 * interpret the HTTP query, build the SQL request and build the result structure.
 *
 * @author thomas
 * @author damiano
 * @author ricardo
 */
public interface SchemaDefinition {

    /**
     * Returns true is the dimension exist and the information exists for this
     * dimension.
     *
     * @param dimensionName
     * @param informationName : can be null in this case test also if the information is
     *                        valid.
     */
    boolean isValidDimension(String dimensionName, String informationName);

    /**
     * Returns true is the dimension exist.
     *
     * @param dimensionName
     */
    boolean isValidDimension(String dimensionName);

    /**
     * Returns true is the dimension is mandatory.
     *
     * @param dimensionName
     */
    boolean isMandatoryDimension(String dimensionName);

    /**
     * <p>
     * Checks if a given name is a measure for this Implementation.
     * </p>
     *
     * @param name Name to check against available measures.
     * @return True if measure exists, false otherwise.
     */
    boolean isValidMeasure(String name);

    /**
     * Returns the dimension information type. if information is null or blank
     * return the type of the default search information.
     *
     * @param dimensionName
     * @param informationName
     * @return
     */
    ColumnDataType getInformationType(String dimensionName, String informationName);

    /**
     * Returns the endpoint name
     *
     * @return
     */
    String getEndPointName();

    /**
     * Returns a collection of mandatory dimension names.
     *
     * @return The collection of names or an empty list.
     */
    Collection<String> getMandatoryDimension();

    /**
     * Check it the specified TZ or TZ alias is supported.
     *
     * @param tzName TZ name or alias.
     */
    boolean isTimeZoneSupported(String tzName);

    public String getDefaultTimezone();

    boolean areLinked(SqlTable table1, SqlTable table2);

    boolean isLinkedToFactTable(SqlTable table);

    DimensionTable getDimensionByName(String dimensionName, String tzName);

    /**
     * <p>
     * Finds and returns an {@link Optional} of {@link AbstractSqlColumn} based
     * on the business name of a given measure.
     * </p>
     *
     * @param informationName Business name of a specific measure.
     * @return {@link Optional} of {@link AbstractSqlColumn} found inside Schema
     * Implementation.
     */
    public Optional<AbstractSqlColumn> getMeasuresByName(String informationName);

    List<AbstractSqlColumn> getMeasuresColumn();

    String getDatabaseName();

    /**
     * Returns the column associated with the informationName is defined. null
     * otherwise.
     *
     * @param dimension
     * @param informationName
     * @return
     */
    AbstractSqlColumn getInformationColumn(DimensionTable dimension, String informationName);

}
