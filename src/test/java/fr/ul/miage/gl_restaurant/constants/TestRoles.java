package fr.ul.miage.gl_restaurant.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestRoles {

    @Test
    @DisplayName("getRole() récupère les bons rôles")
    void verifyGetMenuTypeReturnsRightMenuTypes() {
        Roles serveur = Roles.getRole("Serveur");
        Roles directeur = Roles.getRole("Directeur");
        Roles cuisinier = Roles.getRole("Cuisinier");
        Roles assistant = Roles.getRole("Assistant de service");
        Roles maitre = Roles.getRole("Maître d'hôtel");
        assertThat(serveur, is(Roles.SERVEUR));
        assertThat(directeur, is(Roles.DIRECTEUR));
        assertThat(cuisinier, is(Roles.CUISINIER));
        assertThat(assistant, is(Roles.ASSISTANT_SERVICE));
        assertThat(maitre, is(Roles.MAITRE_HOTEL));
    }

    @Test
    @DisplayName("getRole() renvoie null car le rôle n'existe pas")
    void verifyGetMenuTypeReturnsNull() {
        Roles role = Roles.getRole("nothing");
        assertNull(role);
    }

    @Test
    @DisplayName("role contient la bonne chaîne")
    void verifyRoleFieldContainsRightString() {
        Roles roles = Roles.CUISINIER;
        assertThat(roles.getRole(), equalTo("Cuisinier"));
    }
}
