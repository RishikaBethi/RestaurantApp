package stepDefinitions.api;

import context.ShareContext;
import helpers.specBuilders.RequestBuilder;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class LocationsSteps {

    private final ShareContext shareContext;

    public LocationsSteps(ShareContext shareContext)
    {
        this.shareContext = shareContext;
    }

}
