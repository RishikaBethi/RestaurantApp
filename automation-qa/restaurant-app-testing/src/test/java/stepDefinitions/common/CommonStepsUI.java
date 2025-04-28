package stepDefinitions.common;

import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import pages.LocationPages;
import pages.LoginPage;
import pages.SignUpPage;
import stepDefinitions.ui.LocationSteps;
import utils.ConfigReader;
import utils.DriverManager;

public class CommonStepsUI {

    private static final Logger log = LoggerFactory.getLogger(CommonStepsUI.class);
    private WebDriver driver;
    private LoginPage loginPage;
    private SignUpPage signUpPage;
    private LocationPages locationPages;

    @Given("the user enters into the application")
    public void enterTheBaseUrl() {
        driver = DriverManager.getDriver();
        driver.get(ConfigReader.getProperty("base_url"));
        loginPage = new LoginPage();
        signUpPage = new SignUpPage();
        locationPages = new LocationPages();
    }

    @And("the user enters into the sign in page")
    public void enterIntoTheSignInPage() {
        loginPage.enterTheSignInPage();
    }

    @When("the user enters the {string} and {string}")
    public void enterTheCredentials(String email, String password) {
        loginPage.enterValidCredentials(email, password);
    }

    @And("the user clicks on {string} button")
    public void clicksOnSignInPage(String button) {
        switch (button)
        {
            case "sign in":
                loginPage.clickOnSignInButton();
                break;
            case "create an account link":
                signUpPage.clickCreateAccountLink();
                break;
            case "create an account":
                signUpPage.clickCreateAccountButton();
                break;
            case "Cuisine Ratings":
                locationPages.clickOnCuisineRatings();
                break;
            default:
                throw new customExceptions.NoButtonFoundException("No such button found");
        }
    }

    @Then("the user will be redirected to the {string} page")
    public void verifyRedirectionToMainPage(String page) {
        Assert.assertEquals(driver.getCurrentUrl(), page);
    }

    @Then("the page will display the error {string} message")
    public void verifyTheErrorMessage(String message) {
        Assert.assertEquals(loginPage.getErrorMessage(), message);
    }

}
