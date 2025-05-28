package stepDefinitions.ui;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.testng.Assert;
import pages.RolesAssigningPage;

public class RoleAssigningSteps {


    private RolesAssigningPage rolesAssigningPage;

    public RoleAssigningSteps(){
        rolesAssigningPage = new RolesAssigningPage();
    }

    @And("the user clicks on user logo button")
    public void clickOnUserLogo(){
        rolesAssigningPage.clickOnUserLogo();
    }

    @Then("the user will be assigned the {string} role")
    public void verifyTheRoleAssignment(String role){
        Assert.assertEquals(rolesAssigningPage.getRole(),role);
    }
}
