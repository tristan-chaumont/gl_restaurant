package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.MenuTypes;
import fr.ul.miage.gl_restaurant.model.Dish;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.model.RawMaterial;
import fr.ul.miage.gl_restaurant.model.User;
import fr.ul.miage.gl_restaurant.repository.DishRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.OrderRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.RawMaterialRepositoryImpl;
import fr.ul.miage.gl_restaurant.utilities.InputUtils;
import fr.ul.miage.gl_restaurant.utilities.PrintUtils;
import org.apache.commons.text.TextStringBuilder;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class CuisinierController extends UserController {

    private final TreeSet<Order> ordersQueue;

    private final DishRepositoryImpl dishRepository = DishRepositoryImpl.getInstance();
    private final OrderRepositoryImpl orderRepository = OrderRepositoryImpl.getInstance();
    private final RawMaterialRepositoryImpl rawMaterialRepository = RawMaterialRepositoryImpl.getInstance();


    public CuisinierController(Authentification auth) {
        super(auth);
        this.ordersQueue = new TreeSet<>();
    }

    protected long askRMId(List<RawMaterial> rms) {
        return Long.parseLong(
                InputUtils.readInputInArray(
                        rms.stream().map(u -> u.getRawMaterialId().toString()).collect(Collectors.toList())
                )
        );
    }

    protected List<RawMaterial> getRawMaterials(){
        List<RawMaterial> rawMaterials = rawMaterialRepository.findAll();
        return rawMaterials;
    }

    protected void displayRawMaterials(List<RawMaterial> rawMaterialList){
        var stringBuilder = new TextStringBuilder();
        rawMaterialList.forEach(rm -> stringBuilder.appendln("[%d] %s", rm.getRawMaterialId(), rm.getRawMaterialName()));
        PrintUtils.print(stringBuilder.toString());
    }

    /*protected List<Dish> getDishes(){
        dishRepository.findAll()
    }*/

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

    public void prepareOrder(){
        var orders = getOrdersQueue();
        Order order = orders.first();
        prepareOrder(order);
        PrintUtils.print("La commande N°" + order.getOrderId() + " est prête à être livré.");
    }

    public void createDish(String dishName, String category, MenuTypes menuType, Double price, HashMap<RawMaterial, Integer> rawMaterials){
        Optional<Dish> dish = dishRepository.findByName(dishName);
        if(dish.isEmpty()){
            dishRepository.save(new Dish(dishName, category, menuType, price, false, rawMaterials));
        }
    }

    public void createDish(){
        PrintUtils.print("Veuillez saisir le nom du plat que vous voulez créer : ");
        var dishName = InputUtils.readInput();
        PrintUtils.print("Veuillez saisir la catégorie : ");
        var category = InputUtils.readInput();
        PrintUtils.print("Veuillez choisir le type du menu : ");
        var menuType = MenuTypes.getMenuType(InputUtils.readInputInArray(Arrays.asList(MenuTypes.ADULTES.toString(),MenuTypes.ENFANTS.toString())));
        PrintUtils.print("Veuillez choisir les ingrédients que contient votre plat : ");
        List<RawMaterial> rawMaterialList = getRawMaterials();
        HashMap<RawMaterial,Integer> rawMaterials = new HashMap<>();
        boolean stop = false;
        while(!stop){
            displayRawMaterials(rawMaterialList);
            PrintUtils.print("Pour ajouter un ingrédient, veuillez saisir son id : ");
            var rmId = askRMId(rawMaterialList);
            PrintUtils.print("Veuillez saisir la quantité de l'ingrédient nécessaire à la confection du plat : ");
            var quantity = InputUtils.readIntegerInput();
            if(quantity > 0) {
                rawMaterials.put(rawMaterialRepository.findById(rmId).get(), quantity);
            }
            PrintUtils.print("Voulez-vous ajoutez d'autres ingrédient ? (o/n)");
            if(InputUtils.readInputInArray(Arrays.asList("o","n")).equals("n")) {
                stop = true;
            }
        }
        PrintUtils.print("Veuillez saisir le prix de ce plat : ");
        var price = InputUtils.readDoubleInputInRange(0.0, 50.0);
        createDish(dishName,category,menuType,price,rawMaterials);
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

    public void updateDish(){

    }

    public void deleteDish(Dish dish){
        List<Order> orders = orderRepository.findByDish(dish.getDishId());
        if (orders.isEmpty()){
            dishRepository.delete(dish.getDishId());
        }
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
