package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.DriverManager;

import java.time.Duration;
import java.util.List;

public class FeedbacksPage {

    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(xpath = "//*[@class='lucide lucide-star w-6 h-6 cursor-pointer transition text-yellow-500 fill-yellow-500']")
    private List<WebElement> stars;

    @FindBy(xpath = "//textarea[contains(@class,'w-full b')]")
    private WebElement commentBox;

    @FindBy(xpath = "//button[text()='Culinary Experience']")
    private WebElement culinaryExperience;

    @FindBy(xpath = "//div[contains(text(), 'created')]")
    private WebElement feedbackSuccessfulMessage;

    @FindBy(xpath = "//button[contains(@class, 'full mt-4')]")
    private WebElement updateFeedbackButton;

    @FindBy(xpath = "//*[@class='lucide lucide-star w-6 h-6 cursor-pointer transition text-gray-300']")
    private WebElement fiveStars;

    public FeedbacksPage(){
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void giveFourStars(){
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@class='lucide lucide-star w-6 h-6 cursor-pointer transition text-yellow-500 fill-yellow-500']")));
        stars.get(3).click();
    }

    public void addComment(){
        commentBox.sendKeys("Good...");
    }

    public void clickCulinaryExperience(){
        culinaryExperience.click();
    }

    public String getFeedbackSuccessMessage(){
        wait.until(ExpectedConditions.visibilityOf(feedbackSuccessfulMessage));
        return feedbackSuccessfulMessage.getText();
    }

    public void clickFeedbackButton(){
        updateFeedbackButton.click();
    }

    public void giveFiveStars(){
        fiveStars.click();
    }

    public void clearComment(){
        commentBox.clear();
    }
}
