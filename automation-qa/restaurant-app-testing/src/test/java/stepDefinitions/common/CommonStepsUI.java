package stepDefinitions.common;

import context.ShareContextUI;
import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import pages.*;
import utils.ConfigReader;
import utils.DriverManager;

public class CommonStepsUI {

    private static final Logger log = LoggerFactory.getLogger(CommonStepsUI.class);
    private WebDriver driver;
    private LoginPage loginPage;
    private SignUpPage signUpPage;
    private LocationPages locationPages;
    private FIndTablesPage FIndTablesPage;
    private HomePage homePage;
    private ViewReservationsPage viewReservationsPage;
    private FeedbacksPage feedbacksPage;
    private ProfilePage profilePage;
    private ShareContextUI shareContextUI;
    private WaiterReservationsPage waiterReservationsPage;
    private DishesPage dishesPage;

    public CommonStepsUI(ShareContextUI shareContextUI)
    {
        this.shareContextUI = shareContextUI;
    }

    @Given("the user enters into the application")
    public void enterTheBaseUrl() {
        driver = DriverManager.getDriver();
        driver.get(ConfigReader.getProperty("base_url"));
        loginPage = new LoginPage();
        signUpPage = new SignUpPage();
        locationPages = new LocationPages();
        FIndTablesPage = new FIndTablesPage();
        homePage = new HomePage();
        viewReservationsPage = new ViewReservationsPage();
        feedbacksPage = new FeedbacksPage();
        profilePage = new ProfilePage();
        waiterReservationsPage = new WaiterReservationsPage();
        dishesPage = new DishesPage();
    }


    @And("the user enters into the sign in page")
    public void enterIntoTheSignInPage() {
        loginPage.enterTheSignInPage();
    }

    @When("the user enters the {string} and {string}")
    public void enterTheCredentials(String email, String password) {
        loginPage.enterValidCredentials(email, password);
    }

    @And("the user clicks on {string} button")
    public void clicksOnASpecificButton(String button) throws InterruptedException {
        switch (button) {
            case "sign in" -> {
                loginPage.clickOnSignInButton();
                shareContextUI.setCurrentPage("sign in");
            }

            case "create an account link"->
                signUpPage.clickCreateAccountLink();

            case "create an account" ->
                signUpPage.clickCreateAccountButton();

            case "Cuisine Ratings" -> {
                locationPages.clickOnCuisineRatings();
                shareContextUI.setCurrentPage("main page");
            }

            case "Book a Table" -> {
                FIndTablesPage.clickOnBookATable();
                shareContextUI.setCurrentPage("Book a Table");
            }

            case "Find a Table" ->
                FIndTablesPage.clickOnFindATable();

            case "Make a Reservation" ->
                FIndTablesPage.clickOnMakeAReservation();

            case "Reservations" ->
                homePage.openReservations();

            case "Update Feedback" ->
                viewReservationsPage.clickUpdateFeedback();

            case "Culinary Experience" ->
                feedbacksPage.clickCulinaryExperience();

            case "Save Changes" ->
                profilePage.clickSaveChanges();

            case "Change Password" ->
                profilePage.clickChangePassword();

            case "WaiterReservations" ->
                waiterReservationsPage.waiterClickOnReservations();

            case "waiterReservationsSearch" ->
                waiterReservationsPage.waiterClickOnSearchReservations();

            case "CreateNewReservation" ->
                waiterReservationsPage.clickOnCreateNewReservation();

            case "Visitor" ->
                waiterReservationsPage.clickOnVisitor();

            case "Customer" ->
                waiterReservationsPage.clickOnCustomer();

            case "Make a waiter Reservation" ->
                waiterReservationsPage.clickOnMakeAReservation();

            case "View Menu" ->{
                dishesPage.clickOnViewMenu();
            }

            default->
                throw new customExceptions.NoButtonFoundException("No such button found");
        }
    }

    @Then("the user will be redirected to the {string} page")
    public void verifyRedirectionToMainPage(String page) throws InterruptedException {
        Thread.sleep(2000);
        Assert.assertEquals(driver.getCurrentUrl(), page);
    }

    @Then("the page will display the error {string} message")
    public void verifyTheErrorMessage(String message) {
        String currentPage = shareContextUI.getCurrentPage();
        switch (currentPage)
        {
            case "sign in":
                Assert.assertEquals(loginPage.getErrorMessage(),message);
                break;
            case "Book a Table":
                Assert.assertEquals(FIndTablesPage.getErrorMessage(),message);
        }
    }

}
