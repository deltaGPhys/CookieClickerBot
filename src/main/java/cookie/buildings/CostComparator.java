package cookie.buildings;

import java.util.Comparator;

public class CostComparator implements Comparator<Building> {

    public int compare(Building building1, Building building2) {
        return Long.compare(building1.getCost(), building2.getCost());
    }
}
