package fr.ul.miage.gl_restaurant.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Dish {

    private static final String TABLE_NAME = "Dishes";

    private Long dishId;

    private String text;

    private String menuType;

    private Double price;

    public Dish() {}

    public Dish(Long dishId, String text, String menuType, Double price) {
        this.dishId = dishId;
        this.text = text;
        this.menuType = menuType;
        this.price = price;
    }

    public Dish(String text, String menuType, Double price) {
        this(null, text, menuType, price);
    }
}
