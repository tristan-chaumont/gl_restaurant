package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.repository.DishRepositoryImpl;

public class DirecteurController {

    private final DishRepositoryImpl dishRepository;
    private final StockController stockController;

    public DirecteurController() {
        this.dishRepository = DishRepositoryImpl.getInstance();
        stockController = new StockController();
    }

    public void addDishToDailyMenu(Long dishId) {
        dishRepository.updateDailyMenu(dishId, true);
    }

    public void removeDishFromDailyMenu(Long dishId) {
        dishRepository.updateDailyMenu(dishId, false);
    }

    public void restock() {
        stockController.restock();
    }
}
