package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Environment;
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

    private static final String FIND_ALL_SQL = "SELECT mealId, customersNb, startDate, mealDuration, tableId, billId FROM Meals";
    private static final String FIND_BY_ID_SQL = "SELECT mealId, customersNb, startDate, mealDuration, tableId, billId FROM Meals WHERE mealId = ?";
    private static final String SAVE_SQL = "INSERT INTO Meals(customersNb, startDate, mealDuration, tableId, billId) VALUES(?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE Meals SET customersNb = ?, startDate = ?, mealDuration = ?, tableId = ?, billId = ? WHERE mealId = ?";
    private static final String DELETE_SQL = "DELETE FROM Meals WHERE mealId = ?";

    protected MealRepositoryImpl(Environment environment) {
        super(environment);
    }

    @Override
    public List<Meal> findAll() {
        List<Meal> meals = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL_SQL)) {
            while (resultSet.next()) {
                Long mealId = resultSet.getLong("mealId");
                Integer customersNb = resultSet.getInt("customersNb");
                Timestamp startDate = resultSet.getTimestamp("startDate");
                Long mealDuration = resultSet.getLong("mealDuration");
                Optional<Table> table = new TableRepositoryImpl(Environment.TEST).findById(resultSet.getLong("tableId"));
                if(table.isPresent()){
                    Optional<Bill> bill = new BillRepositoryImpl(Environment.TEST).findById(resultSet.getLong("billId"));
                    bill.ifPresent(value -> meals.add(new Meal(mealId, customersNb, startDate, mealDuration, table.get(), value)));
                }
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return meals;
    }

    @Override
    public Optional<Meal> findById(Long id) {
        Optional<Meal> meal = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.first()) {
                    Long mealId = resultSet.getLong("mealId");
                    Integer customersnb = resultSet.getInt("customersnb");
                    Timestamp startDate = resultSet.getTimestamp("startDate");
                    Long mealDuration = resultSet.getLong("mealDuration");
                    Optional<Table> table = new TableRepositoryImpl(Environment.TEST).findById(resultSet.getLong("tableId"));
                    if (table.isPresent()) {
                        Optional<Bill> bill = new BillRepositoryImpl(Environment.TEST).findById(resultSet.getLong("billId"));
                        if (bill.isPresent()) {
                            meal = Optional.of(new Meal(mealId, customersnb, startDate, mealDuration, table.get(), bill.get()));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return meal;
    }

    @Override
    public Meal save(Meal object) {
        if (object != null && object.getMealId() == null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setInt(1, object.getCustomersNb());
                preparedStatement.setTimestamp(2, object.getStartDate());
                preparedStatement.setDouble(3, object.getMealDuration());
                preparedStatement.setLong(4, object.getTable().getTableId());
                preparedStatement.setLong(5, object.getBill().getBillId());
                int numRowsAffected = preparedStatement.executeUpdate();
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        object.setMealId(resultSet.getLong(1));
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
    public Meal update(Meal object) {
        if (object != null && object.getMealId() != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
                preparedStatement.setInt(1, object.getCustomersNb());
                preparedStatement.setTimestamp(2, object.getStartDate());
                preparedStatement.setLong(3, object.getMealDuration());
                preparedStatement.setLong(4, object.getTable().getTableId());
                preparedStatement.setLong(5, object.getBill().getBillId());
                preparedStatement.setLong(6, object.getMealId());
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
