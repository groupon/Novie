package com.groupon.novie.integrationtest;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * Created by thomas on 09/01/2014.
 */
//to run a specific feature file, specify the directory structure under src/main/features
//i.e. "Regression/CallCredit/Consumer.credentials.feature"
//to run all feature files, use .
//to run specific tag(s), use features = { "." },tags = { "@Common" } where @Common is the tag name
// glue, package which contains annotation @Given, @answer etc

@RunWith(Cucumber.class)
@cucumber.api.CucumberOptions(strict = true, glue = {"com.groupon.novie.integrationtest.mapping"},
        features = {"src/test/features"},
        format = {"pretty", "html:target/cucumber/", "json:target/cucumber.json"}
)
public class NovieIntegrationTestIT {
}
