package com.example.security.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/register.feature",
        glue = "com.example.security.steps.register",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/register/cucumber.html",
                "json:target/cucumber-reports/register/cucumber.json"
        },
        monochrome = true
)
public class RegisterTestRunner {
}