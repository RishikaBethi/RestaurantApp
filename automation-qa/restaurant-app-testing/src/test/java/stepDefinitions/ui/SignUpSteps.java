package stepDefinitions.ui;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import pages.SignUpPage;
import utils.api.APIUtils;

import java.util.List;
import java.util.Map;

public class SignUpSteps {

    private SignUpPage signUpPage;
    private APIUtils apiUtils;

    public SignUpSteps(){
        signUpPage = new SignUpPage();
        apiUtils = new APIUtils();
    }


    @And("the user clicks on create an account link")
    public void clickOnCreateAccountLink(){
        signUpPage.clickCreateAccountLink();
    }

    @When("the user enters the following data:")
    public void enterTheCredentialsOfUser(DataTable dataTable){
        List<Map<String,String>> data = dataTable.asMaps();
        String firstName = data.get(0).get("firstName");
        String lastName = data.get(0).get("lastName");
        String email = data.get(0).get("email");
        String password = data.get(0).get("password");
        String confirmPassword = data.get(0).get("confirmPassword");
        signUpPage.enterTheDetails(apiUtils.addSpaces(firstName),apiUtils.addSpaces(lastName),email,password,confirmPassword);
    }


    @Then("the page will display the registration error {string} message")
    public void verifyErrorMessage(String error)
    {
        Assert.assertEquals(signUpPage.getErrorMessage(),error);
    }

    @Then("the page will display the password error {string} message")
    public void verifyPasswordErrorMessage(String error)
    {
        Assert.assertEquals(signUpPage.getPasswordErrorMessage(),error);
    }
}
