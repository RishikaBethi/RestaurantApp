package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class FeedbacksPage extends BasePage{

    @FindBy(xpath = "//*[contains(@class,'lucide-star')]")
    private List<WebElement> stars;

    @FindBy(xpath = "//textarea[contains(@class,'w-full b')]")
    private WebElement commentBox;

    @FindBy(xpath = "//button[text()='Culinary Experience']")
    private WebElement culinaryExperience;

    @FindBy(xpath = "//div[contains(text(), 'created')]")
    private WebElement feedbackSuccessfulMessage;

    @FindBy(xpath = "//button[contains(@class, 'full mt-4')]")
    private WebElement updateFeedbackButton;

    public FeedbacksPage(){
        super();
    }

    public void giveFourStars(){
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[contains(@class,'lucide-star')]")));
        click(stars.get(3));
    }

    public void addComment(){
        enterTextInField(commentBox, "Good...");
    }

    public void clickCulinaryExperience() {
        culinaryExperience.click();
    }

    public String getFeedbackSuccessMessage(){
        waitForElementToBeVisible(feedbackSuccessfulMessage);
        return getTextOfElement(feedbackSuccessfulMessage);
    }

    public void clickFeedbackButton(){
        click(updateFeedbackButton);
    }

    public void giveFiveStars(){
        click(stars.get(4));
    }

    public void clearComment(){
        commentBox.clear();
    }
}
