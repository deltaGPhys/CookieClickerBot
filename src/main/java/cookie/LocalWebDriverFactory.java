package cookie;

import org.apache.commons.lang.SystemUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.Map;

public class LocalWebDriverFactory {

    private static String executablePath = "./src/main/resources/chromedrivermac-chrome" + getBrowserVersion();

    public static String getBrowserVersion() {
        //TODO: detect chrome version automagically and use the appropriate chromedriver; that driver needs to be added to resources, as well.
        return "84";
    }

    public LocalWebDriverFactory() {
//        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
//        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "info");
//        System.setProperty("org.openqa.selenium.remote.RemoteWebDriver", "info");
        System.setProperty("webdriver.chrome.driver", executablePath);
//        System.setProperty("webdriver.chrome.logfile", "./chromedriver.log");
    }

    private ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        //options.setBinary(executablePath);
        options.addArguments("--disable-gpu");
        options.addArguments("--start-maximized");
        options.addArguments("--mute-audio");
        //options.addArguments("--verbose");
        //options.addArguments("--headless");
        //options.addArguments("--window-size=1366,768");
        //options.addArguments("--single-process");
        //options.addArguments("--no-sandbox");

        return options;
    }

    public WebDriver createDesktopWebDriver() {
        return new ChromeDriver(getChromeOptions());
    }

}
