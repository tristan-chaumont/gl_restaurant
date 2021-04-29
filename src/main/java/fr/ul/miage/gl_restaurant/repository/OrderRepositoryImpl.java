package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.model.Dish;
import fr.ul.miage.gl_restaurant.model.Meal;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.model.RawMaterial;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.*;

@Slf4j
public class OrderRepositoryImpl extends Repository<Order, Long> {

    private static final String FIND_ALL_SQL = "SELECT orderId, orderDate, preparationDate, mealId FROM Orders";
    private static final String FIND_CURRENT_ORDERS = "SELECT orderId, orderDate, preparationDate, mealId FROM Orders WHERE preparationDate IS NULL";
    private static final String FIND_BY_ID_SQL = "SELECT orderId, orderDate, preparationDate, mealId FROM Orders WHERE orderId = ?";
    private static final String SAVE_SQL = "INSERT INTO Orders(orderDate, preparationDate, mealId) VALUES(?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE Orders SET orderDate = ?, preparationDate = ?, mealId = ? WHERE orderId = ?";
    private static final String DELETE_SQL = "DELETE FROM Orders WHERE orderId = ?";

    private static final String FIND_DISHES_BY_ORDER_ID = "SELECT dishId, orderId, quantity FROM Dishes_Orders WHERE orderId = ?";
    private static final String SAVE_DISHES_SQL = "INSERT INTO Dishes_Orders(dishId, orderId, quantity) VALUES(?, ?, ?)";
    private static final String DELETE_DISHES_SQL = "DELETE FROM Dishes_Orders WHERE orderId = ?";

    public OrderRepositoryImpl(Environment environment) {
        super(environment);
    }

    @Override
    public List<Order> findAll() {
        return findAllHelper(FIND_ALL_SQL);
    }

    public List<Order> findCurrentOrders() {
        return findAllHelper(FIND_CURRENT_ORDERS);
    }

    private List<Order> findAllHelper(String query) {
        List<Order> orders = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Long orderId = resultSet.getLong("orderId");
                Timestamp orderDate = resultSet.getTimestamp("orderDate");
                Timestamp preparationDate = resultSet.getTimestamp("preparationDate");
                Optional<Meal> meal = new MealRepositoryImpl(Environment.TEST).findById(resultSet.getLong("mealId"));
                Map<Dish, Integer> dishes = findDishesByOrderId(orderId);
                meal.ifPresent(value -> orders.add(new Order(orderId, orderDate, preparationDate, value, dishes)));
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return orders;
    }

    @Override
    public Optional<Order> findById(Long id) {
        Optional<Order> order = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.first()) {
                    Long orderId = resultSet.getLong("orderId");
                    Timestamp orderDate = resultSet.getTimestamp("orderDate");
                    Timestamp preparationDate = resultSet.getTimestamp("preparationDate");
                    Optional<Meal> meal = new MealRepositoryImpl(Environment.TEST).findById(resultSet.getLong("mealId"));
                    Map<Dish, Integer> dishes = findDishesByOrderId(orderId);
                    if (meal.isPresent()) {
                        order = Optional.of(new Order(orderId, orderDate, preparationDate, meal.get(), dishes));
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return order;
    }

    @Override
    public Order save(Order object) {
        if (object != null && object.getOrderId() == null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setTimestamp(1, object.getOrderDate());
                preparedStatement.setTimestamp(2, object.getPreparationDate());
                preparedStatement.setLong(3, object.getMeal().getMealId());
                preparedStatement.executeUpdate();
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        object.setOrderId(resultSet.getLong("orderId"));
                        saveDishesByOrderId(object.getOrderId(), object.getDishes());
                    }
                } catch (SQLException s) {
                    s.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    @Override
    public Order update(Order object) {
        if (object != null && object.getOrderId() != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
                preparedStatement.setTimestamp(1, object.getOrderDate());
                preparedStatement.setTimestamp(2, object.getPreparationDate());
                preparedStatement.setLong(3, object.getMeal().getMealId());
                preparedStatement.setLong(4, object.getOrderId());
                updateDishesByOrderId(object.getOrderId(), object.getDishes());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    @Override
    public void delete(Long id) {
        if (id != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_SQL)) {
                deleteDishesByOrderId(id);
                preparedStatement.setLong(1, id);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /* DISHES */

    public Map<Dish, Integer> findDishesByOrderId(Long id) {
        Map<Dish, Integer> dishes = new HashMap<>();
        if (id != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_DISHES_BY_ORDER_ID, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                preparedStatement.setLong(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Optional<Dish> dish = new DishRepositoryImpl(Environment.TEST).findById(resultSet.getLong("dishId"));
                        if (dish.isPresent()) {
                            dishes.put(dish.get(), resultSet.getInt("quantity"));
                        }
                    }
                }
            } catch (SQLException e) {
                log.error("Exception: " + e.getMessage());
            }
        }
        return dishes;
    }

    private void saveDishesByOrderId(Long orderId, Map<Dish, Integer> dishes) {
        if (orderId != null && dishes != null) {
            dishes.forEach((dish, quantity) -> {
                if (dish.getDishId() != null) {
                    try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_DISHES_SQL)) {
                        preparedStatement.setLong(1, dish.getDishId());
                        preparedStatement.setLong(2, orderId);
                        preparedStatement.setInt(3, quantity);
                        preparedStatement.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void updateDishesByOrderId(Long orderId, Map<Dish, Integer> dishes) {
        deleteDishesByOrderId(orderId);
        saveDishesByOrderId(orderId, dishes);
    }

    public void deleteDishesByOrderId(Long orderId) {
        if (orderId != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_DISHES_SQL)) {
                preparedStatement.setLong(1, orderId);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
