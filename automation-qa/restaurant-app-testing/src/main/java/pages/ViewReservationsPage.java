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

public class ViewReservationsPage {

    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(xpath = "//button[text()='Update Feedback']")
    private List<WebElement> updateFeedbackButtons;

    public ViewReservationsPage(){
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void clickUpdateFeedback(){
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//button[text()='Update Feedback']")));
        updateFeedbackButtons.get(3).click();
    }

    public void clickUpdateFeedbackOfNewReservation(){
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//button[text()='Update Feedback']")));
        updateFeedbackButtons.get(0).click();
    }
}
