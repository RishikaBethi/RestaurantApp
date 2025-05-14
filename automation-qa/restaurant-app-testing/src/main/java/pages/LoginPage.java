package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utils.ConfigReader;

import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class LoginPage extends BasePage {

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
        super();
    }

    public void enterTheSignInPage(){
        click(signIn);
        waitForElementToBeVisible(email);
    }

    public void enterValidCredentials(String emailText,String passwordText)
    {
        enterTextInField(email, emailText);
        enterTextInField(password, passwordText);
    }

    public void clickOnSignInButton(){
        click(submitButton);
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
        return getTextOfElement(errorMessage);
    }

    public String getMissingEmailText(){
        return getTextOfElement(missingFields);
    }
}
