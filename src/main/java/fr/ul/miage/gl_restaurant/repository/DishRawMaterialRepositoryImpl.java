package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.model.DishRawMaterial;

import java.util.List;
import java.util.Optional;

public class DishRawMaterialRepositoryImpl extends Repository<DishRawMaterial, Long> {

    protected DishRawMaterialRepositoryImpl(Environment environment) {
        super(environment);
    }

    @Override
    public List<DishRawMaterial> findAll() {
        return null;
    }

    @Override
    public Optional<DishRawMaterial> findById(Long id) {
        return null;
    }

    @Override
    public DishRawMaterial save(DishRawMaterial object) {
        return null;
    }

    @Override
    public DishRawMaterial update(DishRawMaterial object) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }
}