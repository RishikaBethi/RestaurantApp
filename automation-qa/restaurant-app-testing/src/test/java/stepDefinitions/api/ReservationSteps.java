package stepDefinitions.api;

import context.ShareContext;
import helpers.specBuilders.RequestBuilder;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ReservationSteps {
    private final ShareContext shareContext;

    public ReservationSteps(ShareContext shareContext){
        this.shareContext = shareContext;
    }

    @When("the user sends authorized get request to {string} request payload")
    public void getReq(String endpoint){
        Response response = given()
                .spec(RequestBuilder.sendAuthorizedCustomerGetRequest(shareContext))
                .when()
                .get(endpoint)
                .then()
                .extract().response();
        shareContext.setResponse(response);
    }
}
