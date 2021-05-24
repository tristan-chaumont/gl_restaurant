package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.model.Dish;
import fr.ul.miage.gl_restaurant.model.Order;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.*;

@Slf4j
public class OrderRepositoryImpl extends Repository<Order, Long> {

    private static OrderRepositoryImpl instance;

    private static final String ORDERID_COLUMN_NAME = "orderId";

    private static final String FIND_ALL_SQL = "SELECT orderId, orderDate, preparationDate, served, mealId FROM Orders";
    private static final String FIND_CURRENT_ORDERS = "SELECT orderId, orderDate, preparationDate, served, mealId FROM Orders WHERE preparationDate IS NULL";
    private static final String FIND_BY_ID_SQL = "SELECT orderId, orderDate, preparationDate, served, mealId FROM Orders WHERE orderId = ?";
    private static final String FIND_BY_MEAL_SQL = "SELECT orderId, orderDate, preparationDate, served, mealId FROM Orders WHERE mealId = ?";
    private static final String SAVE_SQL = "INSERT INTO Orders(orderDate, preparationDate, served, mealId) VALUES(?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE Orders SET orderDate = ?, preparationDate = ?, served = ?, mealId = ? WHERE orderId = ?";
    private static final String DELETE_SQL = "DELETE FROM Orders WHERE orderId = ?";
    private static final String FIND_BY_DISH_SQL = "SELECT distinct Orders.orderId, orderDate, preparationDate, served, mealId FROM Orders INNER JOIN Dishes_Orders ON Orders.orderID = Dishes_Orders.orderId WHERE dishId = ? AND served = true";

    private static final String FIND_DISHES_BY_ORDER_ID = "SELECT dishId, orderId, quantity FROM Dishes_Orders WHERE orderId = ?";
    private static final String SAVE_DISHES_SQL = "INSERT INTO Dishes_Orders(dishId, orderId, quantity) VALUES(?, ?, ?)";
    private static final String DELETE_DISHES_SQL = "DELETE FROM Dishes_Orders WHERE orderId = ?";

    private OrderRepositoryImpl() {
        super();
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
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Map<Dish, Integer> dishes = findDishesByOrderId(resultSet.getLong(ORDERID_COLUMN_NAME));
                orders.add(new Order(resultSet, dishes));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return orders;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return findByIdHelper(id, FIND_BY_ID_SQL);
    }


    public List<Order> findByDish(Long dishId){
        List<Order> orders = new ArrayList<>();
        try (var preparedStatement = connection.prepareStatement(FIND_BY_DISH_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setLong(1, dishId);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.first()) {
                    Map<Dish, Integer> dishes = findDishesByOrderId(resultSet.getLong(ORDERID_COLUMN_NAME));
                    orders.add(new Order(resultSet, dishes));
                }
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return orders;
    }

    public Optional<Order> findByMeal(Long mealId) {
        return findByIdHelper(mealId, FIND_BY_MEAL_SQL);
    }

    private Optional<Order> findByIdHelper(Long id, String query) {
        Optional<Order> order = Optional.empty();
        try (var preparedStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setLong(1, id);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.first()) {
                    Map<Dish, Integer> dishes = findDishesByOrderId(resultSet.getLong(ORDERID_COLUMN_NAME));
                    order = Optional.of(new Order(resultSet, dishes));
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return order;
    }

    @Override
    public Order save(Order object) {
        if (object != null && object.getOrderId() == null) {
            try (var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setTimestamp(1, object.getOrderDate());
                preparedStatement.setTimestamp(2, object.getPreparationDate());
                preparedStatement.setBoolean(3, object.isServed());
                preparedStatement.setLong(4, object.getMeal().getMealId());
                preparedStatement.executeUpdate();
                generateKey(object, preparedStatement);
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        return object;
    }

    private void generateKey(Order object, PreparedStatement preparedStatement) {
        try (var resultSet = preparedStatement.getGeneratedKeys()) {
            if (resultSet.next()) {
                object.setOrderId(resultSet.getLong(ORDERID_COLUMN_NAME));
                saveDishesByOrderId(object.getOrderId(), object.getDishes());
            }
        } catch (SQLException s) {
            log.error(s.getMessage());
        }
    }

    @Override
    public Order update(Order object) {
        if (object != null && object.getOrderId() != null) {
            try (var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
                preparedStatement.setTimestamp(1, object.getOrderDate());
                preparedStatement.setTimestamp(2, object.getPreparationDate());
                preparedStatement.setBoolean(3, object.isServed());
                preparedStatement.setLong(4, object.getMeal().getMealId());
                preparedStatement.setLong(5, object.getOrderId());
                updateDishesByOrderId(object.getOrderId(), object.getDishes());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        return object;
    }

    public void delete(Long id) {
        if (id != null) {
            try (var preparedStatement = connection.prepareStatement(DELETE_SQL)) {
                deleteDishesByOrderId(id);
                preparedStatement.setLong(1, id);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
    }

    /* DISHES */

    public Map<Dish, Integer> findDishesByOrderId(Long id) {
        Map<Dish, Integer> dishes = new HashMap<>();
        if (id != null) {
            try (var preparedStatement = connection.prepareStatement(FIND_DISHES_BY_ORDER_ID, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                preparedStatement.setLong(1, id);
                try (var resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Optional<Dish> dish = DishRepositoryImpl.getInstance().findById(resultSet.getLong("dishId"));
                        if (dish.isPresent()) {
                            dishes.put(dish.get(), resultSet.getInt("quantity"));
                        }
                    }
                }
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        return dishes;
    }

    private void saveDishesByOrderId(Long orderId, Map<Dish, Integer> dishes) {
        if (orderId != null && dishes != null) {
            dishes.forEach((dish, quantity) -> {
                if (dish.getDishId() != null) {
                    try (var preparedStatement = connection.prepareStatement(SAVE_DISHES_SQL)) {
                        preparedStatement.setLong(1, dish.getDishId());
                        preparedStatement.setLong(2, orderId);
                        preparedStatement.setInt(3, quantity);
                        preparedStatement.executeUpdate();
                    } catch (SQLException e) {
                        log.error(e.getMessage());
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
            try (var preparedStatement = connection.prepareStatement(DELETE_DISHES_SQL)) {
                preparedStatement.setLong(1, orderId);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
    }

    public static OrderRepositoryImpl getInstance() {
        if (instance == null) {
            instance = new OrderRepositoryImpl();
        }
        return instance;
    }
}
