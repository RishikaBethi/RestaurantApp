package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.DriverManager;

import java.time.Duration;

public class ProfilePage {

    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(id = "firstName")
    private WebElement firstNameField;

    @FindBy(id = "lastName")
    private WebElement lastNameField;

    @FindBy(xpath = "//button[text()='Save Changes']")
    private WebElement saveChangesButton;

    @FindBy(xpath = "//div[text()='Profile has been successfully updated']")
    private WebElement successfulMessage;

    @FindBy(xpath = "//button[text()='Change Password']")
    private WebElement changePassword;

    @FindBy(xpath = "//p[contains(text(), 'up to 50 characters.')]")
    private WebElement charactersExceededMessage;

    @FindBy(id = "old-password")
    private WebElement oldPasswordField;

    @FindBy(id = "new-password")
    private WebElement newPasswordField;

    @FindBy(id = "confirm-new-password")
    private WebElement confirmNewPasswordField;

    @FindBy(xpath = "//div[text()='Incorrect old password']")
    private WebElement incorrectOldPasswordMessage;

    @FindBy(xpath = "//li[contains(@class,'text-red-600')]")
    private WebElement errorMessage;

    @FindBy(xpath = "//input[@type='file']")
    private WebElement image;

    public ProfilePage(){
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void clickSaveChanges(){
        saveChangesButton.click();
    }

    public String getSuccessfulMessage(){
        wait.until(ExpectedConditions.visibilityOf(successfulMessage));
        return successfulMessage.getText();
    }

    public void enterData(String firstName, String lastName){
        firstNameField.sendKeys(firstName);
        lastNameField.sendKeys(lastName);
    }

    public boolean isCharactersExceededMessageVisible(){
        wait.until(ExpectedConditions.visibilityOf(charactersExceededMessage));
        return charactersExceededMessage.isDisplayed();
    }

    public void clickChangePassword(){
        changePassword.click();
    }

    public void enterPasswords(String oldPassword, String newPassword, String confirmPassword){
        oldPasswordField.sendKeys(oldPassword);
        newPasswordField.sendKeys(newPassword);
        confirmNewPasswordField.sendKeys(confirmPassword);
    }

    public String incorrectOldPasswordMessage(){
        wait.until(ExpectedConditions.visibilityOf(incorrectOldPasswordMessage));
        return incorrectOldPasswordMessage.getText();
    }

    public String getErrorMessage(){
        wait.until(ExpectedConditions.visibilityOf(errorMessage));
        return errorMessage.getText();
    }

    public void uploadImage(String imagePath)
    {
        image.sendKeys(imagePath);
    }
}
