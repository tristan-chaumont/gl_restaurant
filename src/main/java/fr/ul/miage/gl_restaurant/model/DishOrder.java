package fr.ul.miage.gl_restaurant.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DishOrder {

    private Dish dish;

    private Order order;

    private Integer quantity;

    public DishOrder() {}

    public DishOrder(Dish dish, Order order, Integer quantity) {
        this.dish = dish;
        this.order = order;
        this.quantity = quantity;
    }
}
