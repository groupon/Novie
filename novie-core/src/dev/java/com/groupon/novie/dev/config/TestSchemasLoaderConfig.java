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
package com.groupon.novie.dev.config;

import java.util.ArrayList;
import java.util.Collection;

import com.groupon.novie.internal.exception.NovieException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.groupon.novie.SchemasLoader;
import com.groupon.novie.SchemaDefinition;
import com.groupon.novie.engine.ColumnDataType;
import com.groupon.novie.internal.engine.schema.DimensionTable;
import com.groupon.novie.internal.engine.schema.FactTable;
import com.groupon.novie.utils.SchemaDefinitionImpl;

@Configuration
public class TestSchemasLoaderConfig implements SchemasLoader {

    private static Logger _log = LoggerFactory.getLogger(TestSchemasLoaderConfig.class);

    private Collection<SchemaDefinition> configs = new ArrayList<SchemaDefinition>(2);

    private String CSTAliases = "CST;America/Chicago; America/Indiana/Knox; America/Indiana/Tell_City; America/Knox_IN; America/Matamoros; America/Menominee; America/North_Dakota/Beulah; America/North_Dakota/Center; America/North_Dakota/New_Salem; America/Rainy_River; America/Rankin_Inlet; America/Resolute; America/Winnipeg; CST6CDT; Canada/Central; US/Indiana-Starke;";

    public TestSchemasLoaderConfig() {
        configs.add(getSchemaConfig());
        for (SchemaDefinition sc : configs) {
            Collection<Error> errors = ((SchemaDefinitionImpl) sc).buildAndValidateConfig();
            if (errors.size() > 0) {
                for (Error e : errors) {
                    _log.error(e.getMessage());
                }
                throw new IllegalArgumentException("There is error in the configuration.");
            }
        }
    }

    @Bean(name = "novieSchemasLoader")
    public SchemasLoader getSchemasLoader() {
        return this;
    }

    @Override
    public Collection<SchemaDefinition> loadSchemasConfig() throws NovieException {
        return configs;
    }

    public SchemaDefinition getSchemaConfig() {
        SchemaDefinitionImpl returnValue = SchemaDefinitionImpl.createInstance("app");
        returnValue.setDatabaseName("test");
        returnValue.setDefaultTimezone("UTC");

        // Fact table
        FactTable factTable = new FactTable("fact");
        factTable.addTableColumn("u_id", ColumnDataType.INTEGER);
        factTable.addTableColumn("a_id", ColumnDataType.INTEGER);
        factTable.addTableColumn("dt_id", ColumnDataType.INTEGER);
        factTable.addMeasure("login_succeed", ColumnDataType.INTEGER, "loginSucceed");
        factTable.addMeasure("login_failed", ColumnDataType.INTEGER, "loginFailed");
        factTable.addMeasure("logon_duration", ColumnDataType.INTEGER, "logonDuration");
        factTable.addVirtualMeasure("1.0*sum(login_failed)/(sum(login_succeed)+sum(login_failed))", ColumnDataType.DECIMAL, "failedRatio");

        returnValue.setFactTable(factTable);

        // Dim User
        final DimensionTable user = new DimensionTable("dim_user", "user");
        user.addTableColumn("u_id", ColumnDataType.INTEGER);
        user.addTableColumn("u_firstname", ColumnDataType.STRING, "Firstname");
        user.addTableColumn("u_surname", ColumnDataType.STRING, "Surname");
        user.addTableColumn("u_country", ColumnDataType.STRING, "Country");
        user.addTableColumn("u_birth_date", ColumnDataType.DATE, "BirthDate");
        user.setDefaultGroupByKey("u_id");
        user.setDefaultSearchColumn("u_surname");
        user.setDisplayTemplate("%Firstname% %Surname%");
        returnValue.addDimension(user, false);
        returnValue.addSqlForeignKey(ImmutablePair.of("fact", "u_id"), ImmutablePair.of("dim_user", "u_id"));

        // Dim App
        final DimensionTable app = new DimensionTable("dim_app", "application");
        app.addTableColumn("a_id", ColumnDataType.INTEGER);
        app.addTableColumn("a_name", ColumnDataType.STRING, "Name");
        app.addTableColumn("a_url", ColumnDataType.STRING, "Url");
        app.setDefaultGroupByKey("a_id");
        app.setDefaultSearchColumn("a_name");
        returnValue.addDimension(app, true);
        returnValue.addSqlForeignKey(ImmutablePair.of("fact", "a_id"), ImmutablePair.of("dim_app", "a_id"));

        // Dim DT
        final DimensionTable dt = new DimensionTable("dim_dt", "datetime");
        dt.addTableColumn("dt_id", ColumnDataType.INTEGER);
        dt.addTableColumn("dt_name", ColumnDataType.STRING, "Name");
        dt.setDefaultGroupByKey("dt_id");
        dt.setDefaultSearchColumn("dt_name");
        returnValue.addDimension(dt, false);
        returnValue.addSqlForeignKey(ImmutablePair.of("fact", "dt_id"), ImmutablePair.of("dim_dt", "dt_id"));

        final DimensionTable dt_cst = new DimensionTable("dim_dt_CST", "DT_CST");
        dt_cst.addTableColumn("dt_id", ColumnDataType.INTEGER);
        dt_cst.addTableColumn("dt_name", ColumnDataType.STRING, "Name");
        dt_cst.setDefaultGroupByKey("dt_id");
        dt_cst.setDefaultSearchColumn("dt_name");
        returnValue.addTZAlternateDimension("datetime", dt_cst, "US/Central");
        returnValue.addSqlForeignKey(ImmutablePair.of("fact", "dt_id"), ImmutablePair.of("dim_dt_CST", "dt_id"));

        returnValue.addTZAliases("US/Central", CSTAliases.split(";"));

        return returnValue;
    }
}
