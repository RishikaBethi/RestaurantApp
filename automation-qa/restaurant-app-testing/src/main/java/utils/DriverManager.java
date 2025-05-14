package utils;

import org.openqa.selenium.WebDriver;

public class DriverManager {

    private static ThreadLocal<WebDriver> threadLocal = new ThreadLocal<>();

    private DriverManager() {}

    public static void createNewDriver() {
        quitDriver(); // Ensure old one is gone
        threadLocal.set(BrowserFactory.createDriver());
    }

    public static WebDriver getDriver() {
        return threadLocal.get();
    }

    public static void quitDriver() {
        WebDriver driver = threadLocal.get();
        if (driver != null) {
            driver.quit();
            threadLocal.remove();
        }
    }
}
