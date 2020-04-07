package cookie;

import org.openqa.selenium.WebDriver;

import java.util.concurrent.TimeUnit;

public class BrowserUtils {

    public static WebDriver getDesktopDriver() {
        WebDriver driver;

        LocalWebDriverFactory factory = new LocalWebDriverFactory();
        driver = factory.createDesktopWebDriver();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        //setup(driver);
        return driver;
    }
}
