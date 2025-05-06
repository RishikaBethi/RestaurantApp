package stepDefinitions.ui;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import pages.WaiterReservationsPage;
import utils.DriverManager;

import java.sql.Driver;

public class WaiterReservationsSteps {

    private WebDriver driver;
    private WaiterReservationsPage waiterReservationsPage;

    public WaiterReservationsSteps(){
        driver = DriverManager.getDriver();
        waiterReservationsPage = new WaiterReservationsPage();
    }



    @And("the waiter selects the date {string}")
    public void waiterSendsTheDate(String date)
    {
        waiterReservationsPage.sendDate(date);
    }

    @And("the waiter sends the {string} customer email")
    public void sendCustomerEmail(String email)
    {
        waiterReservationsPage.sendCustomerEmail(email);
    }


    @Then("the waiter should able to see reservations")
    public void verifyVisibilityOfReservations(){
        Assert.assertTrue(waiterReservationsPage.getFilteredReservations()>0);
    }

    @And("the waiter selects the guests")
    public void enterTheGuests(){
        waiterReservationsPage.enterTheGuests();
    }

    @And("the waiter selects the {string} for {string}")
    public void sendInputsToFields(String input, String field) throws InterruptedException {
        switch (field)
        {
            case "time" ->{
                waiterReservationsPage.enterTheTimeFrom(input);
                Thread.sleep(4000);
            }


            case "table" ->{
                waiterReservationsPage.enterTheTableNumber(input);
                Thread.sleep(4000);
            }

            case "date" ->
                waiterReservationsPage.sendDateInsideCreateReservation(input);
            default ->
                throw new customExceptions.NoButtonFoundException("No such button found");

        }
    }

    @Then("the page will display the confirmation {string} message")
    public void verifyTheFinalMessageAfterReservation(String message) throws InterruptedException {
        Assert.assertEquals(waiterReservationsPage.getConfirmationMessage(),message);
    }
}
