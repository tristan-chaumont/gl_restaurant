package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.jdbc.DbAccess;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public abstract class Repository<T, K> {

    public Connection connection;

    protected Repository(Environment environment) {
        connection = DbAccess.getInstance(environment);
    }

    abstract List<T> findAll();

    abstract Optional<T> findById(K id);

    abstract T save(T object);

    abstract T update(T object);

    abstract void delete(K id);
}

