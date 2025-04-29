package stepDefinitions.api;

import context.ShareContext;
import helpers.specBuilders.RequestBuilder;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import models.WaiterBookings;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class WaiterBookingsSteps {

    private final ShareContext shareContext;

    public WaiterBookingsSteps(ShareContext shareContext){
        this.shareContext = shareContext;
    }

    @Given("a waiter sends booking request with following data")
    public void sendData(DataTable dataTable){
        List<Map<String, String>> data = dataTable.asMaps();
        WaiterBookings bookings = new WaiterBookings.WaiterBookingsBuilder()
                .setClientType(data.get(0).get("clientType"))
                .setCustomerEmail(data.get(0).get("customerEmail"))
                .setDate(data.get(0).get("date"))
                .setGuestsNumber(data.get(0).get("guestsNumber"))
                .setLocationId(data.get(0).get("locationId"))
                .setTableNumber(data.get(0).get("tableNumber"))
                .setTimeFrom(data.get(0).get("timeFrom"))
                .setTimeTo(data.get(0).get("timeTo"))
                .build();
        System.out.println(bookings);
        shareContext.setUser(bookings);
    }

    @When("the waiter sends authorized post request to {string} request payload")
    public void sendWaiterPostRequest(String endpoint){
        Response response = given()
                .spec(RequestBuilder.sendAuthorizedWaiterPostRequest(shareContext))
                .when()
                .post(endpoint)
                .then()
                .extract().response();
        shareContext.setResponse(response);
    }

    @And("the waiter token is stored")
    public void storeWaiterToken(){
        shareContext.setWaiterToken(shareContext.getResponse().jsonPath().getString("accessToken"));
    }
}
