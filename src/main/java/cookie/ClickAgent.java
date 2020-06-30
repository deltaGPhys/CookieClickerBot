package cookie;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import cookie.buildings.Building;
import cookie.buildings.IdComparator;
import cookie.exceptions.BuildingFormatException;
import cookie.exceptions.IncompleteMetricsException;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import sun.jvm.hotspot.debugger.Page;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClickAgent {

    private WebDriver driver;
    private long cookieCountSessionStart;
    private long cookieCount;
    private long passiveRate;
    private String bakeryName = System.getenv("BAKERY_NAME");
    private String saveKey;
    private String saveFile = System.getenv("SAVE_PATH") + bakeryName.replace(" ","") + "Bakery.txt";
    private String S3_BUCKET = System.getenv("S3_BUCKET");
    private String AWS_ACCESS_KEY = System.getenv("AWS_ACCESS_KEY");
    private String AWS_SECRET_KEY = System.getenv("AWS_SECRET_KEY");
    private String S3_FILE_KEY = bakeryName.replace(" ","") + "Bakery.txt";


    public ClickAgent() throws InterruptedException {
        if (this.driver == null) {
            this.driver = BrowserUtils.getDesktopDriver();
            this.driver.navigate().to(URLDefinitions.cookieClickerURL);
            Thread.sleep(3000);
        }
    }

    public static void main(String[] args) throws Exception, IncompleteMetricsException {
        ClickAgent agent = new ClickAgent();

        //SlackReporter.sendSimpleMessage(String.format("File %s retrieved from S3 bucket %s: %s", agent.S3_FILE_KEY, agent.S3_BUCKET, agent.loadFromS3()));
        SlackReporter.sendSimpleMessage("Game loaded: " + agent.loadGame());

        agent.cookieCountSessionStart = agent.getCookieCount();
        SlackReporter.sendSimpleMessage("Initial count: " + NumberUtils.longPrint(agent.cookieCountSessionStart));

        int clickNum = 2000;
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
            SlackReporter.sendSimpleMessage(String.format("File %s saved to S3 bucket %s: %s", agent.S3_FILE_KEY, agent.S3_BUCKET, agent.saveToS3()));
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
        int goldenInterval = 250;

        for (int i = 0; i < clickNum; i++) {

            if (i % goldenInterval == 0 && i > 0) {
                this.goldenCookieCheck();
            }

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

//                closeNotifications();
            }
            cookie.click();
        }

        Thread.sleep(2000);

        return getCookieCount();
    }

    public void closeNotifications() {
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.MILLISECONDS);
        List<WebElement> closers = driver.findElements(PageElements.byNotificationCloser);
        if (closers.size() > 0) {
            closers.get(0).click();
        }
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    public long getCookieCount() {
        WebElement cookieCountDisplay = driver.findElement(PageElements.byCookieCount);
        this.cookieCount = NumberUtils.longParse(cookieCountDisplay.getText().split(":")[0]);
        this.passiveRate = NumberUtils.longParse(cookieCountDisplay.getText().split(":")[1]);

        return this.cookieCount;
    }

    public void goldenCookieCheck() throws Exception {
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.MILLISECONDS);
        List<WebElement> shimmers = driver.findElements(PageElements.byGoldenCookieShimmer);
        if (shimmers.size() > 0) {
            driver.findElement(PageElements.byGoldenCookieShimmer).click();
            SlackReporter.sendSimpleMessage("Golden cookie clicked");
        }
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
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

            if (!businessSeason && Building.getByName(name) == null) {
                System.out.println(name);
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
        buildings.sort(new IdComparator().reversed());
        long totalPurchase = 0;

        for (Building building: buildings) {
            if (building.equals(Building.GRANDMA)) { //grandmapocalypse
                continue;
            }
            if (this.cookieCount > 10000000 * building.getCost() && building.getCost() != 0) {
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

    public boolean loadFromS3() throws Exception {
        SlackReporter.sendSimpleMessage(String.format("Downloading %s from S3 bucket %s...\n", S3_FILE_KEY, S3_BUCKET));
        AWSCredentials credentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY);
        final AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();
        try {
            S3Object o = s3.getObject(S3_BUCKET, S3_FILE_KEY);
            S3ObjectInputStream s3is = o.getObjectContent();
            FileUtils.copyInputStreamToFile(s3is, new File(saveFile));
            s3is.close();
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return true;
    }

    public boolean saveToS3() throws Exception {
        SlackReporter.sendSimpleMessage(String.format("Saving %s to S3 bucket %s...\n", S3_FILE_KEY, S3_BUCKET));
        AWSCredentials credentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY);
        final AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();
        try {
            s3.putObject(S3_BUCKET, S3_FILE_KEY, new File(saveFile));
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        return true;
    }
}
