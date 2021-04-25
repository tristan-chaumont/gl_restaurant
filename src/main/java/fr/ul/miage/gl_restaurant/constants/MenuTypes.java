package fr.ul.miage.gl_restaurant.constants;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum MenuTypes {
    ADULTES("Adultes"),
    ENFANTS("Enfants");

    private final String menuType;

    MenuTypes(String menuType) {
        this.menuType = menuType;
    }

    @Override
    public String toString() {
        return menuType;
    }

    public static MenuTypes getMenuType(String menuType) {
        Optional<MenuTypes> first = Arrays.stream(MenuTypes.values())
                .filter(menuTypes -> menuTypes.toString().equals(menuType)).findFirst();
        return first.orElse(null);
    }
}
