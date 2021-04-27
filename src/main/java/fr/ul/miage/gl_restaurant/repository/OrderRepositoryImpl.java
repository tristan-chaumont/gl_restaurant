package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.model.Meal;
import fr.ul.miage.gl_restaurant.model.Order;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class OrderRepositoryImpl extends Repository<Order, Long> {

    private static final String FIND_ALL_SQL = "SELECT orderId, orderDate, preparationDate, mealId FROM Orders";
    private static final String FIND_BY_ID_SQL = "SELECT orderId, orderDate, preparationDate, mealId FROM Orders WHERE orderId = ?";
    private static final String SAVE_SQL = "INSERT INTO Orders(orderDate, preparationDate, mealId) VALUES(?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE Orders SET orderDate = ?, preparationDate = ?, mealId = ? WHERE orderId = ?";
    private static final String DELETE_SQL = "DELETE FROM Orders WHERE orderId = ?";

    protected OrderRepositoryImpl(Environment environment) {
        super(environment);
    }

    @Override
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL_SQL)) {
            while (resultSet.next()) {
                Long orderId = resultSet.getLong("orderId");
                Timestamp orderDate = resultSet.getTimestamp("orderDate");
                Timestamp preparationDate = resultSet.getTimestamp("preparationDate");
                Optional<Meal> meal = new MealRepositoryImpl(Environment.TEST).findById(resultSet.getLong("mealId"));
                meal.ifPresent(value -> orders.add(new Order(orderId, orderDate, preparationDate, value)));
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
                    if (meal.isPresent()) {
                        order = Optional.of(new Order(orderId, orderDate, preparationDate, meal.get()));
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
                int numRowsAffected = preparedStatement.executeUpdate();
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        object.setOrderId(resultSet.getLong(1));
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
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    @Override
    public void delete(Long id) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_SQL)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
