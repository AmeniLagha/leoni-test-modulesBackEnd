package com.example.security.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/chargesheet.feature",
        glue = "com.example.security.steps.chargesheet",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/chargesheet/cucumber.html",
                "json:target/cucumber-reports/chargesheet/cucumber.json"
        },
        monochrome = true
)
public class ChargeSheetTestRunner {
}