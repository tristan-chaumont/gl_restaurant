package fr.ul.miage.gl_restaurant.jdbc;

import fr.ul.miage.gl_restaurant.constants.Environment;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Getter
@Slf4j
public class DbAccess {

    private static Connection connection;
    private static String url;
    private static String username;
    private static String password;

    private DbAccess() {}

    private static void parseProperties(String propertiesFileName) {
        var properties = new Properties();
        try (InputStream inputStream = new FileInputStream(propertiesFileName)) {
            properties.load(inputStream);

            url = properties.getProperty("url");
            username = properties.getProperty("username");
            password = properties.getProperty("password");
        } catch (FileNotFoundException e) {
            log.error(String.format("Property file '%s' not found in the classpath.", propertiesFileName));
        } catch (IOException e) {
            log.error(String.format("Could not open file '%s'", propertiesFileName));
            log.error(e.getMessage());
        }
    }

    public static Connection getInstance(Environment environment) {
        if (connection == null) {
            parseProperties(environment.equals(Environment.PROD) ?
                    "db.properties" : "db.test.properties");
            try {
                connection = DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                log.error(e.getMessage());
                System.exit(1);
            }
        }
        return connection;
    }
}
