package pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class RolesAssigningPage extends BasePage{

    @FindBy(xpath = "//span[@data-slot='avatar-fallback']")
    private WebElement userLogo;

    @FindBy(xpath = "//div[contains(@class,'p-1')]/p[1]")
    private WebElement role;

    public RolesAssigningPage(){
        super();
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
