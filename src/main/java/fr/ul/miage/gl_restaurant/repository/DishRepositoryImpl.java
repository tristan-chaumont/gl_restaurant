package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.model.Dish;
import fr.ul.miage.gl_restaurant.model.User;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class DishRepositoryImpl extends Repository<Dish, Long> {

    private static final String FIND_ALL_SQL = "SELECT dishId, wording, category, menuType, price FROM Dishes";
    private static final String FIND_BY_ID_SQL = "SELECT dishId, wording, category, menuType, price FROM Dishes WHERE dishId = ?";
    private static final String FIND_BY_WORDING_SQL = "SELECT dishId, wording, category, menuType, price FROM Dishes WHERE wording = ?";
    private static final String FIND_BY_CATEGORY_SQL = "SELECT dishId, wording, category, menuType, price FROM Dishes WHERE category = ?";
    private static final String SAVE_SQL = "INSERT INTO Dishes(wording, category, menuType, price) VALUES(?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE Dishes SET wording = ?, category = ?, menuType = ?, price = ? WHERE dishId = ?";
    private static final String DELETE_SQL = "DELETE FROM Dishes WHERE dishId = ?";

    protected DishRepositoryImpl(Environment environment) {
        super(environment);
    }

    @Override
    public List<Dish> findAll() {
        List<Dish> dishes = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL_SQL)) {
            while (resultSet.next()) {
                dishes.add(new Dish(resultSet));
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return dishes;
    }

    @Override
    public Optional<Dish> findById(Long id) {
        Optional<Dish> dish = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.first()) {
                    dish = Optional.of(new Dish(resultSet));
                }
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return dish;
    }

    public Optional<Dish> findByWording(String wording) {
        Optional<Dish> dish = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_WORDING_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setString(1, wording);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.first()) {
                    dish = Optional.of(new Dish(resultSet));
                }
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return dish;
    }

    public List<Dish> findByCategory(String category) {
        List<Dish> dishes = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_CATEGORY_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setString(1, category);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    dishes.add(new Dish(resultSet));
                }
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return dishes;
    }

    @Override
    public Dish save(Dish object) {
        if (object != null && object.getDishId() == null) {
            Optional<Dish> dish = findByWording(object.getWording());
            if(dish.isEmpty()){
                try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setString(1, object.getWording());
                    preparedStatement.setString(2, object.getCategory());
                    preparedStatement.setString(3, object.getMenuType().toString());
                    preparedStatement.setDouble(4, object.getPrice());
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
        }
        return object;
    }

    @Override
    public Dish update(Dish object) {
        if (object != null && object.getDishId() != null) {
            Optional<Dish> dish = findByWording(object.getWording());
            if (dish.isEmpty()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
                    preparedStatement.setString(1, object.getWording());
                    preparedStatement.setString(2, object.getCategory());
                    preparedStatement.setString(3, object.getMenuType().toString());
                    preparedStatement.setDouble(4, object.getPrice());
                    preparedStatement.setLong(5, object.getDishId());
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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
