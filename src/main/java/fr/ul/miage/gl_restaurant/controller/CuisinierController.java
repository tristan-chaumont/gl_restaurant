package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.MenuTypes;
import fr.ul.miage.gl_restaurant.model.Dish;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.model.RawMaterial;
import fr.ul.miage.gl_restaurant.repository.DishRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.OrderRepositoryImpl;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

public class CuisinierController extends UserController {

    private final TreeSet<Order> ordersQueue;

    private final DishRepositoryImpl dishRepository = DishRepositoryImpl.getInstance();
    private final OrderRepositoryImpl orderRepository = OrderRepositoryImpl.getInstance();


    public CuisinierController(Authentification auth) {
        super(auth);
        this.ordersQueue = new TreeSet<>();
    }

    /**
     * Récupère toutes les commandes prises par les serveurs et qui n'ont pas encore été préparées.
     * On fait une requête DB à chaque fois pour actualiser les commandes.
     * @return
     *      Les commandes passées par les clients.
     */
    public SortedSet<Order> getOrdersQueue() {
        List<Order> orders = orderRepository.findCurrentOrders();
        this.ordersQueue.addAll(orders);
        return ordersQueue;
    }

    /**
     * Prépare la commande prise par un serveur.
     * Pas besoin de vérifier les stocks, car ils sont décrémentés lorsque le serveur prend la commande.
     * @param order Commande à préparer.
     */
    public void prepareOrder(Order order) {
        order.setPreparationDate(Timestamp.from(Instant.now()));
        orderRepository.update(order);
    }

    public void createDish(String dishName, String category, MenuTypes menuType, Double price, HashMap<RawMaterial, Integer> rawMaterials){
        Optional<Dish> dish = dishRepository.findByName(dishName);
        if(dish.isEmpty()){
            dishRepository.save(new Dish(dishName, category, menuType, price, false, rawMaterials));
        }
    }

    public void updateDish(Dish dish, String dishName, String category, MenuTypes menuType, Double price, HashMap<RawMaterial, Integer> rawMaterials){
        Optional<Dish> resDish = dishRepository.findByName(dishName);
        if(resDish.isEmpty() || dishName.equals(dish.getDishName())){
            List<Order> orders = orderRepository.findByDish(dish.getDishId());
            if(orders.isEmpty()){
                dish.setDishName(dishName);
                dish.setCategory(category);
                dish.setPrice(price);
                dish.setMenuType(menuType);
                dish.setRawMaterials(rawMaterials);
                dishRepository.update(dish);
            }
        }
    }

    public void deleteDish(Dish dish){
        List<Order> orders = orderRepository.findByDish(dish.getDishId());
        if (orders.isEmpty()){
            dishRepository.delete(dish.getDishId());
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
