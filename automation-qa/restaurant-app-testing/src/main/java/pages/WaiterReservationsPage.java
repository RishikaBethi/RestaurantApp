package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class WaiterReservationsPage extends BasePage{

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

    public WaiterReservationsPage() {
        super();
    }

    public void waiterClickOnReservations(){
        reservationsButton.click();
    }

    public void waiterClickOnSearchReservations(){
        waitForElementToBeVisible(waiterReservationsSearch);
        click(waiterReservationsSearch);
    }

    public void sendDate(String dateInput){
        waitForElementToBeVisible(date);
        enterTextInField(date, dateInput);
    }

    public int getFilteredReservations(){
        return getReservations.size();
    }

    public void clickOnCreateNewReservation(){
        waitForElementToBeVisible(createNewReservation);
        click(createNewReservation);
    }

    public void clickOnVisitor(){
        click(visitor);
    }

    public void clickOnCustomer(){
        click(customer);
    }

    public void enterTheGuests(){
        click(guests);
    }

    public void enterTheTimeFrom(String time){
        wait.until(ExpectedConditions.elementToBeClickable(timeFrom)).click();

        // XPath to the dropdown item that matches the time
        WebElement timeOption = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format("//div[@role='option' or @data-slot='item']//div[text()='%s']", time))
        ));
        click(timeOption);
    }

    public void enterTheTableNumber(String tableInput) {
        wait.until(ExpectedConditions.elementToBeClickable(table)).click();

        WebElement tableOption = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format("//div[@role='option' or @data-slot='item']//div[text()='%s']", tableInput))
        ));
        click(tableOption);
    }

    public void clickOnMakeAReservation() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", makeAReservation);
    }

    public String getConfirmationMessage() throws InterruptedException {
        Thread.sleep(2000);
        return getTextOfElement(confirmationMessage);
    }

    public void sendDateInsideCreateReservation(String dateInput){
        dateInsideCreateReservation.clear();
        enterTextInField(dateInsideCreateReservation, dateInput);
    }

    public void sendCustomerEmail(String email) {
        enterTextInField(customerEmail, email);
    }
}
