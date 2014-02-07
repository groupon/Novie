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
package com.groupon.novie.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.groupon.novie.SchemaDefinition;
import com.groupon.novie.engine.ColumnDataType;
import com.groupon.novie.internal.engine.schema.AbstractSqlColumn;
import com.groupon.novie.internal.engine.schema.DimensionTable;
import com.groupon.novie.internal.engine.schema.FactTable;
import com.groupon.novie.internal.engine.schema.PhysicalSqlColumn;
import com.groupon.novie.internal.engine.schema.SqlTable;

/**
 * Default implementation of {@link SchemaDefinition}
 *
 * @author thomas
 */

public final class SchemaDefinitionImpl implements SchemaDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaDefinitionImpl.class);

    protected Map<String, Map<String, SqlTable>> predecessorRelation = Maps.newHashMap();

    /**
     * Map
     * <Table name, SQLTable>
     * Contains all the tables
     */
    private Map<String, SqlTable> sqlTables = Maps.newHashMap();
    /**
     * Map<DimensionName,Dimension>
     */
    private Map<String, DimensionTable> dimensions = Maps.newHashMap();
    /**
     * Map<Pair<originalDimesionName, tzName>,AlternateDimension> All
     * compatibility check must be done during the insertion.
     */
    private Map<Pair<String, String>, DimensionTable> alternateDimensions = new HashMap<Pair<String, String>, DimensionTable>();
    /**
     * Collection<DimensionName> in upper case.
     */
    private Collection<String> mandatoryDimensionNames = new HashSet<String>();

    /**
     * Set<Pair<Pair<table1,col>,Pair<table2,col>>>
     */
    private Set<Pair<Pair<String, String>, Pair<String, String>>> fkRelationshipSet = Sets.newHashSet();
    private FactTable factTable;
    private String databaseName = null;
    private String endPointName;
    /**
     * Map<tzAliase,tzName>
     */
    private Map<String, String> tzNamesAliases = new HashMap<String, String>();

    private String defaultTimezone = null;

    private SchemaDefinitionImpl(String endPointName) {
        this.endPointName = endPointName;
    }

    /**
     * Entry point to create a new instance of this implementation.
     *
     * @param endPointName
     * @return a new instance of this implementation of the
     * {@link SchemaDefinition}.
     */
    public static SchemaDefinitionImpl createInstance(String endPointName) {
        return new SchemaDefinitionImpl(endPointName);
    }

    // Interface methods

    /*
     * (non-Javadoc)
     *
     * @see
     * com.groupon.novie.SchemaConfig#areLinked(com.groupon.novie.internal.engine
     * .internal.SqlTable, com.groupon.novie.internal.engine.internal.SqlTable)
     */
    @Override
    public boolean areLinked(SqlTable table1, SqlTable table2) {
        return predecessorRelation.get(table1.getTableName()).containsKey(table2.getTableName());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.groupon.novie.SchemaConfig#isLinkedToFactTable(com.groupon.novie.
     * internal.engine.internal.SqlTable)
     */
    @Override
    public boolean isLinkedToFactTable(SqlTable table) {
        return areLinked(table, factTable);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.groupon.novie.SchemaConfig#getDimensionByName(java.lang.String,
     * java.lang.String)
     */
    @Override
    public DimensionTable getDimensionByName(String dimensionName, String tzNameAlias) {
        DimensionTable dimensionTable;
        if (StringUtils.isBlank(tzNameAlias) || (StringUtils.isNotBlank(tzNameAlias) && tzNameAlias.equals(getDefaultTimezone()))) {
            dimensionTable = dimensions.get(dimensionName.toUpperCase());
        } else {
            if (!isTimeZoneSupported(tzNameAlias)) {
                LOG.error(tzNameAlias + " TZ is not supported, do not consider it.");
                dimensionTable = dimensions.get(dimensionName.toUpperCase());
            } else {
                String tzName = tzNamesAliases.get(tzNameAlias);
                dimensionTable = alternateDimensions.get(Pair.of(dimensionName.toUpperCase(), tzName));
                if (dimensionTable == null) {
                    // No alternateTable for this dimension/tz pair
                    dimensionTable = dimensions.get(dimensionName.toUpperCase());
                }
            }

        }
        if (dimensionTable == null) {
            throw new IllegalArgumentException("Invalid SqlTable for the dimension: " + dimensionName);
        }
        return dimensionTable;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.groupon.novie.SchemaConfig#getMeasuresColumn()
     */
    @Override
    public List<AbstractSqlColumn> getMeasuresColumn() {
        return Collections.unmodifiableList(Lists.newArrayList(factTable.getMeasuresColumn().values()));
    }

    /**
     * {@inheritDoc SchemaDefinition}
     * {@link SchemaDefinition#getMeasuresByName(String)}
     */
    @Override
    public Optional<AbstractSqlColumn> getMeasuresByName(String informationName) {
        for (AbstractSqlColumn column : factTable.getMeasuresColumn().values()) {
            if (column.getBusinessName().equalsIgnoreCase(informationName)) {
                return Optional.of(column);
            }
        }
        return Optional.absent();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.groupon.novie.SchemaConfig#getDatabaseName()
     */
    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.groupon.novie.SchemaConfig#getEndPointName()
     */
    @Override
    public String getEndPointName() {
        return endPointName;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.groupon.novie.SchemaConfig#getInformationColumn(com.groupon.novie
     * .internal.engine.internal.DimensionTable, java.lang.String)
     */
    @Override
    public AbstractSqlColumn getInformationColumn(DimensionTable dimension, String informationName) {
        final AbstractSqlColumn sqlTableColumn = dimension.getSqlTableColumnByInformationName(informationName);
        if (sqlTableColumn == null) {
            throw new IllegalArgumentException("Invalid sqlTableColumn for the informationName: " + informationName);
        }
        return sqlTableColumn;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.groupon.novie.SchemaConfig#isValidDimension(java.lang.String)
     */
    @Override
    public boolean isValidDimension(String dimensionName) {
        return isValidDimension(dimensionName, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.groupon.novie.SchemaConfig#isValidDimension(java.lang.String,
     * java.lang.String)
     */
    @Override
    public boolean isValidDimension(String dimensionName, String informationName) {
        if (StringUtils.isNotBlank(dimensionName)) {
            DimensionTable dim = dimensions.get(dimensionName.toUpperCase());
            if (dim == null) {
                return false;
            }
            if (StringUtils.isBlank(informationName)) {
                return true;
            }
            if (dim.getSqlTableColumnByInformationName(informationName.toUpperCase()) != null) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.groupon.novie.SchemaDefinition#isMandatoryDimension(java.lang.String)
     */
    @Override
    public boolean isMandatoryDimension(String dimensionName) {
        if (isValidDimension(dimensionName, null)) {
            return mandatoryDimensionNames.contains(dimensionName.toUpperCase(Locale.ENGLISH));
        }
        return false;
    }


    /*
     * (non-Javadoc)
     *
     * @see com.groupon.novie.SchemaConfig#getInformationType(java.lang.String,
     * java.lang.String)
     */
    @Override
    public ColumnDataType getInformationType(String dimensionName, String informationName) {
        if (StringUtils.isNotBlank(dimensionName)) {
            DimensionTable dim = dimensions.get(dimensionName.toUpperCase());
            if (dim == null) {
                throw new IllegalArgumentException("Unknown dimension named " + dimensionName + ".");
            }
            if (StringUtils.isBlank(informationName)) {
                return dim.getDefaultSearchColumn().getColumnType();
            }
            AbstractSqlColumn information = dim.getSqlTableColumnByInformationName(informationName);
            if (information == null) {
                throw new IllegalArgumentException("Unknown information named " + informationName + " for dimension " + dimensionName + ".");
            }
            return information.getColumnType();
        }
        throw new IllegalArgumentException("Dimension name can't be null or blank");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.groupon.novie.SchemaConfig#isTimeZoneSupported(java.lang.String)
     */
    @Override
    public boolean isTimeZoneSupported(String tzName) {
        return tzNamesAliases.containsKey(tzName);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.groupon.novie.SchemaConfig#getDefaultTimezone()
     */
    @Override
    public String getDefaultTimezone() {
        return defaultTimezone;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.groupon.novie.SchemaConfig#getMandatoryDimension()
     */
    @Override
    public Collection<String> getMandatoryDimension() {
        return Collections.unmodifiableCollection(mandatoryDimensionNames);
    }

    // #######

    public void setDatabaseName(String dbName) {
        this.databaseName = dbName;
    }

    public void addSqlForeignKey(Pair<String, String> column1, Pair<String, String> column2) {
        fkRelationshipSet.add(ImmutablePair.of(column1, column2));
    }

    private void buildSqlForeignKey(PhysicalSqlColumn column1, PhysicalSqlColumn column2) {
        if (column1 == null || column2 == null) {
            throw new IllegalArgumentException("Can't link null columns.");
        }
        column1.addForeignKey(column2);
        column2.addForeignKey(column1);
        Map<String, SqlTable> stringSqlTableMap = predecessorRelation.get(column1.getSqlTable().getSqlString());
        if (stringSqlTableMap == null) {
            stringSqlTableMap = Maps.newHashMap();
            predecessorRelation.put(column1.getSqlTable().getSqlString(), stringSqlTableMap);
        }

        stringSqlTableMap.put(column2.getSqlTable().getTableName(), column2.getSqlTable());

        stringSqlTableMap = predecessorRelation.get(column2.getSqlTable().getSqlString());
        if (stringSqlTableMap == null) {
            stringSqlTableMap = Maps.newHashMap();
            predecessorRelation.put(column2.getSqlTable().getSqlString(), stringSqlTableMap);
        }

        stringSqlTableMap.put(column1.getSqlTable().getTableName(), column1.getSqlTable());
    }

    /**
     * Add a new supported tz. Add one aliase with the same name
     *
     * @param tzName
     */
    public void addSupportedTZ(String tzName) {
        if (!StringUtils.isBlank(tzName) && !tzNamesAliases.containsKey(tzName.trim())) {
            tzNamesAliases.put(tzName.trim(), tzName.trim());
            if (LOG.isInfoEnabled()) {
                LOG.info("Endpoint " + this.getEndPointName() + " - add support of TZ: " + tzName);
            }
        }
    }

    public void addTZAliases(String tzName, String... tzAliases) {
        addSupportedTZ(tzName);
        if (tzAliases != null && !StringUtils.isBlank(tzName)) {
            for (String tzAlias : tzAliases) {
                if (StringUtils.isNotBlank(tzAlias)) {
                    String timedTzAlias = tzAlias.trim();
                    if (tzNamesAliases.containsKey(timedTzAlias)) {
                        LOG.warn("Timezone alias \"" + timedTzAlias + "\" already assigned to \"" + tzNamesAliases.get(timedTzAlias)
                                + "\". Replaced by " + tzName);
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Endpoint " + this.getEndPointName() + " - for TZ " + tzName + ", add support of Alias: " + timedTzAlias);
                    }
                    tzNamesAliases.put(timedTzAlias, tzName);
                }
            }
        }
    }

    // Timezone is case sensitive
    public void addTZAlternateDimension(String orignalDimensionName, DimensionTable alternateDimension, String tzName) {
        addSupportedTZ(tzName);
        if (tzNamesAliases.containsValue(tzName)) {
            sqlTables.put(alternateDimension.getTableName(), alternateDimension);
            alternateDimensions.put(Pair.of(orignalDimensionName.toUpperCase(), tzName), alternateDimension);
        } else {
            LOG.error("Unsuported timezone: " + tzName);
        }
    }

    /**
     * Add a dimension as mandatory.
     * Mandatory dimension names are stored in upper case.
     *
     * @param table     The dimension table
     * @param mandatory Add if true, remove otherwise
     */
    public void addDimension(DimensionTable table, boolean mandatory) {
        sqlTables.put(table.getTableName(), table);
        dimensions.put(table.getDimensionName().toUpperCase(), table);
        if (mandatory) {
            mandatoryDimensionNames.add(table.getDimensionName().toUpperCase(Locale.ENGLISH));
        }
    }

    public void setFactTable(FactTable factTable) throws IllegalArgumentException {
        if (this.factTable != null) {
            throw new IllegalArgumentException("Fact table already setted");
        }
        this.factTable = factTable;
        sqlTables.put(factTable.getTableName(), factTable);
    }

    public void setDefaultTimezone(String defaultTimezone) {
        if (StringUtils.isBlank(defaultTimezone)) {
            throw new IllegalArgumentException("Default timezone can't be null.");
        }
        this.defaultTimezone = defaultTimezone;
        addSupportedTZ(defaultTimezone);
    }

    /**
     * Build the configuration links (ie Foreigns key). Checks the validity of
     * the configuration.
     *
     * @throws Exception
     */
    public Collection<Error> buildAndValidateConfig() {
        Collection<Error> returnValue = new ArrayList<Error>();

        // Check default TZ
        if (defaultTimezone == null) {
            returnValue.add(new Error("No default Timezone configured"));
        }

        for (Pair<Pair<String, String>, Pair<String, String>> fkRelationship : fkRelationshipSet) {
            Optional<PhysicalSqlColumn> left = getColumnByPair(fkRelationship.getLeft());
            Optional<PhysicalSqlColumn> right = getColumnByPair(fkRelationship.getRight());
            if (!left.isPresent()) {
                returnValue.add(new Error("Column " + fkRelationship.getLeft().getRight() + " in table " + fkRelationship.getLeft().getLeft()
                        + " doesn't exist."));
            } else if (!right.isPresent()) {
                returnValue.add(new Error("Column " + fkRelationship.getRight().getRight() + " in table " + fkRelationship.getRight().getLeft()
                        + " doesn't exist."));
            } else {
                buildSqlForeignKey(left.get(), right.get());
            }
        }
        // Check Alternate Tables
        for (Entry<Pair<String, String>, DimensionTable> entry : alternateDimensions.entrySet()) {
            DimensionTable dim = dimensions.get(entry.getKey().getLeft());
            if (dim == null) {
                returnValue.add(new Error("Original dimension " + entry.getKey().getLeft() + " does not exist."));
            } else {
                if (!dim.isEquivalent(entry.getValue(), returnValue)) {
                    returnValue.add(new Error("Dimension " + entry.getValue().getDimensionName() + " is not equivalent to " + dim.getDimensionName()
                            + "."));
                }
            }

        }

        return returnValue;
    }

    private Optional<PhysicalSqlColumn> getColumnByPair(Pair<String, String> columnName) {
        SqlTable sqlTable = sqlTables.get(columnName.getLeft());
        if (sqlTable == null) {
            LOG.error("Table with name " + columnName.getLeft() + " not presents in the schema.");
            return Optional.absent();
        }
        Optional<AbstractSqlColumn> rValue = sqlTable.getColumnByName(columnName.getRight());
        if (rValue.isPresent() && PhysicalSqlColumn.class.isAssignableFrom(rValue.get().getClass())) {
            return Optional.of((PhysicalSqlColumn) rValue.get());
        }
        return Optional.absent();
    }

    /**
     * {@inheritDoc SchemaDefinition}
     * {@link SchemaDefinition#isValidMeasure(String)}
     */
    @Override
    public boolean isValidMeasure(String name) {
        for (AbstractSqlColumn column : factTable.getMeasuresColumn().values()) {
            if (column.getBusinessName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        Set<String> unicTZ = new HashSet<String>(tzNamesAliases.values());
        StringBuilder returnValue = new StringBuilder();
        returnValue.append(this.getEndPointName());
        returnValue.append(" [fact: ");
        returnValue.append(this.factTable.getTableName());
        returnValue.append(" ,dim (");
        returnValue.append(this.dimensions.size());
        returnValue.append("): ");
        for (DimensionTable dim : dimensions.values()) {
            returnValue.append(dim.getTableName());
            returnValue.append("(");
            returnValue.append(dim.getDimensionName());
            returnValue.append(") ");
        }
        returnValue.append(",TZ (");
        returnValue.append(this.getDefaultTimezone());
        returnValue.append("): ");
        returnValue.append(unicTZ.size());
        returnValue.append("-");
        returnValue.append(tzNamesAliases.size());
        returnValue.append("]");
        return returnValue.toString();
    }
}
