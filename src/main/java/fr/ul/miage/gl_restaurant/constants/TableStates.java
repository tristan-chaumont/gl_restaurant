package fr.ul.miage.gl_restaurant.constants;

import java.util.Arrays;
import java.util.Optional;

public enum TableStates {

    OCCUPEE("Occupée"),
    LIBRE("Libre"),
    RESERVEE("Réservée"),
    SALE("Sale");

    private final String state;

    TableStates(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return state;
    }

    public static TableStates getState(String state) {
        Optional<TableStates> first = Arrays.stream(TableStates.values()).filter(states -> states.toString().equals(state)).findFirst();
        return first.orElse(null);
    }
}
