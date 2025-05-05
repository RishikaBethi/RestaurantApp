package stepDefinitions.ui;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import pages.HomePage;
import pages.ProfilePage;

public class ProfileUpdateSteps {

    private HomePage homePage;
    private ProfilePage profilePage;

    public ProfileUpdateSteps(HomePage homePage, ProfilePage profilePage){
        this.homePage = homePage;
        this.profilePage = profilePage;
    }

    @When("the user enter {string} and {string}")
    public void enterNames(String firstName, String lastName){
        profilePage.enterData(firstName, lastName);
    }

    @And("the user enters {string}, {string} and {string}")
    public void changePassword(String oldPassword, String newPassword, String confirmPassword){
        profilePage.enterPasswords(oldPassword, newPassword, confirmPassword);
    }

    @And("the user clicks on profile")
    public void clickProfile(){
        homePage.openMyProfile();
    }

    @Then("the page will display the successfully updated message")
    public void displaySuccessfulMessage(){
        Assert.assertTrue(profilePage.getSuccessfulMessage().contains("successfully updated"));
    }

    @Then("the user should see a message that the fields exceed maximum limit")
    public void limitExceededMessage(){
        Assert.assertTrue(profilePage.isCharactersExceededMessageVisible());
    }

    @Then("the page will display {string} message")
    public void testIncorrectOldPasswordMessage(String message){
        Assert.assertEquals(profilePage.incorrectOldPasswordMessage(), message);
    }

    @Then("the page will display error {string}")
    public void testErrorMessage(String errorMessage){
        Assert.assertEquals(profilePage.getErrorMessage(), errorMessage);
    }
}
