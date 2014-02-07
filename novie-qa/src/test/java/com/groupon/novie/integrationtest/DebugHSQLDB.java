package com.groupon.novie.integrationtest;

import com.groupon.novie.integrationtest.mapping.HSQLDBRequestDeclarative;
import org.hsqldb.persist.HsqlProperties;

import java.net.URL;

/**
 * Created by thomas on 15/01/2014.
 */
public class DebugHSQLDB {

    public static void main(String[] args) {
        try {
            HsqlProperties props = new HsqlProperties();
            props.setProperty("server.database.0", "mem:integration");
            props.setProperty("server.dbname.0", "integration");
            props.setProperty("server.silent", "false");
            org.hsqldb.Server server = new org.hsqldb.Server();
            server.setProperties(props);
            server.start();
            URL schemaURL = DebugHSQLDB.class.getResource("/qa_schema.sql");
            System.err.println("Loading schema: " + schemaURL.getFile());
            (new HSQLDBRequestDeclarative()).runSQLScript(schemaURL.getFile());
            System.err.println("Press enter to stop the server");
            System.in.read();
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
