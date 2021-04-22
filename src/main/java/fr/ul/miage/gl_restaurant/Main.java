package fr.ul.miage.gl_restaurant;

import fr.ul.miage.gl_restaurant.jdbc.DbAccess;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

@Slf4j
public class Main {

    public static void main(String[] args) {
        try {
            DbAccess db = DbAccess.getInstance();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        log.info("Connection successful");
    }
}
