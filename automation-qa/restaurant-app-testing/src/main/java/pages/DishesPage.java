package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;

public class DishesPage extends BasePage {

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

    public DishesPage() {
        super();
    }

    public int getNumberOfStaticDishesOnMainPage(){
        return staticMenu.size();
    }

    public void clickOnViewMenu(){
        click(viewMenu);
    }

    public String getDisplayMessage(){
        waitForElementToBeVisible(displayMessage);
        return getTextOfElement(displayMessage);
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
                waitForElementToBeVisible(childElement);
                String price = childElement.getText().replace("$", "").trim();
                prices.add(Integer.parseInt(price));
            } catch (Exception e) {
                System.out.println("Price not found for one element: " + e.getMessage());
            }
        }
        return prices;
    }

}

