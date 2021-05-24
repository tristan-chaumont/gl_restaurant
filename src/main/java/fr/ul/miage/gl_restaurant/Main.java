package fr.ul.miage.gl_restaurant;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.controller.UserController;
import fr.ul.miage.gl_restaurant.jdbc.DbAccess;
import fr.ul.miage.gl_restaurant.utilities.ControllerUtils;
import fr.ul.miage.gl_restaurant.utilities.InputUtils;
import fr.ul.miage.gl_restaurant.utilities.PrintUtils;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

@Slf4j
public class Main {

    public static void main(String[] args) {
        var connection = DbAccess.getInstance();
        log.info("Connection successful");
        ControllerUtils.verifyReservationsForToday();

        var auth = new Authentification();
        var quit = false;
        UserController userController;
        while (!quit) {
            if (!auth.isConnected() && !auth.displayInterface()) {
                quit = true;
            } else {
                userController = ControllerUtils.getController(auth);
                while (auth.isConnected()) {
                    PrintUtils.println(userController.displayActions());
                    PrintUtils.print("Veuillez renseigner le numéro de l'action à effectuer : ");
                    userController.callAction(InputUtils.readIntegerInputInRange(0, userController.getActions().size() + 1));
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
