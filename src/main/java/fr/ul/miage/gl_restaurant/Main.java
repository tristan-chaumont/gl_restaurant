package fr.ul.miage.gl_restaurant;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.jdbc.DbAccess;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;

@Slf4j
public class Main {

    public static void main(String[] args) {
        Connection connection = DbAccess.getInstance(Environment.TEST);
        log.info("Connection successful");
    }
}
