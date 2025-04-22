package stepDefinitions.api;

import context.ShareContext;
import io.cucumber.java.en.Given;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import models.ClientBookings;

import java.util.List;
import java.util.Map;

public class ClientBookingsSteps {

    private final ShareContext shareContext;
    public ClientBookingsSteps(ShareContext shareContext){
        this.shareContext = shareContext;
    }

    @Given("a user sends booking request with following data")
    public void sendBookingData(DataTable dataTable){
        List<Map<String, String>> data = dataTable.asMaps();
        ClientBookings bookings = new ClientBookings.ClientBookingsBuilder()
                .setLocationId(data.get(0).get("locationId"))
                .setTableNumber(data.get(0).get("tableNumber"))
                .setDate(data.get(0).get("date"))
                .setGuestsNumber(data.get(0).get("guestsNumber"))
                .setTimeFrom(data.get(0).get("timeFrom"))
                .setTimeTo(data.get(0).get("timeTo"))
                .build();
        shareContext.setUser(bookings);
    }
    @Then("the status code should be the {int}")
    public void verifyStatus(int code)
    {
        shareContext.getResponse().then().statusCode(code);
    }
}
