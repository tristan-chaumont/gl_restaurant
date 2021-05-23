package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.model.Bill;
import fr.ul.miage.gl_restaurant.model.Meal;
import fr.ul.miage.gl_restaurant.model.Table;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class MealRepositoryImpl extends Repository<Meal, Long> {
    
    private static MealRepositoryImpl instance;

    private static final String FIND_ALL_SQL = "SELECT mealId, customersNb, startDate, mealDuration, tableId, billId FROM Meals";
    private static final String FIND_BY_ID_SQL = "SELECT mealId, customersNb, startDate, mealDuration, tableId, billId FROM Meals WHERE mealId = ?";
    private static final String SAVE_SQL = "INSERT INTO Meals(customersNb, startDate, mealDuration, tableId, billId) VALUES(?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE Meals SET customersNb = ?, startDate = ?, mealDuration = ?, tableId = ?, billId = ? WHERE mealId = ?";
    private static final String DELETE_SQL = "DELETE FROM Meals WHERE mealId = ?";

    private MealRepositoryImpl() {
        super();
    }

    @Override
    public List<Meal> findAll() {
        List<Meal> meals = new ArrayList<>();
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery(FIND_ALL_SQL)) {
            while (resultSet.next()) {
                Long mealId = resultSet.getLong("mealId");
                Integer customersNb = resultSet.getInt("customersNb");
                var startDate = resultSet.getTimestamp("startDate");
                Long mealDuration = resultSet.getLong("mealDuration");
                Optional<Table> table = TableRepositoryImpl.getInstance().findById(resultSet.getLong("tableId"));
                Optional<Bill> bill = BillRepositoryImpl.getInstance().findById(resultSet.getLong("billId"));
                meals.add(new Meal(mealId, customersNb, startDate, mealDuration, table.orElse(null), bill.orElse(null)));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return meals;
    }

    @Override
    public Optional<Meal> findById(Long id) {
        Optional<Meal> meal = Optional.empty();
        try (var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setLong(1, id);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.first()) {
                    Long mealId = resultSet.getLong("mealId");
                    var customersNb = resultSet.getInt("customersNb");
                    var startDate = resultSet.getTimestamp("startDate");
                    Long mealDuration = resultSet.getLong("mealDuration");
                    Optional<Table> table = TableRepositoryImpl.getInstance().findById(resultSet.getLong("tableId"));
                    Optional<Bill> bill = BillRepositoryImpl.getInstance().findById(resultSet.getLong("billId"));
                    meal = Optional.of(new Meal(mealId, customersNb, startDate, mealDuration, table.orElse(null), bill.orElse(null)));
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return meal;
    }

    @Override
    public Meal save(Meal object) {
        if (object != null && object.getMealId() == null) {
            try (var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                setMealParameters(object, preparedStatement);
                preparedStatement.executeUpdate();
                generateKey(object, preparedStatement);
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        return object;
    }

    private void setMealParameters(Meal object, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setInt(1, object.getCustomersNb());
        preparedStatement.setTimestamp(2, object.getStartDate());
        if (object.getMealDuration() == null) {
            preparedStatement.setNull(3, Types.INTEGER);
        } else {
            preparedStatement.setLong(3, object.getMealDuration());
        }
        preparedStatement.setLong(4, object.getTable().getTableId());
        if (object.getBill() == null || object.getBill().getBillId() == null) {
            preparedStatement.setNull(5, Types.INTEGER);
        } else {
            preparedStatement.setLong(5, object.getBill().getBillId());
        }
    }

    private void generateKey(Meal object, PreparedStatement preparedStatement) {
        try (var resultSet = preparedStatement.getGeneratedKeys()) {
            if (resultSet.next()) {
                object.setMealId(resultSet.getLong(1));
            }
        } catch (SQLException s) {
            log.error(s.getMessage());
        }
    }

    @Override
    public Meal update(Meal object) {
        if (object != null && object.getMealId() != null) {
            try (var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
                setMealParameters(object, preparedStatement);
                preparedStatement.setLong(6, object.getMealId());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        return object;
    }

    public void delete(Long id) {
        super.delete(id, DELETE_SQL);
    }

    public static MealRepositoryImpl getInstance() {
        if (instance == null) {
            instance = new MealRepositoryImpl();
        }
        return instance;
    }
}
