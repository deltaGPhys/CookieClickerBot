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
            } else if (string.contains("quadrillion")) {
                mult = 1000000000000000L;
            } else if (string.contains("quintillion")) {
                mult = 1000000000000000000L;
            }
            string = string.replaceAll("[^0-9.]","");
            return (long) (mult * Double.parseDouble(string));
        }

    }

    public static String longPrint(long num) {
        if (Math.abs(num) > 1000000000000000000L) {
            return String.format("%.3f quintillion", num/1000000000000000000.0);
        } else if (Math.abs(num) > 1000000000000000L) {
            return String.format("%.3f quadrillion", num/1000000000000000.0);
        } else if (Math.abs(num) > 1000000000000L) {
            return String.format("%.3f trillion", num/1000000000000.0);
        } else if (Math.abs(num) > 1000000000) {
            return String.format("%.3f billion", num/1000000000.0);
        } else if (Math.abs(num) > 1000000) {
            return String.format("%.3f million", num/1000000.0);
        } else {
            return String.valueOf(num);
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

    public static String intPrint(int num) {
        if (Math.abs(num) > 1000000000) {
            return num/1000000000.0 + " billion";
        } else if (Math.abs(num) > 1000000) {
            return num/1000000.0 + " million";
        } else {
            return String.valueOf(num);
        }
    }
}
