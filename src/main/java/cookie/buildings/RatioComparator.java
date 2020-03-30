package cookie.buildings;

import java.util.Comparator;

public class RatioComparator implements Comparator<Building> {

    public int compare(Building building1, Building building2) {
        return Double.compare(building1.getRatio(), building2.getRatio());
    }
}
