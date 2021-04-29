package fr.ul.miage.gl_restaurant.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@Data
public class Order implements Comparable<Order> {

    private static final String TABLE_NAME = "Orders";

    private Long orderId;

    private Timestamp orderDate;

    private Timestamp preparationDate;

    private Meal meal;

    public Order() {}

    public Order(Long orderId, Timestamp orderDate, Timestamp preparationDate, Meal meal) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.preparationDate = preparationDate;
        this.meal = meal;
    }

    public Order(Timestamp orderDate, Timestamp preparationDate, Meal meal) {
        this(null, orderDate, preparationDate, meal);
    }

    /**
     * Compare les dates de commande pour créer la file d'attente.
     * @param order
     *      Commande à comparer avec la commande actuelle.
     * @return
     *      -1 si la commande actuelle est avant, 0 si elles sont équivalentes, 1 sinon.
     */
    @Override
    public int compareTo(Order order) {
        return this.orderDate.compareTo(order.getOrderDate());
    }
}
