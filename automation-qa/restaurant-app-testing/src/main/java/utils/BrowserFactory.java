package utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class BrowserFactory {

    public static WebDriver createDriver(String browser) {
        WebDriver driver;
        switch (browser) {
            case "Edge":
                driver = new EdgeDriver();
                break;
            case "FireFox":
                driver = new FirefoxDriver();
                break;
            case "Chrome":
            default:
                driver = new ChromeDriver();
                break;
        }

        driver.manage().window().maximize();
        return driver;
    }
}
