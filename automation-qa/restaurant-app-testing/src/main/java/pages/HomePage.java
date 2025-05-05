package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import utils.DriverManager;

public class HomePage {

    private WebDriver driver;

    @FindBy(linkText = "Reservations")
    private WebElement reservations;

    @FindBy(xpath = "//span[@data-slot='avatar-fallback']")
    private WebElement userIcon;

    @FindBy(partialLinkText = "Profile")
    private WebElement myProfile;

    public HomePage(){
        driver = DriverManager.getDriver();
        PageFactory.initElements(driver, this);
    }

    public void openReservations(){
        reservations.click();
    }

    public void openMyProfile(){
        userIcon.click();
        myProfile.click();
    }
}
