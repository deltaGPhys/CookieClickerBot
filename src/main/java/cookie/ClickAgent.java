package cookie;

import cookie.buildings.Building;
import cookie.buildings.CostComparator;
import cookie.exceptions.BuildingFormatException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.List;

public class ClickAgent {

    private WebDriver driver;
    private long cookieCountSessionStart;
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
        agent.cookieCountSessionStart = agent.getCookieCount();
        System.out.println("Initial count: " + agent.cookieCountSessionStart);

        for (int i = 0; i < 6; i++) {
            long time = System.currentTimeMillis();
            agent.doClicks(2000);
            System.out.println(2000/((System.currentTimeMillis() - time)/1000));

            agent.updateBuildings();
            if (i % 2 == 1) {
                agent.purchaseCheapestUpgrade();
            } else {
                agent.simplePurchaseStrategy();
            }
            System.out.println("Game saved: " + agent.exportGame());

        }
        Thread.sleep(10000);

        System.out.println(String.format("%d cookies: %d gained", NumberUtils.longPrint(agent.getCookieCount()), NumberUtils.longPrint(agent.cookieCount - agent.cookieCountSessionStart)));

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
        this.cookieCount = NumberUtils.longParse(cookieCountDisplay.getText().split(":")[0]);

        return this.cookieCount;
    }

    public void goldenCookieCheck() {
        WebElement goldenCookie = driver.findElement(PageElements.byGoldenCookie);
        //if (goldenCookie.getAttribute(""))
    }

    public void updateBuildings() {
        List<WebElement> elements = driver.findElements(By.className("product"));
        boolean businessSeason = false;
        if (elements.size() > 0) {
            businessSeason = driver.findElement(By.id("productName0")).getText().equals("Rolling pin");
        }


        for (WebElement element: elements) {
            // id and name
            int id = Integer.parseInt(element.getAttribute("id").replace("product",""));
            String name = driver.findElement(By.id("productName"+id)).getText();
            if (name.equals("")) {
                continue;
            }
            Building building = Building.getById(id);

            if (!businessSeason && !name.equals(Building.valueOf(name.toUpperCase()))) {
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

//            Actions hover = new Actions(driver);
//            hover.moveToElement(element);
//            hover.build();
//            hover.perform();
//
//            WebElement tooltip = driver.findElement(cookie.PageElements.byBuildingTooltip);
//            String text = tooltip.getText();
//            System.out.println(text);

//            System.out.println(building.toString());
        }
    }

    public void simplePurchaseStrategy() {
        getCookieCount();
        List<Building> buildings = Arrays.asList(Building.values());
        buildings.sort(new CostComparator().reversed());
        for (Building building: buildings) {

            if (this.cookieCount > 1000 * building.getCost() && building.getCost() != 0) {
                for (int i = 0; i < 5; i++) {
                    building.getElement().click();
                    getCookieCount();
                    System.out.println(building.toString());
                }
            } else if (this.cookieCount > 2 * building.getCost() && building.getCost() != 0) {
                building.getElement().click();
                getCookieCount();
                System.out.println(building.toString());
            }
        }
    }

    public void purchaseCheapestUpgrade() {
        WebElement upgrade = driver.findElement(PageElements.byCheapestUpgrade);
        if (upgrade.getAttribute("class").equals("crate upgrade enabled")) {
            upgrade.click();
            System.out.println("Upgrade purchased");
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
