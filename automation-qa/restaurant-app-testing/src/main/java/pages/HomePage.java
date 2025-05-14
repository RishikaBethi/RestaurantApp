package pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class HomePage extends BasePage{

    @FindBy(linkText = "Reservations")
    private WebElement reservations;

    @FindBy(xpath = "//span[@data-slot='avatar-fallback']")
    private WebElement userIcon;

    @FindBy(partialLinkText = "Profile")
    private WebElement myProfile;

    public HomePage(){
        super();
    }

    public void openReservations(){
        click(reservations);
    }

    public void openMyProfile(){
        click(userIcon);
        click(myProfile);
    }
}
