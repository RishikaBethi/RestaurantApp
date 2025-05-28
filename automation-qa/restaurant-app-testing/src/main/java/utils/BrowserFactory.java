package utils;

import customExceptions.NoSuchBrowserException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class BrowserFactory {

    public static WebDriver createDriver() {
        WebDriver driver;
        String browser = System.getProperty("browser");
        switch (browser.toLowerCase()) {
            case "edge" -> driver = new EdgeDriver();
            case "firefox" -> driver = new FirefoxDriver();
            case "chrome" -> driver = new ChromeDriver();
            default -> throw new NoSuchBrowserException("Enter a valid browser name");
        }
        driver.manage().window().maximize();
        return driver;
    }
}
