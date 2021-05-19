package fr.ul.miage.gl_restaurant.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestMenuTypes {

    @Test
    @DisplayName("getMenuType() récupère les bons types de menu")
    void verifyGetMenuTypeReturnsRightMenuTypes() {
        MenuTypes menuAdultes = MenuTypes.getMenuType("Adultes");
        MenuTypes menuEnfants = MenuTypes.getMenuType("Enfants");
        assertThat(menuAdultes, is(MenuTypes.ADULTES));
        assertThat(menuEnfants, is(MenuTypes.ENFANTS));
    }

    @Test
    @DisplayName("getMenuType() renvoie null car le type de menu n'existe pas")
    void verifyGetMenuTypeReturnsNull() {
        MenuTypes menu = MenuTypes.getMenuType("nothing");
        assertNull(menu);
    }

    @Test
    @DisplayName("menuType contient la bonne chaîne")
    void verifyMenuTypeFieldContainsRightString() {
        MenuTypes menuTypes = MenuTypes.ADULTES;
        assertThat(menuTypes.getMenuType(), equalTo("Adultes"));
    }
}
