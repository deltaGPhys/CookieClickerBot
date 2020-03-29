import lombok.Getter;
import org.openqa.selenium.By;

public class PageElements {

    public static final By byOptionsButton = By.id("prefsButton");
    public static final By byLoadButton = By.linkText("Load from file");
    public static final By byImportButton = By.linkText("Import save");
    public static final By byimportBox = By.id("textareaPrompt");
    public static final By byImportLoad = By.id("promptOption0");
    public static final By byFileInputBox = By.id("FileLoadInput");
    public static final By bySaveButton = By.linkText("Save to file");
    public static final By byExportButton = By.linkText("Export save");
    public static final By byExportLoad = byImportLoad;
    public static final By byMenuClose = By.cssSelector("#menu > div.close.menuClose");

    public static final By byStatsButton = By.id("statsButton");

    public static final By byBakeryName = By.id("bakeryName");
    public static final By byBigCookie = By.id("bigCookie");
    public static final By byCookieCount = By.id("cookies");

}
