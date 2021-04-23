package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.model.Dish;
import fr.ul.miage.gl_restaurant.model.User;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class DishRepositoryImpl implements Repository<Dish, Long> {

    private static final String FIND_ALL_SQL = "SELECT dishId, category, menuType, price FROM Dishes";
    private static final String FIND_BY_ID_SQL = "SELECT dishId, category, menuType, price FROM Users WHERE dishId = ?";
    private static final String SAVE_SQL = "INSERT INTO Users(category, menuType, price) VALUES(?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE Dishes SET category = ?, menuType = ?, price = ? WHERE dishId = ?";
    private static final String DELETE_SQL = "DELETE FROM Dishes WHERE dishId = ?";

    @Override
    public List<Dish> findAll() {
        List<Dish> dishes = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL_SQL)) {
            while (resultSet.next()) {
                Long dishId = resultSet.getLong("dishId");
                String category = resultSet.getString("login");
                String menuType = resultSet.getString("lastName");
                String price = resultSet.getString("firstName");
                dishes.add(new Dish(dishId, category, menuType, price));
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return dishes;
    }

    @Override
    public Optional<Dish> findById(Long id) {
        Optional<Dish> dish = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.first()) {
                    Long dishId = resultSet.getLong("dishId");
                    String category = resultSet.getString("login");
                    String menuType = resultSet.getString("lastName");
                    String price = resultSet.getString("firstName");
                    dish = Optional.of(new Dish(dishId, category, menuType, price));
                }
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return dish;
    }

    @Override
    public Dish save(Dish object) {
        if (object != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, object.getCategory());
                preparedStatement.setString(2, object.getMenuType());
                preparedStatement.setDouble(3, object.getPrice());
                int numRowsAffected = preparedStatement.executeUpdate();
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        object.setDishId(resultSet.getLong(1));
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
    public Dish update(Dish object) {
        if (object != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
                preparedStatement.setString(1, object.getCategory());
                preparedStatement.setString(2, object.getMenuType());
                preparedStatement.setDouble(3, object.getPrice());
                preparedStatement.setLong(5, object.getDishId());
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