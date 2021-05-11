package fr.ul.miage.gl_restaurant.constants;

import java.util.Arrays;
import java.util.Optional;

public enum Units {
    KG("kg"),
    L("L"),
    U("u");

    private final String unit;

    Units(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return unit;
    }

    public static Units getUnit(String unit) {
        Optional<Units> first = Arrays.stream(Units.values()).filter(units -> units.toString().equals(unit)).findFirst();
        return first.orElse(null);
    }
}
