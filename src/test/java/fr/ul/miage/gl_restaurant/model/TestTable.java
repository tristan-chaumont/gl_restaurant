package fr.ul.miage.gl_restaurant.model;

import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class TestTable {

    @Test
    @DisplayName("toString affiche toutes les infos de la table sans serveur")
    void verifyToStringWithoutServerSucceed() {
        Table table = new Table(200L, 1, TableStates.LIBRE, 4, null);
        var expected = new TextStringBuilder();
        expected.appendln("-".repeat(20))
                .appendln("| " + StringUtils.center("Table n°200", 16) + " |")
                .appendln("-".repeat(20))
                .appendln("Étage : 1")
                .appendln("Serveur : Aucun")
                .appendln("Nombre de places : 4")
                .appendln("Statut : Libre");
        assertThat(table.toString(), equalTo(expected.toString()));
    }

    @Test
    @DisplayName("toString affiche toutes les infos de la table avec un serveur")
    void verifyToStringWithServerSucceed() {
        User user = new User(255L, "chaumontt", "Chaumont", "Tristan", Roles.SERVEUR);
        Table table = new Table(200L, 1, TableStates.LIBRE, 4, user);
        var expected = new TextStringBuilder();
        expected.appendln("-".repeat(20))
                .appendln("| " + StringUtils.center("Table n°200", 16) + " |")
                .appendln("-".repeat(20))
                .appendln("Étage : 1")
                .appendln("Serveur : Tristan Chaumont")
                .appendln("Nombre de places : 4")
                .appendln("Statut : Libre");
        assertThat(table.toString(), equalTo(expected.toString()));
    }
}
