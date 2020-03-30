package cookie.buildings;

import lombok.Getter;
import org.openqa.selenium.WebElement;

@Getter
public enum Building {
    CURSOR("Cursor", 0),
    GRANDMA("Grandma", 1),
    FARM("Farm", 2),
    MINE("Mine", 3),
    FACTORY("Factory", 4),
    BANK("Bank", 5),
    TEMPLE("Temple", 6),
    WIZARD_TOWER("Wizard Tower", 7),
    SHIPMENT("Shipment", 8),
    ALCHEMY_LAB("Alchemy Lab", 9),
    PORTAL("Portal", 10),
    TIME_MACHINE("Time Machine", 11),
    ANTIMATTER_CONDENSER("Antimatter Condenser", 12),
    PRISM("Prism", 13),
    CHANCEMAKER("Chancemaker", 14),
    FRACTAL_ENGINE("Fractal_Engine", 15),
    JAVASCRIPT_CONSOLE("Javascript Console", 16);

    private String name;
    private int id;
    private long cost;
    private double rate;
    private double ratio;
    private int qty;
    private boolean unlocked;
    private boolean affordable;
    private WebElement element;

    Building(String name, int id) {
        this.name = name;
        this.id = id;
        this.cost = 0L;
        this.rate = 0.0;
        this.ratio = 0.0;
        this.qty = 0;
        this.unlocked = false;
        this.affordable = false;
        this.element = null;
    }

    public void setCost(long cost) {
        this.cost = cost;
        this.ratio = this.rate/this.cost;
    }

    public void setRate(double rate) {
        this.rate = rate;
        this.ratio = this.rate/this.cost;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public void setAffordable(boolean affordable) {
        this.affordable = affordable;
    }

    public void setElement(WebElement element) {
        this.element = element;
    }

    @Override
    public String toString() {
        return "cookie.buildings.Building{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", cost=" + cost +
                ", rate=" + rate +
                ", ratio=" + ratio +
                ", qty=" + qty +
                ", unlocked=" + unlocked +
                ", affordable=" + affordable +
                '}';
    }
}
