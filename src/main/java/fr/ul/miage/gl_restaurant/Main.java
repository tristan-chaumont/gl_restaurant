package fr.ul.miage.gl_restaurant;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.jdbc.DbAccess;
import fr.ul.miage.gl_restaurant.utilities.ControllerUtils;
import fr.ul.miage.gl_restaurant.utilities.InputUtils;
import fr.ul.miage.gl_restaurant.utilities.PrintUtils;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

@Slf4j
public class Main {

    public static void main(String[] args) {
        var connection = DbAccess.getInstance(Environment.TEST);
        log.info("Connection successful");

        var auth = new Authentification();
        var quit = false;
        while (!quit) {
            if (auth.isConnected()) {
                try {
                    var userController = ControllerUtils.getController(auth);
                    PrintUtils.print("%s%n", userController.displayActions());
                    PrintUtils.print("Veuillez renseigner le numéro de l'action à effectuer : ");
                    userController.callAction(InputUtils.readIntegerInputInRange(0, userController.getActions().size() + 1));
                } catch (NullPointerException e) {
                    log.error(String.format("Le rôle de l'utilisateur n'existe pas, veuillez réessayer.%n"));
                }
            } else {
                if (!auth.displayInterface()) {
                    quit = true;
                }
            }
        }

        try {
            connection.close();
            log.info("Connection closed");
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }
}
