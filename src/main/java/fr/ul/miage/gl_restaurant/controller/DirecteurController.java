package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.Units;
import fr.ul.miage.gl_restaurant.model.Dish;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.model.RawMaterial;
import fr.ul.miage.gl_restaurant.repository.DishRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.OrderRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.RawMaterialRepositoryImpl;
import fr.ul.miage.gl_restaurant.utilities.PrintUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

import java.util.*;
import java.util.stream.Collectors;

public class DirecteurController extends UserController {

    private final DishRepositoryImpl dishRepository;
    private final RawMaterialRepositoryImpl rawMaterialRepository;
    private final OrderRepositoryImpl orderRepository;
    private final StockController stockController;

    /**
     * ACTION DE L'UTILISATEUR
     */
    private static final String ACTION_1 = "1 : Connaître le profit des plats";

    public DirecteurController(Authentification auth) {
        super(auth);
        this.dishRepository = DishRepositoryImpl.getInstance();
        this.rawMaterialRepository = RawMaterialRepositoryImpl.getInstance();
        this.orderRepository = OrderRepositoryImpl.getInstance();
        stockController = new StockController();
        this.actions.addAll(Arrays.asList(
            ACTION_1
        ));
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
            var updatable = true;
            for (Dish dish: dishes) {
                List<Order> orders = orderRepository.findByDish(dish.getDishId());
                if(!orders.isEmpty()){
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
        if(dishes.isEmpty()){
            rawMaterialRepository.delete(rawMaterial.getRawMaterialId());
        }
    }

    /**
     * Génère la liste des profits de chaque plat.
     * Le profit est incrémenté pour un plat uniquement s'il fait partie d'une commande
     * et que la facture de celle-ci a été payée.
     * La liste des profits est triée par ordre décroissant.
     */
    protected Map<Dish, Double> generateDishesProfit() {
        Map<Dish, Double> profits = new HashMap<>();
        // on initialise la map des profits avec tous les plats et 0€ de profit
        dishRepository.findAll().forEach(d -> profits.put(d, 0.0));
        // on parcourt la liste des commandes et on incrémente le profit de chaque plat
        // uniquement si la facture a été payée
        orderRepository.findAll().forEach(o -> {
            var meal = o.getMeal();
            if (meal.getBill().isPaid()) {
                o.getDishes().forEach((dish, quantity) -> {
                    double oldProfit = profits.get(dish);
                    profits.put(dish, oldProfit + dish.getPrice() * quantity);
                });
            }
        });
        return profits
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new
                ));
    }

    protected String displayDishesProfit() {
        PrintUtils.println("-".repeat(50));
        PrintUtils.println(StringUtils.center("Profits des plats du restaurant", 50));
        PrintUtils.println("-".repeat(50));
        var stringBuilder = new TextStringBuilder();
        generateDishesProfit().forEach((dish, profit) ->
            stringBuilder.appendln("- %s : %.2f€", dish.getDishName(), profit)
        );
        return stringBuilder.toString();
    }

    @Override
    public void callAction(int action) {
        PrintUtils.println();
        switch (action) {
            case 0:
                auth.disconnect();
                break;
            case 1:
                PrintUtils.println(displayDishesProfit());
                break;
            default:
                break;
        }
    }
}
