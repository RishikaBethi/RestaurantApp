package stepDefinitions.api;

import context.ShareContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import models.ProfileUpdate;

import java.util.List;
import java.util.Map;

public class UpdateUserProfileSteps {

    private ShareContext shareContext;

    public UpdateUserProfileSteps(ShareContext shareContext)
    {
        this.shareContext =shareContext;
    }

    @And("the user sends the profile update request with following data:")
    public void sendProfileUpdateDetails(DataTable dataTable)
    {
        List<Map<String,String>> data = dataTable.asMaps();
        ProfileUpdate profileUpdate = new ProfileUpdate.ProfileUpdateBuilder()
                .setBase64encodedImage(data.get(0).get("base64encodedImage"))
                .setFirstName(data.get(0).get("firstName"))
                .setLastName(data.get(0).get("lastName"))
                .build();
        shareContext.setUser(profileUpdate);

    }
}
