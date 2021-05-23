package fr.ul.miage.gl_restaurant.model;

import fr.ul.miage.gl_restaurant.repository.MealRepositoryImpl;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@ToString
@Data
public class Order implements Comparable<Order> {

    private Long orderId;

    private Timestamp orderDate;

    private Timestamp preparationDate;

    private boolean served;

    private Meal meal;

    private Map<Dish, Integer> dishes;

    public Order() {
        this.dishes = new HashMap<>();
    }

    public Order(Long orderId, Meal meal) {
        this.orderId = orderId;
        this.orderDate = Timestamp.from(Instant.now());
        this.preparationDate = null;
        this.served = false;
        this.meal = meal;
    }

    public Order(Long orderId, Timestamp orderDate, Timestamp preparationDate, Meal meal) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.preparationDate = preparationDate;
        this.served = false;
        this.meal = meal;
        this.dishes = new HashMap<>();
    }

    public Order(Long orderId, Timestamp orderDate, Timestamp preparationDate, Meal meal, Map<Dish, Integer> dishes) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.preparationDate = preparationDate;
        this.served = false;
        this.meal = meal;
        this.dishes = dishes;
    }

    public Order(Long orderId, Timestamp orderDate, Timestamp preparationDate, boolean served, Meal meal, Map<Dish, Integer> dishes) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.preparationDate = preparationDate;
        this.served = served;
        this.meal = meal;
        this.dishes = dishes;
    }

    public Order(Timestamp orderDate, Timestamp preparationDate, Meal meal) {
        this(null, orderDate, preparationDate, meal);
    }

    public Order(Timestamp orderDate, boolean served, Meal meal) {
        this(null, orderDate, null, served, meal, new HashMap<>());
    }

    public Order(Timestamp orderDate, Timestamp preparationDate, Meal meal, Map<Dish, Integer> dishes) {
        this(null, orderDate, preparationDate, meal, dishes);
    }

    public Order(Timestamp orderDate,  Meal meal, Map<Dish, Integer> dishes) {
        this(null, orderDate, null, meal, dishes);
    }

    public Order(ResultSet resultSet, Map<Dish, Integer> dishes) throws SQLException {
        this.orderId = resultSet.getLong("orderId");
        this.orderDate = resultSet.getTimestamp("orderDate");
        this.preparationDate = resultSet.getTimestamp("preparationDate");
        this.served = resultSet.getBoolean("served");
        Optional<Meal> optionalMeal = MealRepositoryImpl.getInstance().findById(resultSet.getLong("mealId"));
        this.meal = optionalMeal.orElse(null);
        this.dishes = dishes;
    }

    public void addDish(Dish dish, Integer quantity) {
        dishes.put(dish, quantity);
    }

    public void clearDishes() {
        dishes.clear();
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
