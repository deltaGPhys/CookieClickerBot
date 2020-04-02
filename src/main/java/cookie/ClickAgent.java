package cookie;

import cookie.buildings.Building;
import cookie.buildings.CostComparator;
import cookie.exceptions.BuildingFormatException;
import cookie.exceptions.IncompleteMetricsException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ClickAgent {

    private WebDriver driver;
    private long cookieCountSessionStart;
    private long cookieCount;
    private long passiveRate;
    private double clickRate;
    private String bakeryName = System.getenv("BAKERY_NAME");
    private String saveKey;
    private String saveFile = System.getenv("SAVE_PATH") + bakeryName.replace(" ","") + "Bakery.txt";

    public ClickAgent() throws InterruptedException {
        if (this.driver == null) {
            this.driver = BrowserUtils.getDesktopDriver();
            this.driver.navigate().to(URLDefinitions.cookieClickerURL);
            Thread.sleep(3000);
        }
    }

    public static void main(String[] args) throws Exception, IncompleteMetricsException {
        ClickAgent agent = new ClickAgent();

        SlackReporter.sendSimpleMessage("Game loaded: " + agent.loadGame());
        agent.cookieCountSessionStart = agent.getCookieCount();
        SlackReporter.sendSimpleMessage("Initial count: " + NumberUtils.longPrint(agent.cookieCountSessionStart));

        int clickNum = 8000;
        int processNum = 1;

        for (int i = 0; i < processNum; i++) {
            SlackReporter.sendSimpleMessage(String.format("Beginning process %d of %d", i+1, processNum));
            SlackReporter.sendSimpleMessage(String.format("Beginning of process: %s cookies\nPassive rate: %s cookies per second", NumberUtils.longPrint(agent.getCookieCount()), NumberUtils.longPrint(agent.passiveRate)));

            agent.doClicks(clickNum);
            SlackReporter.sendSimpleMessage(String.format("End of process: %s cookies\nPassive rate: %s cookies per second", NumberUtils.longPrint(agent.getCookieCount()), NumberUtils.longPrint(agent.passiveRate)));

            agent.updateBuildings();
            if (i % 2 == 1) {
                agent.purchaseCheapestUpgrade();
            } else {
                agent.simplePurchaseStrategy();
            }
            SlackReporter.sendSimpleMessage("Game saved: " + agent.exportGame());

        }
        Thread.sleep(10000);

        SlackReporter.sendSimpleMessage(String.format("%s cookies: %s gained", NumberUtils.longPrint(agent.getCookieCount()), NumberUtils.longPrint(agent.cookieCount - agent.cookieCountSessionStart)));

        agent.driver.close();
    }

    public long doClicks(int clickNum) throws Exception, IncompleteMetricsException {

        WebElement cookie = driver.findElement(PageElements.byBigCookie);
        long batchStartTime = System.currentTimeMillis();
        long batchCookieStart = this.cookieCount;
        int interval = 1000;

        for (int i = 0; i < clickNum; i++) {

            if (i % interval == 0 && i > 0) {
                // calculate metrics for this entire batch up to now, not just this interval

//              // ClickMetrics metrics = new ClickMetrics(batchStartTime, batchCookieStart);
                ClickMetrics metrics = new ClickMetrics(batchStartTime, this.cookieCount);
                getCookieCount();
                metrics.endInterval(System.currentTimeMillis(), this.cookieCount, this.passiveRate, interval);
                batchStartTime = System.currentTimeMillis();
                SlackReporter.sendSimpleMessage(String.valueOf(i));
                metrics.reportMetrics();
                metrics.reportPrediction(clickNum-interval * i/interval);
            }
            cookie.click();
        }

        Thread.sleep(2000);

        return getCookieCount();
    }

    public long getCookieCount() {
        WebElement cookieCountDisplay = driver.findElement(PageElements.byCookieCount);
        this.cookieCount = NumberUtils.longParse(cookieCountDisplay.getText().split(":")[0]);
        this.passiveRate = NumberUtils.longParse(cookieCountDisplay.getText().split(":")[1]);

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

    public void simplePurchaseStrategy() throws Exception {
        getCookieCount();
        List<Building> buildings = Arrays.asList(Building.values());
        buildings.sort(new CostComparator().reversed());
        long totalPurchase = 0;

        for (Building building: buildings) {
            if (this.cookieCount > 5000 * building.getCost() && building.getCost() != 0) {
                for (int i = 0; i < 5; i++) {
                    building.getElement().click();
                    totalPurchase += building.getCost();
                    this.cookieCount -= building.getCost();
                    SlackReporter.sendSimpleMessage(building.toString());
                }
            } else if (this.cookieCount > 2 * building.getCost() && building.getCost() != 0) {
                building.getElement().click();
                totalPurchase += building.getCost();
                this.cookieCount -= building.getCost();
                SlackReporter.sendSimpleMessage(building.toString());
            }
        }
        updateBuildings();
    }

    public void purchaseCheapestUpgrade() throws Exception {
        WebElement upgrade = driver.findElement(PageElements.byCheapestUpgrade);
        if (upgrade.getAttribute("class").equals("crate upgrade enabled")) {
            upgrade.click();
            SlackReporter.sendSimpleMessage("Upgrade purchased");
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
