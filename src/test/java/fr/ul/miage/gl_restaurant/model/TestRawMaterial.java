package fr.ul.miage.gl_restaurant.model;

import fr.ul.miage.gl_restaurant.constants.Units;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class TestRawMaterial {

    @Test
    @DisplayName("Le nom de la matière première est assez court")
    void verifyToStringRMNameHasCorrectLength() {
        RawMaterial rawMaterial = new RawMaterial("TestRM", 50, Units.KG);

        var expected = new TextStringBuilder();
        expected.appendln("-".repeat(20))
                .appendln("| " + StringUtils.center(rawMaterial.getRawMaterialName(), 16) + " |")
                .appendln("-".repeat(20))
                .appendln("Quantité en stock : 50")
                .appendln("Unité : kg");
        assertThat(rawMaterial.toString(), equalTo(expected.toString()));
    }

    @Test
    @DisplayName("Le nom de la matière première est trop long")
    void verifyToStringRMNameIsTooLength() {
        RawMaterial rawMaterial = new RawMaterial("TestRMJustTooTooTooTooLong", 50, Units.KG);

        var expected = new TextStringBuilder();
        expected.appendln("-".repeat(30))
                .appendln("| " + StringUtils.center(rawMaterial.getRawMaterialName(), 26) + " |")
                .appendln("-".repeat(30))
                .appendln("Quantité en stock : 50")
                .appendln("Unité : kg");
        assertThat(rawMaterial.toString(), equalTo(expected.toString()));
    }

    @Test
    @DisplayName("Le nom de la matière première est assez court")
    void verifyToStringRMNameIsBlank() {
        RawMaterial rawMaterial = new RawMaterial("", 50, Units.KG);

        var expected = new TextStringBuilder();
        expected.appendln("-".repeat(20))
                .appendln("| " + StringUtils.center(rawMaterial.getRawMaterialName(), 16) + " |")
                .appendln("-".repeat(20))
                .appendln("Quantité en stock : 50")
                .appendln("Unité : kg");
        assertThat(rawMaterial.toString(), equalTo(expected.toString()));
    }
}
