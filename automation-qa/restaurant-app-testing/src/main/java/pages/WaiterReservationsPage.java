package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.DriverManager;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class WaiterReservationsPage {

    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(xpath = "//a[.='Reservations']")
    private WebElement reservationsButton;

    @FindBy(xpath = "(//button[contains(@class,'text-green-600')])[1]")
    private WebElement waiterReservationsSearch;

    @FindBy(xpath = "//div[contains(@class,'md:grid-cols-3')]")
    private List<WebElement> getReservations;

    @FindBy(xpath = "//input[@type='date']")
    private WebElement date;

    @FindBy(xpath = "(//input[@type='date'])[2]")
    private WebElement dateInsideCreateReservation;

    @FindBy(xpath = "//button[.='+ Create New Reservation']")
    private WebElement createNewReservation;

    @FindBy(xpath = "//button[.='+']")
    private WebElement guests;

    @FindBy(xpath = "(//span[@data-slot='select-value'])[3]//parent::button")
    private WebElement table;

    @FindBy(xpath = "(//span[@data-slot='select-value'])[1]//parent::button")
    private WebElement timeFrom;

    @FindBy(id = "visitor")
    private WebElement visitor;

    @FindBy(xpath = "//button[.='Make a Reservation']")
    private WebElement makeAReservation;

    @FindBy(xpath = "//li/div/div")
    private WebElement confirmationMessage;

    @FindBy(id = "customer")
    private WebElement customer;

    @FindBy(xpath = "//input[@placeholder='e.g. customer@example.com']")
    private WebElement customerEmail;

    public WaiterReservationsPage()
    {
        driver = DriverManager.getDriver();
        PageFactory.initElements(driver,this);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void waiterClickOnReservations(){
        reservationsButton.click();
    }

    public void waiterClickOnSearchReservations(){
        wait.until(ExpectedConditions.visibilityOf(waiterReservationsSearch));
        waiterReservationsSearch.click();
    }

    public void sendDate(String dateInput){
        wait.until(ExpectedConditions.visibilityOf(date));
        date.sendKeys(dateInput);
    }

    public int getFilteredReservations(){
        return getReservations.size();
    }

    public void clickOnCreateNewReservation(){
        wait.until(ExpectedConditions.visibilityOf(createNewReservation));
        createNewReservation.click();
    }

    public void clickOnVisitor(){
        visitor.click();
    }

    public void clickOnCustomer(){
        customer.click();
    }

    public void enterTheGuests(){
        guests.click();
    }

    public void enterTheTimeFrom(String time){
        wait.until(ExpectedConditions.elementToBeClickable(timeFrom)).click();

        // XPath to the dropdown item that matches the time
        WebElement timeOption = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format("//div[@role='option' or @data-slot='item']//div[text()='%s']", time))
        ));
        timeOption.click();
    }

    public void enterTheTableNumber(String tableInput)
    {
        wait.until(ExpectedConditions.elementToBeClickable(table)).click();

        WebElement tableOption = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format("//div[@role='option' or @data-slot='item']//div[text()='%s']", tableInput))
        ));
        tableOption.click();
    }

    public void clickOnMakeAReservation() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", makeAReservation);
        //makeAReservation.click();
    }

    public String getConfirmationMessage() throws InterruptedException {
        Thread.sleep(2000);
        return confirmationMessage.getText();
    }

    public void sendDateInsideCreateReservation(String dateInput){
        dateInsideCreateReservation.clear();
        dateInsideCreateReservation.sendKeys(dateInput);
    }

    public void sendCustomerEmail(String email)
    {
        customerEmail.sendKeys(email);
    }
}
