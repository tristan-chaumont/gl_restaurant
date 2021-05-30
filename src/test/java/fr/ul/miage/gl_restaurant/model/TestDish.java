package fr.ul.miage.gl_restaurant.model;

import fr.ul.miage.gl_restaurant.constants.MenuTypes;
import fr.ul.miage.gl_restaurant.constants.Units;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class TestDish {

    @Test
    @DisplayName("Le nom du plat est assez court et n'a pas d'ingrédients")
    void verifyToStringDishNameHasCorrectLengthAndHasNoIngredients() {
        Dish dish = new Dish("Test", "Test", MenuTypes.ADULTES, 5.0, true);
        var expected = new TextStringBuilder();
        expected.appendln("-".repeat(20))
                .appendln("| " + StringUtils.center(dish.getDishName(), 16) + " |")
                .appendln("-".repeat(20))
                .appendln("Catégorie : Test")
                .appendln("Prix : 5.00€")
                .appendln("Menu Adultes");
        assertThat(dish.toString(), equalTo(expected.toString()));
    }

    String generateStringBuilder(Dish dish, String content) {
        var expected = new TextStringBuilder();
        expected.appendln("-".repeat(27))
                .appendln("| " + StringUtils.center(dish.getDishName(), 23) + " |")
                .appendln("-".repeat(27))
                .appendln("Catégorie : Test")
                .appendln("Prix : 5.00€")
                .appendln("Menu Adultes")
                .appendln("Ingrédients : ")
                .appendln(content);
        return expected.toString();
    }

    @Test
    @DisplayName("Le nom du plat est trop long et a des ingrédients")
    void verifyToStringDishNameIsTooLengthAnsHasIngredients() {
        Dish dish = new Dish("TestReallyTooTooTooLong", "Test", MenuTypes.ADULTES, 5.0, true);
        Map<RawMaterial, Integer> rawMaterials = new HashMap<>() {{
           put(new RawMaterial("TestRM", 50, Units.KG), 2);
           put(new RawMaterial("TestRM2", 20, Units.U), 5);
        }};
        dish.setRawMaterials(rawMaterials);
        var expected = generateStringBuilder(dish,
                String.format(" - TestRM (x2)%n - TestRM2 (x5)"));

        var expected2 = generateStringBuilder(dish,
                String.format(" - TestRM2 (x5)%n - TestRM (x2)"));
        assertThat(dish.toString(), either(equalTo(expected)).or(equalTo(expected2)));
    }

    @Test
    @DisplayName("Le nom du plat est une chaine vide")
    void verifyToStringDishNameIsBlank() {
        Dish dish = new Dish("", "Test", MenuTypes.ADULTES, 5.0, true);
        var expected = new TextStringBuilder();
        expected.appendln("-".repeat(20))
                .appendln("| " + StringUtils.center(dish.getDishName(), 16) + " |")
                .appendln("-".repeat(20))
                .appendln("Catégorie : Test")
                .appendln("Prix : 5.00€")
                .appendln("Menu Adultes");
        assertThat(dish.toString(), equalTo(expected.toString()));
    }
}
