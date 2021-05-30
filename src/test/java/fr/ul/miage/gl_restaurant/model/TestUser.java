package fr.ul.miage.gl_restaurant.model;

import fr.ul.miage.gl_restaurant.constants.Roles;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class TestUser {

    @Test
    @DisplayName("toString affiche toutes les info de l'utilisateur")
    void verifyToStringUserSucceed() {
        User user = new User(255L, "chaumontt", "Chaumont", "Tristan", Roles.SERVEUR);
        var expected = new TextStringBuilder();
        expected.appendln("-".repeat(25))
                .appendln("| " + StringUtils.center("Utilisateur n°255", 21) + " |")
                .appendln("-".repeat(25))
                .appendln("Login : chaumontt")
                .appendln("Nom : Chaumont")
                .appendln("Prénom : Tristan")
                .appendln("Rôle : Serveur");

        assertThat(user.toString(), equalTo(expected.toString()));
    }
}
