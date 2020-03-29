import Exceptions.BuildingFormatException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import sun.jvm.hotspot.debugger.Page;

import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ClickAgent {

    private WebDriver driver;
    private long cookieCount;
    private String bakeryName = "Cool Robot";
    private String saveKey;
    private String saveFile = "/Users/joshua.gates/" + bakeryName.replace(" ","") + "Bakery.txt";

    public ClickAgent() throws InterruptedException {
        if (this.driver == null) {
            this.driver = BrowserUtils.getDesktopDriver();
            this.driver.navigate().to(URLDefinitions.cookieClickerURL);
            Thread.sleep(3000);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ClickAgent agent = new ClickAgent();

        System.out.println("Game loaded: " + agent.loadGame());

        agent.updateBuildings();
//        for (int i = 0; i < 3; i++) {
//            agent.doClicks(1000);
//
//            System.out.println("Game saved: " + agent.exportGame());
//        }
        System.out.println(agent.getCookieCount() + " cookies");

        agent.driver.close();
    }

    public long doClicks(int clickNum) throws InterruptedException {

        WebElement cookie = driver.findElement(PageElements.byBigCookie);
        for (int i = 0; i < clickNum; i++) {
            cookie.click();
        }

        Thread.sleep(2000);

        return getCookieCount();
    }

    public long getCookieCount() {
        WebElement cookieCountDisplay = driver.findElement(PageElements.byCookieCount);
        this.cookieCount = Long.parseLong(cookieCountDisplay.getText().split(":")[0].replaceAll("[^0-9]",""));

        return this.cookieCount;
    }

    public void updateBuildings() {
        List<WebElement> elements = driver.findElements(By.className("product"));

        for (WebElement element: elements) {
            // id and name
            int id = Integer.parseInt(element.getAttribute("id").replace("product",""));
            String name = driver.findElement(By.id("productName"+id)).getText();
            if (name.equals("")) {
                continue;
            }
            Building building = Building.valueOf(name.toUpperCase());
            if (building.getId() != id) {
                throw new BuildingFormatException();
            }
            building.setElement(element);

            // interpret class data
            String[] classes = element.getAttribute("class").split(" ");
            for (String clazz : classes) {
                if (clazz.equals("unlocked")) {
                    building.setUnlocked(true);
                } else if (clazz.equals("enabled")) {
                    building.setAffordable(true);
                }
            }

            // set numerical data
            if (building.isUnlocked()) {
                building.setCost(NumberUtils.longParse(driver.findElement(By.id("productPrice" + id)).getText()));
                building.setQty(NumberUtils.intParse(driver.findElement(By.id("productOwned" + id)).getText()));
            }

            System.out.println(building.toString());
        }
    }

    public boolean loadGame() throws InterruptedException {
        driver.findElement(PageElements.byOptionsButton).click();
        Thread.sleep(1000);
        driver.findElement(PageElements.byFileInputBox).sendKeys(this.saveFile);
        Thread.sleep(1000);
        driver.findElement(PageElements.byMenuClose).click();

        return driver.findElement(PageElements.byBakeryName).getText().equals(this.bakeryName + "'s bakery");
    }

    public boolean loadSaveKey() {
        try {
            this.saveKey = new String(Files.readAllBytes(Paths.get(this.saveFile)));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println(this.saveKey);
        return true;
    }

    public boolean storeSaveKey() {

        try {
            if (!this.saveKey.equals("")) {
                Files.write(Paths.get(this.saveFile), this.saveKey.getBytes());
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public boolean importGame() throws InterruptedException {
        // sendKeys() is pretty slow - if JS exectuor worked, it'd be faster
        driver.findElement(PageElements.byOptionsButton).click();
        Thread.sleep(1000);
        driver.findElement(PageElements.byImportButton).click();
        if (loadSaveKey()) {
            //JavascriptExecutor js = (JavascriptExecutor)driver;
            System.out.println(this.saveKey);
            //js.executeScript("document.getElementById('textareaPrompt').setAttribute('value', '" + this.saveKey + "')");
            driver.findElement(PageElements.byimportBox).sendKeys(this.saveKey);
            Thread.sleep(1000);
            driver.findElement(PageElements.byImportLoad).click();
            Thread.sleep(1000);
            driver.findElement(PageElements.byMenuClose).click();

            return driver.findElement(PageElements.byBakeryName).getText().equals(this.bakeryName + "'s bakery");
        } else {
            return false;
        }
    }

    public boolean saveGame() throws InterruptedException {
        driver.findElement(PageElements.byOptionsButton).click();
        Thread.sleep(1000);
        // unfinished

        return false;
    }

    public boolean exportGame() throws InterruptedException {
        driver.findElement(PageElements.byOptionsButton).click();
        Thread.sleep(1000);
        driver.findElement(PageElements.byExportButton).click();
        Thread.sleep(1000);
        this.saveKey = driver.findElement(PageElements.byimportBox).getText();
        driver.findElement(PageElements.byExportLoad).click();
        Thread.sleep(1000);
        driver.findElement(PageElements.byMenuClose).click();

        return storeSaveKey();
    }
}
