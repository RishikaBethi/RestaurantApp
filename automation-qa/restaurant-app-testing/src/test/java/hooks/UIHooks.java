package hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import utils.DriverManager;

public class UIHooks {

    @Before
    public void setUp() {
        DriverManager.createNewDriver();
    }

    @After
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
