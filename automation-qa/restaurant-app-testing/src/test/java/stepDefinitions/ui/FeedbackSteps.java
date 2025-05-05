package stepDefinitions.ui;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import pages.FeedbacksPage;
import pages.ViewReservationsPage;

public class FeedbackSteps {

    private FeedbacksPage feedbacksPage;
    private ViewReservationsPage viewReservationsPage;

    public FeedbackSteps(FeedbacksPage feedbacksPage, ViewReservationsPage viewReservationsPage){
        this.feedbacksPage = feedbacksPage;
        this.viewReservationsPage = viewReservationsPage;
    }

    @When("the user gives {int} stars")
    public void giveFourStars(int stars){
        if(stars == 5)
            feedbacksPage.giveFiveStars();
        else
            feedbacksPage.giveFourStars();
    }

    @When("the user clicks on Update Feedback button for giving feedback")
    public void clickUpdateFeedbackNewReservation(){
            viewReservationsPage.clickUpdateFeedbackOfNewReservation();
    }

    @And("adds a comment")
    public void addComment(){
        feedbacksPage.addComment();
    }

    @And("the user clicks the Update Feedback button")
    public void clickUpdateFeedback(){
        feedbacksPage.clickFeedbackButton();
    }

    @And("gives no comment")
    public void clearComment(){
        feedbacksPage.clearComment();
    }

    @Then("the page will display the feedback created message")
    public void testFeedbackCreatedMessage(){
        Assert.assertEquals(feedbacksPage.getFeedbackSuccessMessage(), "Feedback has been created");
    }
}
