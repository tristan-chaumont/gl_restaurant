package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.Units;
import fr.ul.miage.gl_restaurant.model.Dish;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.model.RawMaterial;
import fr.ul.miage.gl_restaurant.repository.DishRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.OrderRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.RawMaterialRepositoryImpl;

import java.util.List;
import java.util.Optional;

import static ch.qos.logback.core.joran.spi.ConsoleTarget.findByName;

public class DirecteurController extends UserController {

    private final DishRepositoryImpl dishRepository;
    private final RawMaterialRepositoryImpl rawMaterialRepository;
    private final OrderRepositoryImpl orderRepository;
    private final StockController stockController;

    public DirecteurController(Authentification auth) {
        super(auth);
        this.dishRepository = DishRepositoryImpl.getInstance();
        this.rawMaterialRepository = RawMaterialRepositoryImpl.getInstance();
        this.orderRepository = OrderRepositoryImpl.getInstance();
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

    public RawMaterial addRawMaterial(String rawMaterialName, Integer stockQuantity, Units unit){
        Optional<RawMaterial> result = rawMaterialRepository.findByName(rawMaterialName);
        RawMaterial rm = null;
        if(result.isEmpty()){
            rm = rawMaterialRepository.save(new RawMaterial(rawMaterialName, stockQuantity, unit));
        }
        return rm;
    }

    public void updateRawMaterial(RawMaterial rawMaterial, String rawMaterialName, Integer stockQuantity, Units unit){
        Optional<RawMaterial> result = rawMaterialRepository.findByName(rawMaterialName);
        if(result.isEmpty()){
            List<Dish> dishes = dishRepository.findByRM(rawMaterial.getRawMaterialId());
            boolean updatable = true;
            for (Dish dish: dishes) {
                List<Order> orders = orderRepository.findByDish(dish.getDishId());
                if(orders.size() > 0){
                    updatable = false;
                    break;
                }
            }
            if (updatable) {
                rawMaterial.setRawMaterialName(rawMaterialName);
                rawMaterial.setStockQuantity(stockQuantity);
                rawMaterial.setUnit(unit);
                rawMaterialRepository.update(rawMaterial);
            }
        }
    }

    public void deleteRawMaterial(RawMaterial rawMaterial){
        List<Dish> dishes = dishRepository.findByRM(rawMaterial.getRawMaterialId());
        if(dishes.size() == 0){
            rawMaterialRepository.delete(rawMaterial.getRawMaterialId());
        }
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
