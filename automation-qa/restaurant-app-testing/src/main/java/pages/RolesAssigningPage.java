package pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class RolesAssigningPage extends BasePage{

    @FindBy(xpath = "//span[@data-slot='avatar-fallback']")
    private WebElement userLogo;

    @FindBy(xpath = "//div[contains(@class,'p-1')]/p[1]")
    private WebElement role;

    public RolesAssigningPage(){
        super();
    }

    public void clickOnUserLogo(){
        waitForElementToBeVisible(userLogo);
        click(userLogo);
    }

    public String getRole(){
        waitForElementToBeVisible(role);
        return getTextOfElement(role);
    }
}
