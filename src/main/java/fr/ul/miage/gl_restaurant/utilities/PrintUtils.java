package fr.ul.miage.gl_restaurant.utilities;

public class PrintUtils {

    private PrintUtils() {}

    public static void print(String format, Object... arguments) {
        System.out.printf(format, arguments);
    }

    public static void println(String format, Object... arguments) {
        print(format + "%n", arguments);
    }

    public static void println() {
        print("%n");
    }
}
