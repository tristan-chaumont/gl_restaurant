package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Environment;

import java.util.List;
import java.util.Optional;

public class DishOrderRepositoryImpl extends Repository<DishOrderRepositoryImpl, Long> {

    protected DishOrderRepositoryImpl(Environment environment) {
        super(environment);
    }

    @Override
    public List<DishOrderRepositoryImpl> findAll() {
        return null;
    }

    @Override
    public Optional<DishOrderRepositoryImpl> findById(Long id) {
        return null;
    }

    @Override
    public DishOrderRepositoryImpl save(DishOrderRepositoryImpl object) {
        return null;
    }

    @Override
    public DishOrderRepositoryImpl update(DishOrderRepositoryImpl object) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }
}
