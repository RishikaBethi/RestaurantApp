package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;
import utils.DriverManager;

import java.time.Duration;

import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class LoginPage {

    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(xpath = "//button[.='Sign In']")
    private WebElement signIn;

    @FindBy(xpath = "//input[@type='email']")
    private WebElement email;

    @FindBy(xpath = "//input[@type='password']")
    private WebElement password;

    @FindBy(xpath = "//button[@type='submit']")
    private WebElement submitButton;

    @FindBy(xpath = "//div[@data-title]")
    private WebElement errorMessage;

    @FindBy(xpath = "//p[@class='text-red-500 text-xs mt-1']")
    private WebElement missingFields;


    public LoginPage(){
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver,this);
    }

    public void enterTheSignInPage(){
        signIn.click();
        wait.until(visibilityOfElementLocated(By.xpath("//input[@type='email']")));
    }

    public void enterValidCredentials(String emailText,String passwordText)
    {
        email.sendKeys(emailText);
        password.sendKeys(passwordText);
    }

    public void clickOnSignInButton(){
        submitButton.click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlToBe(ConfigReader.getProperty("base_url")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@data-title]")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[@class='text-red-500 text-xs mt-1']"))
        ));
    }

    public String getTheCurrentUrl(){
        return driver.getCurrentUrl();
    }

    public String getErrorMessage(){
        wait.until(visibilityOfElementLocated(By.xpath("//div[@data-title]")));
        return errorMessage.getText();
    }

    public String getMissingEmailText(){
        return missingFields.getText();
    }
}
