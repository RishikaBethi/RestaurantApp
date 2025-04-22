package hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.openqa.selenium.WebDriver;
import utils.DriverManager;

public class UIHooks {

    @Before
    public void setUp() {
        DriverManager.createNewDriver("Chrome"); // Always create a new driver
    }

    @After
    public void tearDown() {
        DriverManager.quitDriver(); // Quit after every scenario
    }
}
