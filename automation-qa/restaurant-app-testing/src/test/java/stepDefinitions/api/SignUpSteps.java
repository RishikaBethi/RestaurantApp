package stepDefinitions.api;

import context.ShareContext;
import helpers.specBuilders.RequestBuilder;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import models.SignUp;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

public class SignUpSteps {

    private final ShareContext shareContext;

    public SignUpSteps(ShareContext shareContext)
    {
        this.shareContext =shareContext;
    }

    @Given("user sends a signup request with the following data")
    public void UserSendASignUpRequest(DataTable dataTable){
        List<Map<String,String>> data = dataTable.asMaps();
        SignUp user = new SignUp.SignUpBuilder()
                .setFirstName(data.get(0).get("firstName"))
                .setLastName(data.get(0).get("lastName"))
                .setEmail(data.get(0).get("email"))
                .setPassword(data.get(0).get("password"))
                .build();
        shareContext.setUser(user);
    }




}

