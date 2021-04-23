package fr.ul.miage.gl_restaurant.jdbc;

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
    private static String url, username, password;

    private DbAccess() {}

    private static void parseProperties() {
        String propertiesFileName = "db.properties";
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(propertiesFileName)) {
            properties.load(inputStream);

            url = properties.getProperty("url");
            username = properties.getProperty("username");
            password = properties.getProperty("password");
        } catch (FileNotFoundException e) {
            log.error(String.format("Property file '%s' not found in the classpath.", propertiesFileName));
        } catch (IOException e) {
            log.error(String.format("Could not open file '%s'", propertiesFileName));
            log.error("Exception: " + e);
        }
    }

    public static Connection getInstance() {
        if (connection == null) {
            parseProperties();
            try {
                connection = DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                log.error("Exception: " + e.getMessage());
            }
        }
        return connection;
    }
}
