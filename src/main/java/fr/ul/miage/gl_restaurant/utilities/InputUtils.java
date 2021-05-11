package fr.ul.miage.gl_restaurant.utilities;

import java.util.Scanner;
import java.util.regex.Pattern;

public class InputUtils {

    public static final Scanner scanner = new Scanner(System.in);

    public static String readInput() {
        return scanner.nextLine();
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
        while (!isValidInteger(input) &&
                !(startInclusive <= Integer.parseInt(input) && endExclusive > Integer.parseInt(input))) {
            input = scanner.nextLine();
        }
        return Integer.parseInt(input);
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
