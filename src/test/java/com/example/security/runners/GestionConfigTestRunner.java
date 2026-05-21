package com.example.security.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/gestion-config.feature",
        glue = "com.example.security.steps.gestionconfig",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/gestion-config/cucumber.html",
                "json:target/cucumber-reports/gestion-config/cucumber.json"
        },
        monochrome = true
)
public class GestionConfigTestRunner {
}