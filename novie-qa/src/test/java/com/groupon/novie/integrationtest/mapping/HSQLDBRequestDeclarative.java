package com.groupon.novie.integrationtest.mapping;

import com.groupon.novie.integrationtest.utils.AutomationProperties;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.hsqldb.cmdline.SqlFile;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;


/**
 * Created by thomas on 09/01/2014.
 */
public class HSQLDBRequestDeclarative {

    private static Logger _log = LoggerFactory.getLogger(HSQLDBRequestDeclarative.class);


    private Connection getConnectionOnIntegration() throws Exception {

        Class.forName("org.hsqldb.jdbcDriver");
        return DriverManager.getConnection(AutomationProperties.getProperty("db.connection.url"), AutomationProperties.getProperty("db.connection.user"), AutomationProperties.getProperty("db.connection.pwd"));

    }


    @Given("^Dataset (.*) loaded.$")
    public void runSQLScript(String fileName) {
        try {
            SqlFile sqlFile = new SqlFile(new File(fileName));
            Connection c = getConnectionOnIntegration();
            c.setAutoCommit(false);
            sqlFile.setConnection(c);
            sqlFile.execute();
            c.commit();
            c.close();
        } catch (Exception e) {
            _log.error("Error when loading the Dataset: " + fileName, e);
            Assert.fail();
        }
    }

    @Given("^A clean database.$")
    @Then("^clean the database.$")
    public void selectFromApp() {
        runSQLScript(this.getClass().getResource("/qa_schema_clean.sql").getFile());

    }

}
