package fr.ul.miage.gl_restaurant.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Dish {

    private Long dishId;

    private String text;

    private String menuType;

    private Double precision;

    public Dish() {}

    public Dish(Long dishId, String text, String menuType, Double precision) {
        this.dishId = dishId;
        this.text = text;
        this.menuType = menuType;
        this.precision = precision;
    }

    public Dish(String text, String menuType, Double precision) {
        this(null, text, menuType, precision);
    }
}
