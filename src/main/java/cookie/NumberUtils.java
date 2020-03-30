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
            string = string.replaceAll("[^0-9.]","");
            return (long) (mult * Double.parseDouble(string));
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
            string = string.replaceAll("[^0-9.]","");
            return (int) (mult * Double.parseDouble(string));
        }

    }
}
