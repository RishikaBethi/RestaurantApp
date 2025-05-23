package stepDefinitions.common;

import context.ShareContext;
import helpers.specBuilders.RequestBuilder;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import models.SignIn;
import utils.ConfigReader;


import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

public class CommonStepsAPI {

    private final ShareContext shareContext;

    public CommonStepsAPI(ShareContext shareContext)
    {
        this.shareContext = shareContext;
    }

    @Given("the base_uri of the application")
    public void theBaseURIOfApplication(){
        shareContext.setBaseUri(ConfigReader.getProperty("base_uri"));
    }

    @When("the user sends the post request to {string} with the request payload")
    public void sendPOSTRequest(String endpoint){
        Response response = given()
                .spec(RequestBuilder.sendPostRequestSpec(shareContext))
                .when()
                .post(endpoint)
                .then()
                .extract().response();
        shareContext.setResponse(response);
    }

    @Given("the user is authenticated")
    public void authenticateTheUser(){
        SignIn signIn = new SignIn.SignInBuilder()
                .setEmail("sushmag@example.com")
                .setPassword("Password123!")
                .build();
        shareContext.setUser(signIn);
        Response response = given()
                .spec(RequestBuilder.sendPostRequestSpec(shareContext))
                .when()
                .post("auth/sign-in")
                .then()
                .statusCode(201)
                .extract()
                .response();
        shareContext.setCustomerToken(response.jsonPath().getString("accessToken"));
    }

    @When("the user sends the authorized post request to {string} with the request payload")
    public void sendAuthenticatedPOSTRequest(String endpoint){

        Response response = given()
                .spec(RequestBuilder.sendPostRequest(shareContext,shareContext.getCustomerToken()))
                .when()
                .post(endpoint)
                .then()
                .extract().response();
        shareContext.setResponse(response);
    }

    @When("the user sends the authorized get request to {string} request payload")
    public void sendAuthorizedGetRequest(String endpoint)
    {

        Response response = given()
                .spec(RequestBuilder.sendGetRequest(shareContext,shareContext.getCustomerToken()))
                .when()
                .get(endpoint)
                .then()
                .extract().response();
        shareContext.setResponse(response);
    }

    @When("the user sends the get request to {string} request payload")
    public void sendGetRequest(String endpoint)
    {
        Response response = given()
                .spec(RequestBuilder.sendGetRequest(shareContext))
                .when()
                .get(endpoint)
                .then()
                .extract().response();
        shareContext.setResponse(response);

    }

    @When("the user sends the authorized delete request to {string} request payload")
    public void sendAuthorizedDeleteRequest(String endpoint){
        Response response = given()
                .spec(RequestBuilder.sendDeleteRequest(shareContext,shareContext.getCustomerToken()))
                .when()
                .delete(endpoint)
                .then()
                .extract().response();
        shareContext.setResponse(response);

    }

    @When("the user sends the delete request to {string} request payload")
    public void sendDeleteRequest(String endpoint){
        Response response = given()
                .spec(RequestBuilder.sendDeleteRequest(shareContext))
                .when()
                .delete(endpoint)
                .then()
                .extract().response();
        shareContext.setResponse(response);
    }


    @When("the user sends the authorized put request to {string}")
    public void sendAuthorizedPostRequest(String endpoint)
    {
        Response response = given()
                .spec(RequestBuilder.sendAuthorizedPutRequest(shareContext))
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response();

        shareContext.setResponse(response);

    }

    @Then("the status code should be {int}")
    public void validateStatusCode(int statusCode)
    {
        shareContext.getResponse().then().statusCode(statusCode);
    }

    @And("the response should contain success {string} message")
    public void validateSuccessMessage(String message)
    {
        shareContext.getResponse().then().body("message",equalTo(message));
    }

    @And("the response should contain failed {string} message")
    public void validateFailedMessage(String message)
    {
        shareContext.getResponse().then().body("message",equalTo(message));
    }

   @And("the response should contain failed error {string} message")
   public void validateErrorMessage(String error)
   {
       shareContext.getResponse().then().body("error",equalTo(error));
   }

   @And("the response should validate the {string} schema")
    public void validateTheSchema(String schema){
        shareContext.getResponse().then().body(matchesJsonSchemaInClasspath("schemas/"+schema+".json"));
   }

   @And("the response should contain the token id of the user based on {string}")
   public void storeTheTokenOfWaiterAndCustomer(String role)
   {
       if(role.equals("Waiter"))
       {
           shareContext.setWaiterToken(shareContext.getResponse().jsonPath().getString("token"));
       }
       if(role.equals("Customer"))
       {
           shareContext.setWaiterToken(shareContext.getResponse().jsonPath().getString("token"));
       }
   }


}
