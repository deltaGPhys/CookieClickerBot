public class NumberUtils {

    public static long longParse(String string) {
        if (string.equals("")) {
            return 0L;
        } else {
            string = string.replace(",","");
            // gonna need to interpret millions, etc.
            return Long.parseLong(string);
        }

    }

    public static int intParse(String string) {
        if (string.equals("")) {
            return 0;
        } else {
            string = string.replace(",","");
            // gonna need to interpret millions, etc.
            return Integer.parseInt(string);
        }

    }
}
