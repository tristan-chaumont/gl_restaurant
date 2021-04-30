package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.repository.DishRepositoryImpl;

public class DirecteurController {

    private final DishRepositoryImpl dishRepository;

    public DirecteurController(DishRepositoryImpl dishRepository) {
        this.dishRepository = dishRepository;
    }

    public void addDishToDailyMenu(Long dishId) {
        dishRepository.updateDailyMenu(dishId, true);
    }

    public void removeDishFromDailyMenu(Long dishId) {
        dishRepository.updateDailyMenu(dishId, false);
    }
}
