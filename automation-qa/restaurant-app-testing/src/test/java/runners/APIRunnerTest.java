package runners;


import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "src/test/resources/features/api",
        glue = {"stepDefinitions.api","stepDefinitions.common"},
        tags = "@smoke or @regression",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber-html-report.html",
                "json:target/cucumber-reports/Cucumber.json",
                "junit:target/cucumber-reports/Cucumber.xml",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true
)
public class APIRunnerTest extends AbstractTestNGCucumberTests {

        @Override
        @DataProvider(parallel = true)
        public Object[][] scenarios() {
                return super.scenarios();
        }
}
