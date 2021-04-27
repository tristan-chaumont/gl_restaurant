package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Table;
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
public class TableRepositoryImpl extends Repository<Table, Long> {

    private static final String FIND_ALL_SQL = "SELECT tableId, floor, state, places, userId FROM Tables";
    private static final String FIND_BY_ID_SQL = "SELECT tableId, floor, state, places, userId FROM Tables WHERE tableId = ?";
    private static final String SAVE_SQL = "INSERT INTO Tables(floor, state, places, userId) VALUES(?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE Tables SET floor = ?, state = ?, places = ?, userId = ? WHERE tableId = ?";
    private static final String DELETE_SQL = "DELETE FROM Tables WHERE tableId = ?";

    protected TableRepositoryImpl(Environment environment) {
        super(environment);
    }

    @Override
    public List<Table> findAll() {
        List<Table> tables = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL_SQL)) {
            while (resultSet.next()) {
                Long tableId = resultSet.getLong("tableId");
                Integer floor = resultSet.getInt("floor");
                TableStates state = TableStates.getState(resultSet.getString("state"));
                Integer places = resultSet.getInt("places");
                Optional<User> user = new UserRepositoryImpl(Environment.TEST).findById(resultSet.getLong("userId"));
                user.ifPresent(value -> tables.add(new Table(tableId, floor, state, places, value)));
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return tables;
    }

    @Override
    public Optional<Table> findById(Long id) {
        Optional<Table> table = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.first()) {
                    Long tableId = resultSet.getLong("tableId");
                    Integer floor = resultSet.getInt("floor");
                    TableStates state = TableStates.getState(resultSet.getString("state"));
                    Integer places = resultSet.getInt("places");
                    Optional<User> user = new UserRepositoryImpl(Environment.TEST).findById(resultSet.getLong("userId"));
                    if (user.isPresent()) {
                        table = Optional.of(new Table(tableId, floor, state, places, user.get()));
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return table;
    }

    @Override
    public Table save(Table object) {
        if (object != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setInt(1, object.getFloor());
                preparedStatement.setString(2, object.getState().toString());
                preparedStatement.setInt(3, object.getPlaces());
                preparedStatement.setLong(4, object.getUser().getUserId());
                int numRowsAffected = preparedStatement.executeUpdate();
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        object.setTableId(resultSet.getLong(1));
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
    public Table update(Table object) {
        if (object != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
                preparedStatement.setInt(1, object.getFloor());
                preparedStatement.setString(2, object.getState().toString());
                preparedStatement.setInt(3, object.getPlaces());
                preparedStatement.setLong(4, object.getUser().getUserId());
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
