package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class ViewReservationsPage extends BasePage{

    @FindBy(xpath = "//button[text()='Update Feedback']")
    private List<WebElement> updateFeedbackButtons;

    public ViewReservationsPage(){
        super();
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
