package stepDefinitions.api;

import context.ShareContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import models.SignIn;
import java.util.Map;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;

public class SignInSteps {

    private final ShareContext shareContext;

    public SignInSteps(ShareContext shareContext)
    {
        this.shareContext = shareContext;
    }


    @Given("user sends a sign in request with the following data")
    public void sendSignInData(DataTable dataTable)
    {
        List<Map<String,String>> data= dataTable.asMaps();
       // System.out.println(data);
        SignIn user = new SignIn.SignInBuilder()
                .setEmail(data.get(0).get("email"))
                .setPassword(data.get(0).get("password"))
                .build();
        System.out.println(data);
        shareContext.setUser(user);
    }

    @And("the response should contain the {string} assigned")
    public void validateTheRole(String role)
    {
        role = role.trim();
        shareContext.getResponse().then().body("role",equalTo(role));
    }

    @And("the response should contain failed error or message for {int} based on {string} message")
    public void validateResponseBasedOnAttempts(int status,String message)
    {
        if(status==401)
        {
            shareContext.getResponse().then().body("error",equalTo(message));
        }
        else if(status == 403)
        {
            shareContext.getResponse().then().body("message",equalTo(message));
        }

    }
}
