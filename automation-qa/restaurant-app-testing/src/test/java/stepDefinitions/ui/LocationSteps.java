package stepDefinitions.ui;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import pages.LocationPages;

public class LocationSteps {

    private LocationPages locationPages;
    static String locationsNameBeforeClick;

    public LocationSteps(){
        locationPages = new LocationPages();
    }

    @When("the user scrolls to the {string} view")
    public void userScrollsIntoViews(String view) {
        switch (view) {
            case "locations" ->
                locationPages.scrollIntoLocationsView();
            case "Customer Reviews" ->
                locationPages.scrollIntoCustomerReviewsView();
            default ->
                throw new customExceptions.NoViewFoundException("No view found");
        }
    }

    @Then("the user should find the locations")
    public void userFindsAndClicksOnALocation() throws InterruptedException {
        locationsNameBeforeClick = locationPages.getLocationsNameBeforeClick();
        locationPages.clickOnALocation();
    }

    @And("the users should be able to see the locations description")
    public void verifyTheDescription(){
        String locationsNameAfterClick = locationPages.getLocationsNameAfterClick();
        Assert.assertEquals(locationsNameAfterClick,locationsNameBeforeClick);
    }

    @And("the users should be able to see the {string} ratings")
    public void verifyTheRating(String ratings){
        Assert.assertEquals(locationPages.getRatings(),ratings);
    }

    @And("verify the presence of ratings")
    public void verifyPresenceOfRatings(){
        Assert.assertTrue(locationPages.getNumberOfRatings()>0);
    }

    @And("the user clicks on {string} in dropDown")
    public void selectTheRequirementFromDropDown(String requirement) {
        locationPages.filterTheRatingByGivenRequirement(requirement);
    }

    @And("the ratings should be displayed in the {string} order")
    public void verifyTheSelectedOrderOfRatings(String order){
        switch (order){
            case "Top rated first" ->
                Assert.assertEquals(locationPages.getTheOrderOfRatingsSelectedFromDropDown(),locationPages.sortTheRatingsInSpecifiedOrder("descending"));
            case "Low rated first" ->
                Assert.assertEquals(locationPages.getTheOrderOfRatingsSelectedFromDropDown(),locationPages.sortTheRatingsInSpecifiedOrder("ascending"));
            default ->
                System.out.println("No order specified");
        }
    }

}
