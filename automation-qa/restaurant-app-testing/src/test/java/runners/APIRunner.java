package runners;


import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/test/resources/features/api",
        glue = "stepDefinitions.api"
)
public class APIRunner extends AbstractTestNGCucumberTests {
}
