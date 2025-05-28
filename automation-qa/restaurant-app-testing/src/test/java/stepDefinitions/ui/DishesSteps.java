package stepDefinitions.ui;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.junit.Assert;
import pages.DishesPage;

import java.util.Collections;
import java.util.List;

public class DishesSteps {

    private DishesPage dishesPage;

    public DishesSteps(){
        dishesPage = new DishesPage();
    }

    @Then("the user should be able to see the available {string} dishes")
    public void verifyTheAvailabilityOfStaticMenu(String type){
        switch (type) {
            case "static" ->
                    Assert.assertTrue(dishesPage.getNumberOfStaticDishesOnMainPage()>0);
            case "dynamic" ->
                Assert.assertTrue(dishesPage.getNumberOfDynamicDishes()>0);
            default ->
                throw new customExceptions.NoButtonFoundException("No dishes found");
        }
    }

    @Then("the page will display the {string} dishes message")
    public void verifyTheDisplayMessage(String message){
        Assert.assertEquals(dishesPage.getDisplayMessage(),message);
    }

    @And("the user selects the {string} from dropdown")
    public void selectTheTypeOfSorting(String type)
    {
        dishesPage.selectSortTypeFromDropDown(type);
    }

    @Then("verify whether the price is sorted {string} order")
    public void verifyTheSorting(String order) {
        switch (order) {
            case "Price Low to High" -> {
                List<Integer> actualPrices = dishesPage.getPricesOfDishes();
                List<Integer> expectedPrices = dishesPage.getPricesOfDishes();
                Collections.sort(actualPrices);
                Assert.assertEquals(actualPrices,expectedPrices);
            }

            case "Price High to Low" -> {
                List<Integer> actualPrices = dishesPage.getPricesOfDishes();
                List<Integer> expectedPrices = dishesPage.getPricesOfDishes();
                Collections.sort(actualPrices,Collections.reverseOrder());
                Assert.assertEquals(actualPrices,expectedPrices);
            }
        }
    }
}
