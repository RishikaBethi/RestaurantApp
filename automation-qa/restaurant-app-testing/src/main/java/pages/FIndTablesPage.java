package pages;

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
import java.util.concurrent.TimeUnit;

public class FIndTablesPage {

    private WebDriver driver;
    private WebDriverWait wait;

    //@FindBy(xpath = "//a[.='Book a Table']")
    @FindBy(partialLinkText = "Book a Table")
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

    @FindBy(xpath = "//button[contains(@class, 'border')]")
    private List<WebElement> timeSlotButtons;

    @FindBy(xpath = "//button[text()='+']")
    private WebElement incrementButton;

    @FindBy(xpath = "//span[@class='font-medium text-gray-700']")
    private WebElement reservationFormGuests;

    @FindBy(xpath = "//span[@class='font-semibold text-green-600']")
    private WebElement guestsCount;

    @FindBy(xpath = "//h2[text()='Reservation Confirmed!']")
    private WebElement reservationConfirmationMessage;

    @FindBy(xpath = "//button[text()='Make a Reservation']")
    private WebElement makeAReservation;

    public FIndTablesPage(){
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

    public boolean visibilityOfTimeSlots() {
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        return !timeSlotButtons.isEmpty();
    }

    public String getErrorMessage(){
        wait.until(ExpectedConditions.visibilityOf(errorMessage));
        return errorMessage.getText();
    }

    public void clickOnTimeSlot(){
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        timeSlotButtons.get(0).click();
    }

    public void increment(){
        wait.until(ExpectedConditions.visibilityOf(reservationFormGuests));
        incrementButton.click();
        incrementButton.click();
    }

    public String getGuestsCount(){
        return guestsCount.getText();
    }

    public String getReservationConfirmationMessage(){
        return reservationConfirmationMessage.getText();
    }

    public void clickOnMakeAReservation(){
        makeAReservation.click();
    }
}
