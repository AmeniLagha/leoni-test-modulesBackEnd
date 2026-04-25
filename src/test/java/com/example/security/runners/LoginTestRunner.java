package com.example.security.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/login.feature",
        glue = "com.example.security.steps.login",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/login/cucumber.html",
                "json:target/cucumber-reports/login/cucumber.json"
        },
        monochrome = true
)
public class LoginTestRunner {
}