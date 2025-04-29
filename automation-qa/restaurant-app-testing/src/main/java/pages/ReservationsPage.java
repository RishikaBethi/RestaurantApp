package pages;

import io.cucumber.java.sl.In;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.DriverManager;

import java.time.Duration;
import java.util.List;

public class ReservationsPage {

    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(xpath = "//a[.='Book a Table']")
    private WebElement bookATable;

    @FindBy(xpath = "//button[.='Find a Table']")
    private WebElement findATable;

    @FindBy(xpath = "(//select[contains(@class,'w-full')])[1]")
    private WebElement locations;

    @FindBy(xpath = "//input[contains(@class,'w-full')]")
    private WebElement date;

    @FindBy(xpath = "(//select[contains(@class,'w-full')])[2]")
    private WebElement timeSlot;

    @FindBy(xpath = "//input[contains(@class,'text-center')]")
    private WebElement guests;

    @FindBy(xpath = "//div[contains(@class,'md:grid-cols-2')]/div")
    private List<WebElement> availableTables;

    @FindBy(xpath = "//li/div/div")
    private WebElement errorMessage;

    public ReservationsPage(){
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver,this);
    }

    public void clickOnBookATable(){
        bookATable.click();
    }

    public void clickOnFindATable(){
        wait.until(ExpectedConditions.visibilityOf(findATable));
        findATable.click();
    }

    public void selectTimeFromDropDown(String time){
        wait.until(ExpectedConditions.visibilityOf(timeSlot));
        Select timeSlot_dd = new Select(timeSlot);
        timeSlot_dd.selectByValue(time);
    }

    public int getNumberOfAvailableTables(){
        return availableTables.size();
    }

    public void sendDetailsToBookATable(String location,String InputDate,String InputTime,String InputGuests)
    {
        wait.until(ExpectedConditions.visibilityOf(locations));
        wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(locations, By.xpath(".//option[@value='" + location + "']")));
        Select locations_dd = new Select(locations);
        locations_dd.selectByValue(location);
        date.clear();
        date.sendKeys(InputDate);
        selectTimeFromDropDown(InputTime);
        guests.clear();
        guests.sendKeys(InputGuests);
    }

    public String getErrorMessage(){
        wait.until(ExpectedConditions.visibilityOf(errorMessage));
        return errorMessage.getText();
    }

}
