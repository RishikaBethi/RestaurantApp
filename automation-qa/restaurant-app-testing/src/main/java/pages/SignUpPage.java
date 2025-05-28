package pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class SignUpPage extends BasePage{

    @FindBy(xpath = "//a[.='Create an Account']")
    private WebElement createAccountLink;

    @FindBy(id = "firstName")
    private WebElement firstName;

    @FindBy(id = "lastName")
    private WebElement lastName;

    @FindBy(id = "email")
    private WebElement email;

    @FindBy(id = "password")
    private WebElement password;

    @FindBy(id = "confirmPassword")
    private WebElement confirmPassword;

    @FindBy(xpath = "//p[contains(@class,'text-red-500')]")
    private WebElement errorMessage;

    @FindBy(xpath = "//button[.='Create an Account']")
    private WebElement createAccountButton;

    @FindBy(xpath = "//li[contains(@class,'text-red-500')]")
    private WebElement passwordErrorMessage;

    public SignUpPage(){
        super();
    }

    public void clickCreateAccountLink(){
        waitForElementToBeVisible(createAccountLink);
        click(createAccountLink);
    }

    public void enterTheDetails(String firstNameData, String lastNameData, String emailData, String passwordData, String confirmPasswordData) {
        enterTextInField(firstName, firstNameData);
        enterTextInField(lastName, lastNameData);
        enterTextInField(email, emailData);
        enterTextInField(password, passwordData);
        enterTextInField(confirmPassword, confirmPasswordData);
    }

    public String getErrorMessage(){
        return getTextOfElement(errorMessage);
    }

    public void clickCreateAccountButton(){
        click(createAccountButton);
    }

    public String getPasswordErrorMessage(){
        return getTextOfElement(passwordErrorMessage);
    }
}
