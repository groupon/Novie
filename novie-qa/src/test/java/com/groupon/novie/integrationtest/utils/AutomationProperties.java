package com.groupon.novie.integrationtest.utils;

import java.util.Properties;

/**
 * Created by thomas on 15/01/2014.
 */
public class AutomationProperties {


    private static AutomationProperties instance;

    private Properties props;

    private AutomationProperties() throws Exception {
        if (props == null) {
            props = new Properties();

            props.load(getClass().getResourceAsStream("/automation.properties"));

        }
    }

    public static String getProperty(String propertyName) throws Exception {
        if (instance == null) {
            instance = new AutomationProperties();
        }
        return instance.props.getProperty(propertyName);
    }
}
