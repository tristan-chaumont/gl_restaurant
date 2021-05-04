package fr.ul.miage.gl_restaurant.auth;

import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestAuthentification {

    Authentification auth;

    @BeforeEach
    void initialize() {
        auth = new Authentification();
    }

    @Test
    @DisplayName("L'authentification fonctionne | L'utilisateur existe")
    void verifySignInReturnsFalse() {
        auth.signIn("chaumontt");
        User user = auth.getUser();
        assertNotNull(user);
        assertThat(user.getFirstName(), equalTo("Tristan"));
        assertThat(user.getLastName(), equalTo("Chaumont"));
        assertThat(user.getRole(), is(Roles.SERVEUR));
    }

    @Test
    @DisplayName("L'authentification échoue | Le login fourni n'existe pas")
    void verifySignInGetsUser() {
        auth.signIn("unknown");
        User user = auth.getUser();
        assertNull(user);
    }

    @Test
    @DisplayName("L'utilisateur se déconnecte")
    void verifyLogOut() {
        auth.signIn("chaumontt");
        assertNotNull(auth.getUser());
        auth.logOut();
        assertNull(auth.getUser());
    }

    @AfterEach
    void tearDown() {
        auth = null;
    }
}
