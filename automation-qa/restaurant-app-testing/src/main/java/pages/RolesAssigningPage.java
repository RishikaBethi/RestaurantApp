package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.DriverManager;

import java.time.Duration;

public class RolesAssigningPage {

    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(xpath = "//span[@data-slot='avatar-fallback']")
    private WebElement userLogo;

    @FindBy(xpath = "//div[contains(@class,'p-1')]/p[1]")
    private WebElement role;

    public RolesAssigningPage(){
        driver = DriverManager.getDriver();
        PageFactory.initElements(driver,this);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void clickOnUserLogo(){
        wait.until(ExpectedConditions.visibilityOf(userLogo));
        userLogo.click();
    }

    public String getRole(){
        wait.until(ExpectedConditions.visibilityOf(role));
        return role.getText();
    }
}
