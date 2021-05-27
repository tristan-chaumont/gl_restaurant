package fr.ul.miage.gl_restaurant.jdbc;

import fr.ul.miage.gl_restaurant.constants.Environment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestDbAccess {

    Connection connection1, connection2;

    @Test
    @DisplayName("Deux connexions lancées")
    void verifyBothConnectionsAreTheSame() {
        connection1 = DbAccess.getInstance();
        connection2 = DbAccess.getInstance();
        assertThat(connection1, is(connection2));
    }

    @Test
    @DisplayName("Aucune connexion")
    void verifyConnectionIsNull() {
        assertNull(connection1);
        assertNull(connection2);
    }

    @Test
    @DisplayName("La config récupère le bon environnement")
    void verifyConfigGetsEnvironment() {
        DbAccess.parseConfigProperties();
        assertThat(DbAccess.environment, either(is(Environment.TEST)).or(is(Environment.PROD)));
    }

    @AfterEach
    void tearDown() {
        connection1 = null;
        connection2 = null;
    }
}
