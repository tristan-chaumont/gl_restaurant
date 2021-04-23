package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.model.User;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UserRepositoryImpl implements Repository<User, Long> {

    private static final String FIND_ALL_SQL = "SELECT userId, login, lastName, firstName, role FROM Users";

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
    public User findById(Long id) {
        return null;
    }

    @Override
    public User save(User object) {
        return null;
    }

    @Override
    public User update(User object) {
        return null;
    }

    @Override
    public User delete(Long id) {
        return null;
    }
}
