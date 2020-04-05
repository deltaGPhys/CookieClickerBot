package cookie.buildings;

import java.util.Comparator;

public class IdComparator implements Comparator<Building> {

    public int compare(Building building1, Building building2) {
        return Long.compare(building1.getId(), building2.getId());
    }
}

