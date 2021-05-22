package fr.ul.miage.gl_restaurant.jdbc;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.utilities.PrintUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.TextStringBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton
 */
@Getter
@Slf4j
public class DbAccess {

    private static Connection connection;
    private static String url;
    private static String username;
    private static String password;
    private static Environment environment;

    private DbAccess() {}

    /**
     * Récupère les credentials de connexion à la DB.
     * @param propertiesFileName Fichier des properties (soit 'db.properties', soit 'db.test.properties').
     */
    protected static void parseProperties(String propertiesFileName) {
        var properties = new Properties();
        try (InputStream inputStream = new FileInputStream(propertiesFileName)) {
            properties.load(inputStream);

            url = properties.getProperty("url");
            username = properties.getProperty("username");
            password = properties.getProperty("password");
        } catch (FileNotFoundException e) {
            log.error(String.format("Property file '%s' not found in the classpath.", propertiesFileName));
            System.exit(1);
        } catch (IOException e) {
            log.error(String.format("Could not open file '%s'", propertiesFileName));
            log.error(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Récupère la bonne DB à utiliser au lancement de l'application.
     */
    protected static void parseConfigProperties() {
        var properties = new Properties();
        try (InputStream inputStream = new FileInputStream("config.properties")) {
            properties.load(inputStream);

            String env = properties.getProperty("environment");
            if (env.equalsIgnoreCase("prod")) {
                environment = Environment.PROD;
            } else if (env.equalsIgnoreCase("test")) {
                environment = Environment.TEST;
            } else {
                throw new IllegalArgumentException();
            }
        } catch (FileNotFoundException e) {
            log.error("Property file 'config.properties' not found in the classpath.");
            System.exit(1);
        } catch (IOException e) {
            log.error("Could not open file 'config.properties'");
            log.error(e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            var error = new TextStringBuilder();
            error.appendln("Erreur lors de la lecture du fichier 'config.properties'.")
                    .appendln("Vérifiez que son contenu correspond bien à :")
                    .appendln("\tenvironment=test")
                    .appendln("\tOU")
                    .appendln("\tenvironment=prod")
                    .appendln("en fonction de la base de données que vous souhaitez utiliser.");
            PrintUtils.print(error.toString());
            System.exit(1);
        }
    }

    public static Connection getInstance() {
        if (connection == null) {
            parseConfigProperties();
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
