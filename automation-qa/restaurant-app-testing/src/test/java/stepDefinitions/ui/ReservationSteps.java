package stepDefinitions.ui;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import pages.ReservationsPage;

public class ReservationSteps {

    private ReservationsPage reservationsPage;

    public ReservationSteps(){
        reservationsPage = new ReservationsPage();
    }

    @When("the user selects the {string} timeslot")
    public void selectTheTimeSlot(String time){
        reservationsPage.selectTimeFromDropDown(time);
    }

    @Then("the user should be able to see the available tables")
    public void verifyThePresenceOfAvailableTables(){
        Assert.assertTrue(reservationsPage.getNumberOfAvailableTables()>0);
    }

    @When("the user selects the {string} {string} {string} {string} details")
    public void enterTheDetailsToFindTable(String location,String date,String time,String guests){
        reservationsPage.sendDetailsToBookATable(location,date,time,guests);
    }

    @And("the user should be not see any available tables")
    public void verifyTheAbsenceOfAvailableTables(){
        Assert.assertTrue(reservationsPage.getNumberOfAvailableTables()==0);
    }
}