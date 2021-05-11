package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.repository.DishRepositoryImpl;

public class DirecteurController extends UserController {

    private final DishRepositoryImpl dishRepository;
    private final StockController stockController;

    public DirecteurController(Authentification auth) {
        super(auth);
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

    @Override
    public String displayActions() {
        //TODO
        return "";
    }

    @Override
    public void callAction(int action) {
        switch (action) {
            case 0:
                auth.disconnect();
                break;
            default:
                break;
        }
    }
}
