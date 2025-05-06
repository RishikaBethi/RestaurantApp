package pages;

import io.cucumber.java.eo.Se;
import org.checkerframework.checker.signature.qual.FullyQualifiedName;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.DriverManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class DishesPage {

    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(xpath = "//div[contains(@class,'md:grid-cols-4')]")
    private List<WebElement> staticMenu;

    @FindBy(xpath = "//button[.='View Menu']")
    private WebElement viewMenu;

    @FindBy(xpath = "//li/div/div")
    private WebElement displayMessage;

    @FindBy(xpath = "//div[contains(@class,'sm:grid-cols-2')]/div")
    private List<WebElement> dynamicMenu;

    @FindBy(id = "sort")
    private WebElement sort;

    public DishesPage()
    {
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver,this);
    }

    public int getNumberOfStaticDishesOnMainPage(){
        return staticMenu.size();
    }

    public void clickOnViewMenu(){
        viewMenu.click();
    }

    public String getDisplayMessage(){
        wait.until(ExpectedConditions.visibilityOf(displayMessage));
        return displayMessage.getText();
    }

    public int getNumberOfDynamicDishes(){
        return dynamicMenu.size();
    }

    public void selectSortTypeFromDropDown(String type){
        Select sort_dd = new Select(sort);
        sort_dd.selectByVisibleText(type);
    }

    public List<Integer> getPricesOfDishes() {
        List<Integer> prices = new ArrayList<>();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement scrollElement = driver.findElement(By.xpath("//button[.='All']"));
        js.executeScript("arguments[0].scrollIntoView()", scrollElement);

        for (WebElement element : dynamicMenu) {
            try {
                WebElement childElement = element.findElement(By.xpath(".//span[contains(text(), '$')]"));
                wait.until(ExpectedConditions.visibilityOf(childElement));
                String price = childElement.getText().replace("$", "").trim();
                prices.add(Integer.parseInt(price));
            } catch (Exception e) {
                System.out.println("Price not found for one element: " + e.getMessage());
            }
        }
        return prices;
    }

}

