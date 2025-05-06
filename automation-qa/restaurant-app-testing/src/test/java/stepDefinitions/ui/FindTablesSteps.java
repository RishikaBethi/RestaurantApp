package stepDefinitions.ui;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import pages.FIndTablesPage;

public class FindTablesSteps {

    private FIndTablesPage FIndTablesPage;

    public FindTablesSteps(){
        FIndTablesPage = new FIndTablesPage();
    }

    @When("the user selects the {string} timeslot")
    public void selectTheTimeSlot(String time){
        FIndTablesPage.selectTimeFromDropDown(time);
    }

    @Then("the user should be able to see the available tables")
    public void verifyThePresenceOfAvailableTables(){
        Assert.assertTrue(FIndTablesPage.getNumberOfAvailableTables()>0);
    }

    @Then("the user should be able to see the available timeslots")
    public void verifyPresenceOfTimeSlots() {
        Assert.assertTrue(FIndTablesPage.visibilityOfTimeSlots());
    }

    @When("the user selects the {string} {string} {string} {string} details")
    public void enterTheDetailsToFindTable(String location,String date,String time,String guests){
        FIndTablesPage.sendDetailsToBookATable(location,date,time,guests);
    }

    @And("the user should be not see any available tables")
    public void verifyTheAbsenceOfAvailableTables(){
        Assert.assertTrue(FIndTablesPage.getNumberOfAvailableTables()==0);
    }

    @Then("the user should be able to increment guests count")
    public void incrementCount(){
        FIndTablesPage.increment();
        Assert.assertEquals("3", FIndTablesPage.getGuestsCount());
    }

    @Then("the {string} message is shown")
    public void successfulRegistrationMessage(String message){
        Assert.assertEquals(FIndTablesPage.getReservationConfirmationMessage(), message);
    }

    @And("the user clicks on a timeslot")
    public void clickOnTimeSlot(){
        FIndTablesPage.clickOnTimeSlot();
    }

    @And("the user increments the guests")
    public void incrementGuests(){
        FIndTablesPage.increment();
    }
}