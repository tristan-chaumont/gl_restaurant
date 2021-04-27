package fr.ul.miage.gl_restaurant;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.jdbc.DbAccess;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

@Slf4j
public class Main {

    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        Connection connection = DbAccess.getInstance(Environment.TEST);
        log.info("Connection successful");

        Authentification auth = new Authentification();
        boolean quit = false;
        while (!quit) {
            if (auth.isConnected()) {
                System.out.println("Pour vous déconnecter, tapez : !d");
                String choice = sc.next();
                if (choice.equals("!d")) {
                    auth.disconnect();
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
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
