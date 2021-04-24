package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.jdbc.DbAccess;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public abstract class Repository<T, K> {

    Connection connection = DbAccess.getInstance();

    protected Repository() {
        
    }

    abstract List<T> findAll();

    abstract Optional<T> findById(K id);

    abstract T save(T object);

    abstract T update(T object);

    abstract void delete(K id);
}

