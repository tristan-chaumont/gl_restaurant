package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.jdbc.DbAccess;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class Repository<T, K> {

    protected Connection connection;

    protected Repository(Environment environment) {
        connection = DbAccess.getInstance(environment);
    }

    public abstract List<T> findAll();

    public abstract Optional<T> findById(K id);

    public abstract T save(T object);

    public abstract T update(T object);

    public void delete(K id, String query) {
        if (id != null) {
            try (var preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setLong(1, (Long) id);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
    }
}

