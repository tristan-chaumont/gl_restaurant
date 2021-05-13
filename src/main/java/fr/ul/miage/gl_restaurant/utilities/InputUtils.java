package fr.ul.miage.gl_restaurant.utilities;

import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class InputUtils {

    public static final Scanner scanner = new Scanner(System.in);

    public static String readInput() {
        return scanner.nextLine();
    }

    public static String readInputInArray(List<String> values) {
        String input = readInput();
        while (!values.contains(input)) {
            System.out.println("Votre saisie doit correspondre à l'un des éléments suivants : " + String.join(", ", values));
            System.out.print("Veuillez réessayer : ");
            input = readInput();
        }
        return input;
    }

    /**
     * Vérifie si l'input entré par l'utilisateur est conforme au pattern.
     * @param input Entrée de l'utilisateur.
     * @param pattern Pattern à utiliser.
     * @return True si l'input est valide, false sinon.
     */
    private boolean isValidPattern(String input, String pattern) {
        return Pattern.matches(pattern, input);
    }

    public static int readIntegerInputInRange(int startInclusive, int endExclusive) {
        String input = scanner.nextLine();
        while (!isValidInteger(input) || !isIntegerInRange(Integer.parseInt(input), startInclusive, endExclusive)) {
            input = readInput();
        }
        return Integer.parseInt(input);
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
        System.out.print("La valeur est trop petite ou trop grande, veuillez réessayer : ");
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
            System.out.print("Problème lors de la saisie de la valeur, veuillez réessayer : ");
            return false;
        }
        return true;
    }
}
