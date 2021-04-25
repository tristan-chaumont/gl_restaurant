package fr.ul.miage.gl_restaurant.constants;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum Roles {
    SERVEUR("Serveur"),
    DIRECTEUR("Directeur"),
    CUISINIER("Cuisinier"),
    ASSISTANT_SERVICE("Assistant de service"),
    MAITRE_HOTEL("Maître d'hôtel");

    private final String role;

    Roles(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return role;
    }

    public static Roles getRole(String role) {
        Optional<Roles> first = Arrays.stream(Roles.values()).filter(roles -> roles.toString().equals(role)).findFirst();
        return first.orElse(null);
    }
}
