package fr.ul.miage.gl_restaurant.repository;

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
public class UserRepositoryImpl implements Repository<User, Long> {

    private static final String FIND_ALL_SQL = "SELECT userId, login, lastName, firstName, role FROM Users";
    private static final String FIND_BY_ID_SQL = "SELECT userId, login, lastName, firstName, role FROM Users WHERE userId = ?";
    private static final String SAVE_SQL = "INSERT INTO Users(login, lastName, firstName, role) VALUES(?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE Users SET login = ?, lastName = ?, firstName = ?, role = ? WHERE userId = ?";
    private static final String DELETE_SQL = "DELETE FROM Users WHERE userId = ?";

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL_SQL)) {
            while (resultSet.next()) {
                Long userId = resultSet.getLong("userId");
                String login = resultSet.getString("login");
                String lastName = resultSet.getString("lastName");
                String firstName = resultSet.getString("firstName");
                String role = resultSet.getString("role");
                users.add(new User(userId, login, lastName, firstName, role));
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return users;
    }

    @Override
    public Optional<User> findById(Long id) {
        Optional<User> user = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.first()) {
                    Long userId = resultSet.getLong("userId");
                    String login = resultSet.getString("login");
                    String lastName = resultSet.getString("lastName");
                    String firstName = resultSet.getString("firstName");
                    String role = resultSet.getString("role");
                    user = Optional.of(new User(userId, login, lastName, firstName, role));
                }
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return user;
    }

    @Override
    public User save(User object) {
        if (object != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, object.getLogin());
                preparedStatement.setString(2, object.getLastName());
                preparedStatement.setString(3, object.getFirstName());
                preparedStatement.setString(4, object.getRole());
                int numRowsAffected = preparedStatement.executeUpdate();
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        object.setUserId(resultSet.getLong(1));
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
    public User update(User object) {
        if (object != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
                preparedStatement.setString(1, object.getLogin());
                preparedStatement.setString(2, object.getLastName());
                preparedStatement.setString(3, object.getFirstName());
                preparedStatement.setString(4, object.getRole());
                preparedStatement.setLong(5, object.getUserId());
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
