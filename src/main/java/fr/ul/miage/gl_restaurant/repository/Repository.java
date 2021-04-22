package fr.ul.miage.gl_restaurant.repository;

import java.util.List;

public interface Repository<T, K> {

    List<T> findAll();

    T findById(K id);

    T save(T object);

    T update(T object);

    T delete(K id);
}

