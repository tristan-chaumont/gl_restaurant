package fr.ul.miage.gl_restaurant.utilities;

import org.apache.commons.validator.GenericValidator;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class InputUtils {

    private InputUtils() {}

    public static final Scanner scanner = new Scanner(System.in);

    public static String readInput() {
        return scanner.nextLine();
    }

    public static String readInputInArray(List<String> values) {
        String input = readInput();

        while (!values.contains(input)) {
            PrintUtils.println("Votre saisie doit correspondre à l'un des éléments suivants : %s", String.join(", ", values));
            PrintUtils.print("Veuillez réessayer : ");
            input = readInput();
        }
        return input;
    }

    public static LocalDate readDate() {
        String input = readInput();

        while (!GenericValidator.isDate(input, "yyyy-MM-dd", true) ||
                LocalDate.parse(input).isBefore(LocalDate.now())) {
            PrintUtils.println("Le format de votre date doit correspondre à <aaaa-mm-jj> et votre date ne doit pas être située avant la date d'aujourd'hui.");
            PrintUtils.print("Veuillez réessayer : ");
            input = readInput();
        }

        return LocalDate.parse(input);
    }

    public static String readInputConfirmation() {
        String input = readInput();

        while (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) {
            PrintUtils.print("Vous devez répondre par [y]es ou [n]o, veuillez réessayer : ");
            input = readInput();
        }
        return input.toLowerCase();
    }

    public static int readIntegerInputInRange(int startInclusive, int endExclusive) {
        String input = scanner.nextLine();
        while (!isValidInteger(input) || !isIntegerInRange(Integer.parseInt(input), startInclusive, endExclusive)) {
            input = readInput();
        }
        return Integer.parseInt(input);
    }

    public static int readIntegerInput() {
        String input = scanner.nextLine();
        while (!isValidInteger(input)) {
            input = readInput();
        }
        return Integer.parseInt(input);
    }

    public static double readDoubleInput() {
        String input = scanner.nextLine();
        while (!isValidDouble(input)) {
            input = readInput();
        }
        return Double.parseDouble(input);
    }

    public static double readDoubleInputInRange(double startInclusive, double endExclusive) {
        String input = scanner.nextLine();
        while (!isValidDouble(input) || !isDoubleInRange(Double.parseDouble(input), startInclusive, endExclusive)) {
            input = readInput();
        }
        return Double.parseDouble(input);
    }

    /**
     * Vérifie si l'input entré par l'utilisateur est bien dans la range définie.
     * @param input Entrée de l'utilisateur.
     * @param startInclusive Borne inférieure fermée.
     * @param endExclusive Borne supérieure ouverte.
     * @return True si l'input est valide, false sinon.
     */
    private static boolean isIntegerInRange(int input, int startInclusive, int endExclusive) {
        if (startInclusive <= input && endExclusive > input) {
            return true;
        }
        PrintUtils.print("La valeur est trop petite ou trop grande, veuillez réessayer : ");
        return false;
    }

    /**
     * Vérifie si l'input entré par l'utilisateur est bien dans la range définie.
     * @param input Entrée de l'utilisateur.
     * @param startInclusive Borne inférieure fermée.
     * @param endExclusive Borne supérieure ouverte.
     * @return True si l'input est valide, false sinon.
     */
    private static boolean isDoubleInRange(double input, double startInclusive, double endExclusive) {
        if (startInclusive <= input && endExclusive > input) {
            return true;
        }
        PrintUtils.print("La valeur est trop petite ou trop grande, veuillez réessayer : ");
        return false;
    }

    /**
     * Vérifie si l'input entré par l'utilisateur est un entier valide.
     * @param input Entrée de l'utilisateur.
     * @return True si l'input est valide, false sinon.
     */
    private static boolean isValidInteger(String input) {
        try {
            Integer.parseInt(input);
        } catch (NumberFormatException e) {
            PrintUtils.print("Problème lors de la saisie de la valeur, veuillez réessayer : ");
            return false;
        }
        return true;
    }

    /**
     * Vérifie si l'input entré par l'utilisateur est un nombre réel valide.
     * @param input Entrée de l'utilisateur.
     * @return True si l'input est valide, false sinon.
     */
    private static boolean isValidDouble(String input) {
        try {
            Double.parseDouble(input);
        } catch (NumberFormatException e) {
            PrintUtils.print("Problème lors de la saisie de la valeur, veuillez réessayer : ");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        PrintUtils.print(InputUtils.readDate().toString());
    }
}
