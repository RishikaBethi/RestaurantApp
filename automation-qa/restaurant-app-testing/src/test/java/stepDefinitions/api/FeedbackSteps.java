package stepDefinitions.api;

import context.ShareContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import models.Feedbacks;

import java.util.List;
import java.util.Map;

public class FeedbackSteps {

    private ShareContext shareContext;

    public FeedbackSteps(ShareContext shareContext){
        this.shareContext = shareContext;
    }

    @Given("the user sends a feedback request with the following data:")
    public void sendFeedbackDetails(DataTable dataTable)
    {
        List<Map<String,String>> data = dataTable.asMaps();
        Feedbacks feedbacks = new Feedbacks.FeedbacksBuilder()
                .setCuisineComment(data.get(0).get("cuisineComment"))
                .setCuisineRating(data.get(0).get("cuisineRating"))
                .setReservationId(data.get(0).get("reservationId"))
                .setServiceComment(data.get(0).get("serviceComment"))
                .setServiceRating(data.get(0).get("serviceRating"))
                .build();
        shareContext.setUser(feedbacks);
    }



}
