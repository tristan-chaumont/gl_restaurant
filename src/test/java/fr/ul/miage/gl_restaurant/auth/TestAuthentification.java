package fr.ul.miage.gl_restaurant.auth;

import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.model.User;
import fr.ul.miage.gl_restaurant.utilities.InputUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

    @Test
    @DisplayName("L'utilisateur n'est pas connecté")
    void verifyIsConnectedReturnsFalse() {
        assertFalse(auth.isConnected());
    }

    @Test
    @DisplayName("L'utilisateur est connecté")
    void verifyIsConnectedReturnsTrue() {
        auth.signIn("chaumontt");
        assertTrue(auth.isConnected());
    }

    @Test
    @DisplayName("disconnect() déconnecte l'utilisateur")
    void verifyDisconnectSetUserToNull() {
        auth.signIn("chaumontt");
        auth.disconnect();
        assertNull(auth.getUser());
    }

    @Test
    @DisplayName("displayInterface() retourne false car l'utilisateur quitte l'application")
    void verifyDisplayInterfaceReturnsFalse() {
        try (MockedStatic<InputUtils> utilities = Mockito.mockStatic(InputUtils.class)) {
            utilities.when(InputUtils::readInput).thenReturn("!q");
            assertFalse(auth.displayInterface());
        }
    }

    @Test
    @DisplayName("displayInterface() retourne true car l'utilisateur se connecte")
    void verifyDisplayInterfaceReturnsTrue() {
        try (MockedStatic<InputUtils> utilities = Mockito.mockStatic(InputUtils.class)) {
            utilities.when(InputUtils::readInput).thenReturn("chaumontt");
            assertTrue(auth.displayInterface());
        }
    }

    @Test
    @DisplayName("displayInterface() boucle car l'utilisateur rentre un mauvais utilisateur, puis fonctionne")
    void verifyDisplayInterfaceFirstLoopThenReturnsTrue() {
        try (MockedStatic<InputUtils> utilities = Mockito.mockStatic(InputUtils.class)) {
            utilities.when(InputUtils::readInput).thenReturn("unknownUser", "chaumontt");
            assertTrue(auth.displayInterface());
        }
    }

    @AfterEach
    void tearDown() {
        auth = null;
    }
}
