package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocationPages extends BasePage {

    private JavascriptExecutor js;

    @FindBy(xpath = "//h3[.='Locations']")
    private WebElement locationsView;

    @FindBy(xpath="(//img[contains(@class,'h-40')]//following-sibling::div/div)[1]")
    private WebElement locations;

    @FindBy(xpath = "(//h1[.='Green & Tasty']//following-sibling::div//following-sibling::p)[1]")
    private WebElement locationsNameAfterClick;

    @FindBy(xpath = "(//h1[.='Green & Tasty']//following-sibling::div//following-sibling::p)[2]")
    private WebElement ratings;

    @FindBy(xpath = "//h2[.='Customer Reviews']")
    private WebElement customerReviews;

    @FindBy(xpath = "//h2[.='Customer Reviews']//following-sibling::div[contains(@class,'md:grid-cols-4')]/div")
    private List<WebElement> serviceOrCuisineRatings;

    @FindBy(xpath = "//button[.='Cuisine Experience']")
    private WebElement cuisineRatings;

    public LocationPages() {
        super();
        js = (JavascriptExecutor) driver;
    }

    public void scrollIntoLocationsView() {
        js.executeScript("arguments[0].scrollIntoView()",locationsView);
    }

    public String getLocationsNameBeforeClick(){
        waitForElementToBeVisible(locations);
        return getTextOfElement(locations);
    }

    public void clickOnALocation(){
        click(locations);
    }

    public String getLocationsNameAfterClick(){
        waitForElementToBeVisible(locationsNameAfterClick);
        return getTextOfElement(locationsNameAfterClick);
    }

    public void scrollIntoCustomerReviewsView(){
        waitForElementToBeVisible(customerReviews);
        js.executeScript("arguments[0].scrollIntoView()",customerReviews);
    }

    public String getRatings(){
        return getTextOfElement(ratings);
    }

    public int getNumberOfRatings(){
        return serviceOrCuisineRatings.size();
    }

    public void clickOnCuisineRatings(){
        click(cuisineRatings);
    }

    public void filterTheRatingByGivenRequirement(String requirement) {
        WebElement filterDropDown = driver.findElement(By.xpath("//select[contains(@class,'border-green-600')]"));
        Select filterDropDown_dd = new Select(filterDropDown);
        filterDropDown_dd.selectByContainsVisibleText(requirement);
    }

    public List<Integer> sortTheRatingsInSpecifiedOrder(String order){
        List<Integer> sortedServiceRatings = new ArrayList<>();
        for(WebElement element:serviceOrCuisineRatings) {
            List<WebElement> elements = element.findElements(By.xpath("//*[local-name()='svg']"));
            sortedServiceRatings.add(elements.size());
        }
        switch (order) {
            case "descending" ->
                Collections.sort(sortedServiceRatings,Collections.reverseOrder());
            case "ascending" ->
                Collections.sort(sortedServiceRatings);
            default ->
                System.out.println("No order provided");
        }
        return sortedServiceRatings;
    }

    public List<Integer> getTheOrderOfRatingsSelectedFromDropDown(){
        List<Integer> actualServiceRatings = new ArrayList<>();
        for(WebElement element:serviceOrCuisineRatings) {
            List<WebElement> elements = element.findElements(By.xpath("//*[local-name()='svg']"));
            actualServiceRatings.add(elements.size());
        }
        return actualServiceRatings;
    }
}

