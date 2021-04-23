package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.jdbc.DbAccess;

import java.sql.Connection;
import java.util.List;

public interface Repository<T, K> {

    Connection connection = DbAccess.getInstance();

    List<T> findAll();

    T findById(K id);

    T save(T object);

    T update(T object);

    T delete(K id);
}

