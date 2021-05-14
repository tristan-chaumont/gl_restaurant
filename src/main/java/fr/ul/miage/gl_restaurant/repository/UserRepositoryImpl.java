package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.model.User;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Slf4j
public class UserRepositoryImpl extends Repository<User, Long> {

    private static UserRepositoryImpl instance;

    private static final String FIND_ALL_SQL = "SELECT userId, login, lastName, firstName, role FROM Users";
    private static final String FIND_BY_ID_SQL = "SELECT userId, login, lastName, firstName, role FROM Users WHERE userId = ?";
    private static final String FIND_BY_LOGIN_SQL = "SELECT userId, login, lastName, firstName, role FROM Users WHERE login = ?";
    private static final String FIND_BY_ROLE_SQL = "SELECT userId, login, lastName, firstName, role FROM Users WHERE role = ?";
    private static final String SAVE_SQL = "INSERT INTO Users(login, lastName, firstName, role) VALUES(?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE Users SET login = ?, lastName = ?, firstName = ?, role = ? WHERE userId = ?";
    private static final String DELETE_SQL = "DELETE FROM Users WHERE userId = ?";

    private UserRepositoryImpl(Environment environment) {
        super(environment);
    }

    /* FIND */

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery(FIND_ALL_SQL)) {
            while (resultSet.next()) {
                users.add(new User(resultSet));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return users;
    }

    @Override
    public Optional<User> findById(Long id) {
        Optional<User> user = Optional.empty();
        if (id != null) {
            try (var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                preparedStatement.setLong(1, id);
                user = getUserFromSQL(preparedStatement);
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        return user;
    }

    public Optional<User> findByLogin(String login) {
        Optional<User> user = Optional.empty();
        try (var preparedStatement = connection.prepareStatement(FIND_BY_LOGIN_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setString(1, login);
            user = getUserFromSQL(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return user;
    }

    public Set<User> findByRole(Roles role) {
        Set<User> users = new LinkedHashSet<>();
        try (var preparedStatement = connection.prepareStatement(FIND_BY_ROLE_SQL)) {
            preparedStatement.setString(1, role.toString());
            try (var resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(new User(resultSet));
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return users;
    }

    private Optional<User> getUserFromSQL(PreparedStatement preparedStatement) {
        Optional<User> user = Optional.empty();
        try (var resultSet = preparedStatement.executeQuery()) {
            if (resultSet.first()) {
                user = Optional.of(new User(resultSet));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return user;
    }

    @Override
    public User save(User object) {
        if (object != null && object.getUserId() == null) {
            Optional<User> user = findByLogin(object.getLogin());
            if (user.isEmpty()) {
                try (var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setString(1, object.getLogin());
                    preparedStatement.setString(2, object.getLastName());
                    preparedStatement.setString(3, object.getFirstName());
                    preparedStatement.setString(4, object.getRole().toString());
                    preparedStatement.executeUpdate();
                    generateKey(object, preparedStatement);
                } catch (SQLException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return object;
    }

    private void generateKey(User object, PreparedStatement preparedStatement) {
        try (var resultSet = preparedStatement.getGeneratedKeys()) {
            if (resultSet.next()) {
                object.setUserId(resultSet.getLong(1));
            }
        } catch (SQLException s) {
            log.error(s.getMessage());
        }
    }

    @Override
    public User update(User object) {
        if (object != null && object.getUserId() != null) {
            Optional<User> user = findByLogin(object.getLogin());
            if (user.isEmpty() || object.getUserId().equals(user.get().getUserId())) {
                try (var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
                    preparedStatement.setString(1, object.getLogin());
                    preparedStatement.setString(2, object.getLastName());
                    preparedStatement.setString(3, object.getFirstName());
                    preparedStatement.setString(4, object.getRole().toString());
                    preparedStatement.setLong(5, object.getUserId());
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    log.error(e.getMessage());
                }
            } else {
                // La mise à jour de l'utilisateur a échoué, le login que vous lui avez affecté est déjà existant.
                Optional<User> userAlreadyExists = findById(object.getUserId());
                if (userAlreadyExists.isPresent()) {
                    object = userAlreadyExists.get();
                }
            }
        }
        return object;
    }

    public void delete(Long id) {
        super.delete(id, DELETE_SQL);
    }

    public static UserRepositoryImpl getInstance() {
        if (instance == null) {
            instance = new UserRepositoryImpl(Environment.TEST);
        }
        return instance;
    }
}
