package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.jdbc.DbAccess;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface Repository<T, K> {

    Connection connection = DbAccess.getInstance();

    List<T> findAll();

    Optional<T> findById(K id);

    T save(T object);

    T update(T object);

    T delete(K id);
}

