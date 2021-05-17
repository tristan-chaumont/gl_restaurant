package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.model.User;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class TableRepositoryImpl extends Repository<Table, Long> {

    private static TableRepositoryImpl instance;

    private static final String TABLEID_COLUMN_NAME = "tableId";

    private static final String FIND_ALL_SQL = "SELECT tableId, floor, state, places, userId FROM Tables";
    private static final String FIND_BY_ID_SQL = "SELECT tableId, floor, state, places, userId FROM Tables WHERE tableId = ?";
    private static final String FIND_BY_USERID_SQL = "SELECT tableId, floor, state, places, userId FROM Tables WHERE userId = ?";
    private static final String SAVE_SQL = "INSERT INTO Tables(floor, state, places, userId) VALUES(?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE Tables SET floor = ?, state = ?, places = ?, userId = ? WHERE tableId = ?";
    private static final String DELETE_SQL = "DELETE FROM Tables WHERE tableId = ?";

    private TableRepositoryImpl(Environment environment) {
        super(environment);
    }

    @Override
    public List<Table> findAll() {
        List<Table> tables = new ArrayList<>();
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery(FIND_ALL_SQL)) {
            while (resultSet.next()) {
                tables.add(new Table(resultSet));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return tables;
    }

    @Override
    public Optional<Table> findById(Long id) {
        Optional<Table> table = Optional.empty();
        if (id != null) {
            try (var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                preparedStatement.setLong(1, id);
                try (var resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.first()) {
                        table = Optional.of(new Table(resultSet));
                    }
                }
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        return table;
    }

    public List<Table> findByUserId(Long id) {
        List<Table> tables = new ArrayList<>();
        try (var preparedStatement = connection.prepareStatement(FIND_BY_USERID_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setLong(1, id);
            try (var resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Long tableId = resultSet.getLong(TABLEID_COLUMN_NAME);
                    Integer floor = resultSet.getInt("floor");
                    var state = TableStates.getState(resultSet.getString("state"));
                    Integer places = resultSet.getInt("places");
                    Optional<User> user = UserRepositoryImpl.getInstance().findById(resultSet.getLong("userId"));
                    tables.add(new Table(tableId, floor, state, places, user.orElse(null)));
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return tables;
    }

    @Override
    public Table save(Table object) {
        if (object != null && object.getTableId() == null) {
            try (var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                addAllTableFields(object, preparedStatement);
                preparedStatement.executeUpdate();
                generateKey(object, preparedStatement);
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        return object;
    }

    private void addAllTableFields(Table object, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setInt(1, object.getFloor());
        preparedStatement.setString(2, object.getState().toString());
        preparedStatement.setInt(3, object.getPlaces());
        if (object.getUser() == null) {
            preparedStatement.setNull(4, Types.INTEGER);
        } else {
            preparedStatement.setLong(4, object.getUser().getUserId());
        }
    }

    private void generateKey(Table object, PreparedStatement preparedStatement) {
        try (var resultSet = preparedStatement.getGeneratedKeys()) {
            if (resultSet.next()) {
                object.setTableId(resultSet.getLong(1));
            }
        } catch (SQLException s) {
            log.error(s.getMessage());
        }
    }

    @Override
    public Table update(Table object) {
        if (object != null && object.getTableId() != null) {
            try (var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
                addAllTableFields(object, preparedStatement);
                preparedStatement.setLong(5, object.getTableId());
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

    public static TableRepositoryImpl getInstance() {
        if (instance == null) {
            instance = new TableRepositoryImpl(Environment.TEST);
        }
        return instance;
    }
}
