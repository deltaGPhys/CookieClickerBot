package cookie;

public class NumberUtils {

    public static long longParse(String string) {
        if (string.equals("")) {
            return 0L;
        } else {
            long mult = 1;
            string = string.replace(",","");

            if (string.contains("million")) {
                mult = 1000000;
            } else if (string.contains("billion")) {
                mult = 1000000000;
            } else if (string.contains("trillion")) {
                mult = 1000000000000L;
            }
            string = string.replaceAll("[^0-9]","");
            return mult * Long.parseLong(string);
        }

    }

    public static int intParse(String string) {
        if (string.equals("")) {
            return 0;
        } else {
            int mult = 1;
            string = string.replace(",","");

            if (string.contains("million")) {
                mult = 1000000;
            } else if (string.contains("billion")) {
                mult = 1000000000;
            }
            string = string.replaceAll("[^0-9]","");
            return mult * Integer.parseInt(string);
        }

    }
}
