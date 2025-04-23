package stepDefinitions.ui;

import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import pages.LoginPage;
import utils.ConfigReader;
import utils.DriverManager;

public class CommonSteps {

    private static final Logger log = LoggerFactory.getLogger(CommonSteps.class);
    private WebDriver driver;
    private LoginPage loginPage;

    @Given("the base url of the application")
    public void enterTheBaseUrl() {
        driver = DriverManager.getDriver();
        driver.get(ConfigReader.getProperty("base_url"));
        loginPage = new LoginPage();
    }

    @Given("the user enters into the sign in page")
    public void enterIntoTheSignInPage() {
        loginPage.enterTheSignInPage();
    }

    @When("the user enters the {string} and {string}")
    public void enterTheCredentials(String email, String password) {
        loginPage.enterValidCredentials(email, password);
    }

    @And("the user clicks on sign in button")
    public void clicksOnSignInPage() {
        loginPage.clickOnSignInButton();
    }

    @Then("the user will be redirected to the main page")
    public void verifyRedirectionToMainPage() {
        Assert.assertEquals(driver.getCurrentUrl(), ConfigReader.getProperty("base_url"));
    }

    @Then("the page will display the error {string} message")
    public void verifyTheErrorMessage(String message) {
        Assert.assertEquals(loginPage.getErrorMessage(), message);
    }

    @Given("print the word {string}")
    public void printTheWord(String arg0) {
        System.out.println(arg0);
    }
}
