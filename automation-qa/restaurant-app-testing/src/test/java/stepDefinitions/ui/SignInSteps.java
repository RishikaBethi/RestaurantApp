package stepDefinitions.ui;

import io.cucumber.java.en.Then;
import models.Login;
import org.openqa.selenium.bidi.log.Log;
import org.testng.Assert;
import pages.LoginPage;

public class SignInSteps {

    private LoginPage loginPage;

    public SignInSteps(LoginPage loginPage)
    {
        this.loginPage = loginPage;
    }

    @Then("the page will display the missing fields error {string} message")
    public void verifyMissingFields(String message)
    {
        Assert.assertEquals(loginPage.getMissingEmailText(),message);
    }


}
