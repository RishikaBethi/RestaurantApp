package pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ProfilePage extends BasePage{

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
        super();
    }

    public void clickSaveChanges(){
        saveChangesButton.click();
    }

    public String getSuccessfulMessage(){
        waitForElementToBeVisible(successfulMessage);
        return getTextOfElement(successfulMessage);
    }

    public void enterData(String firstName, String lastName){
        enterTextInField(firstNameField, firstName);
        enterTextInField(lastNameField, lastName);
    }

    public boolean isCharactersExceededMessageVisible(){
        waitForElementToBeVisible(charactersExceededMessage);
        return charactersExceededMessage.isDisplayed();
    }

    public void clickChangePassword(){
        click(changePassword);
    }

    public void enterPasswords(String oldPassword, String newPassword, String confirmPassword){
        oldPasswordField.sendKeys(oldPassword);
        newPasswordField.sendKeys(newPassword);
        confirmNewPasswordField.sendKeys(confirmPassword);
    }

    public String incorrectOldPasswordMessage(){
        waitForElementToBeVisible(incorrectOldPasswordMessage);
        return getTextOfElement(incorrectOldPasswordMessage);
    }

    public String getErrorMessage(){
        waitForElementToBeVisible(errorMessage);
        return getTextOfElement(errorMessage);
    }

    public void uploadImage(String imagePath) {
        enterTextInField(image, imagePath);
    }
}
