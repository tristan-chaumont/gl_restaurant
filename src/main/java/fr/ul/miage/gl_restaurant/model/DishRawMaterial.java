package fr.ul.miage.gl_restaurant.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DishRawMaterial {

    private static final String TABLE_NAME = "Dishes_RawMaterials";

    private Dish dish;

    private RawMaterial rawMaterial;

    private Integer quantity;

    public DishRawMaterial() {}

    public DishRawMaterial(Dish dish, RawMaterial rawMaterial, Integer quantity) {
        this.dish = dish;
        this.rawMaterial = rawMaterial;
        this.quantity = quantity;
    }
}
