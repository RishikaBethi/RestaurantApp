package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

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
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[.='Create an Account']")));
        createAccountLink.click();
    }

    public void enterTheDetails(String firstNameData, String lastNameData, String emailData, String passwordData, String confirmPasswordData) {
        firstName.sendKeys(firstNameData);
        lastName.sendKeys(lastNameData);
        email.sendKeys(emailData);
        password.sendKeys(passwordData);
        confirmPassword.sendKeys(confirmPasswordData);
    }

    public String getErrorMessage(){
        return errorMessage.getText();
    }

    public void clickCreateAccountButton(){
        createAccountButton.click();
    }

    public String getPasswordErrorMessage(){
        return passwordErrorMessage.getText();
    }
}
