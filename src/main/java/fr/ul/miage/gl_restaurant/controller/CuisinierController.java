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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class CuisinierController extends UserController {

    private final TreeSet<Order> ordersQueue;
    private double preparationTime;

    private final DishRepositoryImpl dishRepository = DishRepositoryImpl.getInstance();
    private final OrderRepositoryImpl orderRepository = OrderRepositoryImpl.getInstance();
    private final RawMaterialRepositoryImpl rawMaterialRepository = RawMaterialRepositoryImpl.getInstance();

    /**
     * ACTIONS DE L'UTILISATEUR
     */
    private static final String ACTION_1 = "1 : Préparer une commande";
    private static final String ACTION_2 = "2 : Créer un plat";
    private static final String ACTION_3 = "3 : Modifier un plat";
    private static final String ACTION_4 = "4 : Supprimer un plat";
    private static final String ACTION_5 = "5 : Afficher les commandes à préparer";
    private static final String ACTION_6 = "6 : Afficher la carte du jour";
    private static final String ACTION_7 = "7 : Afficher les stocks";
    private static final String ACTION_8 = "8 : Afficher le temps de préparation moyen";


    public CuisinierController(Authentification auth) {
        super(auth);
        this.ordersQueue = new TreeSet<>();
        this.actions.addAll(Arrays.asList(ACTION_1, ACTION_2, ACTION_3, ACTION_4, ACTION_5, ACTION_6, ACTION_7, ACTION_8));
    }

    protected long askRMId(List<RawMaterial> rms) {
        return Long.parseLong(
                InputUtils.readInputInArray(
                        rms.stream().map(rm -> rm.getRawMaterialId().toString()).collect(Collectors.toList())
                )
        );
    }

    protected long askUpdateRMId(List<RawMaterial> rms) {
        var list = rms.stream().map(rm -> rm.getRawMaterialId().toString()).collect(Collectors.toList());
        list.add("0");
        return Long.parseLong(
                InputUtils.readInputInArray(list)
        );
    }

    protected long askDishId(List<Dish> dishes) {
        return Long.parseLong(
                InputUtils.readInputInArray(
                        dishes.stream().map(d -> d.getDishId().toString()).collect(Collectors.toList())
                )
        );
    }

    protected long askDeleteDishId(List<Dish> dishes) {
        var list =  dishes.stream().map(d -> d.getDishId().toString()).collect(Collectors.toList());
        list.add("0");
        return Long.parseLong(
                InputUtils.readInputInArray(
                        list
                )
        );
    }

    protected List<RawMaterial> getRawMaterials(){
        List<RawMaterial> rawMaterials = rawMaterialRepository.findAll();
        return rawMaterials;
    }

    protected String displayRawMaterials(List<RawMaterial> rawMaterialList){
        var stringBuilder = new TextStringBuilder();
        rawMaterialList.forEach(rm -> stringBuilder.appendln("[%d] %s", rm.getRawMaterialId(), rm.getRawMaterialName()));
        return stringBuilder.toString();
    }

    protected List<Dish> getDishes(){
        return dishRepository.findNotDailyMenu();
    }

    protected String displayDishes(List<Dish> dishes){
        var stringBuilder = new TextStringBuilder();
        dishes.forEach(dish -> stringBuilder.appendln("[%d] %s", dish.getDishId(), dish.getDishName()));
        return stringBuilder.toString();
    }

    protected Dish displayDish(Long dishId){
        var dish = dishRepository.findById(dishId).get();
        PrintUtils.println(dish.toString());
        return dish;
    }

    protected String displayDishRawMaterials(Map<RawMaterial,Integer> rawMaterials){
        var stringBuilder = new TextStringBuilder();
        rawMaterials.forEach((rm,q) -> stringBuilder.appendln("[%d] %s : %d", rm.getRawMaterialId(), rm.getRawMaterialName(), q));
        return stringBuilder.toString();
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

    public String displayOrdersQueue(){
        var orders = getOrdersQueue();
        var stringBuilder = new TextStringBuilder();
        stringBuilder.appendln("-".repeat(20))
                .appendln("|" + StringUtils.center("Commandes", 18) + "|")
                .appendln("-".repeat(20));
        orders.forEach(order -> {
            stringBuilder.appendln("Commande de la table N°%d :", order.getMeal().getTable().getTableId());
            order.getDishes().forEach((dish,quantity) -> stringBuilder.appendln("\t- %s (x%d)", dish.getDishName(), quantity));
            stringBuilder.appendln("");
        });
        return stringBuilder.toString();
    }

    public String displayDailyMenu(){
        var dailyMenu = dishRepository.findDailyMenu();
        var stringBuilder = new TextStringBuilder();
        stringBuilder.appendln("-".repeat(20))
                .appendln("|" + StringUtils.center("Menu du jour", 18) + "|")
                .appendln("-".repeat(20));
        dailyMenu.forEach(dish -> stringBuilder.appendln("\t- %s %.2f€", dish.getDishName(), dish.getPrice()));
        return stringBuilder.toString();
    }

    public String displayStock(){
        var stocks = getRawMaterials();
        var stringBuilder = new TextStringBuilder();
        stringBuilder.appendln("-".repeat(20))
                .appendln("|" + StringUtils.center("Stocks", 18) + "|")
                .appendln("-".repeat(20));
        stocks.forEach(rm -> stringBuilder.appendln("%s : %d%s", rm.getRawMaterialName(), rm.getStockQuantity(), rm.getUnit().toString()));
        return stringBuilder.toString();
    }

    protected void addPreparationTime(Order order){
        preparationTime += (order.getPreparationDate().getTime() - order.getOrderDate().getTime())/60000;
    }

    public String averagePreparationTime(){
        var orders = orderRepository.findPrepOrder();
        var avg = preparationTime / orders.size();
        var stringBuilder = new TextStringBuilder();
        stringBuilder.appendln("En moyenne, un plat est préparé en %.2f minutes", avg);
        return stringBuilder.toString();
    }

    /**
     * Prépare la commande prise par un serveur.
     * Pas besoin de vérifier les stocks, car ils sont décrémentés lorsque le serveur prend la commande.
     * @param order Commande à préparer.
     */
    public void prepareOrder(Order order) {
        order.setPreparationDate(Timestamp.from(Instant.now()));
        addPreparationTime(order);
        orderRepository.update(order);
    }

    public void prepareOrder(){
        var orders = getOrdersQueue();
        Order order = orders.first();
        prepareOrder(order);
        PrintUtils.println("La commande N°" + order.getOrderId() + " est prête à être servie.");
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
        PrintUtils.print("Ce plat est-il un menu enfant ? y/n) : ");
        var choice = InputUtils.readInputConfirmation();
        var menuType = MenuTypes.ADULTES;
        if(choice.equals("o")){
            menuType = MenuTypes.ENFANTS;
        }
        PrintUtils.print("Veuillez choisir les ingrédients que contient votre plat : ");
        List<RawMaterial> rawMaterialList = getRawMaterials();
        HashMap<RawMaterial,Integer> rawMaterials = new HashMap<>();
        boolean stop = false;
        while(!stop){
            PrintUtils.println(displayRawMaterials(rawMaterialList));
            PrintUtils.print("Pour ajouter un ingrédient, veuillez saisir son id : ");
            var rmId = askRMId(rawMaterialList);
            PrintUtils.print("Veuillez saisir la quantité de l'ingrédient nécessaire à la confection du plat : ");
            var quantity = InputUtils.readIntegerInput();
            if(quantity > 0) {
                rawMaterials.put(rawMaterialRepository.findById(rmId).get(), quantity);
            }
            PrintUtils.print("Voulez-vous ajoutez d'autres ingrédient ? (y/n)");
            if(InputUtils.readInputConfirmation().equals("n")) {
                stop = true;
            }
        }
        PrintUtils.print("Veuillez saisir le prix de ce plat : ");
        var price = InputUtils.readDoubleInputInRange(1.0, 50.0);
        createDish(dishName,category,menuType,price,rawMaterials);
    }

    public void updateDish(Dish dish, String dishName, String category, MenuTypes menuType, Double price, Map<RawMaterial, Integer> rawMaterials){
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
        List<Dish> dishes = getDishes();
        if(dishes.isEmpty())
            PrintUtils.println("Il n'y pas de plat modifiable.");
        else {
            PrintUtils.println("Voici la liste des plats que vous pouvez modifié : ");
            PrintUtils.println(displayDishes(dishes));
            PrintUtils.print("Pour modifier l'un de ces plats, veuillez saisir son id : ");
            var dishId = askDishId(dishes);
            var dish = displayDish(dishId);
            PrintUtils.print("Pour modifier le nom du plat, veuillez saisir un autre nom, sinon entrez le même nom : ");
            var dishName = InputUtils.readInput();
            PrintUtils.print("Pour modifier la catégorie, veuillez saisir la nouvelle catégorie, sinon entrez l'ancienne : ");
            var category = InputUtils.readInput();
            PrintUtils.print("Voulez-vous modifier le type du menu ? (y/n) : ");
            var choice = InputUtils.readInputConfirmation();
            var menuType = MenuTypes.ADULTES;
            if ((dish.getMenuType() == MenuTypes.ADULTES && choice.equals("y")) || (dish.getMenuType() == MenuTypes.ENFANTS && choice.equals("n"))) {
                menuType = MenuTypes.ENFANTS;
            }
            PrintUtils.print("Pour modifier le prix, veuillez saisir le nouveau prix, sinon entrez l'ancien : ");
            var price = InputUtils.readDoubleInputInRange(1.0, 50.0);
            var rawMaterials = dish.getRawMaterials();
            PrintUtils.println(displayDishRawMaterials(rawMaterials));
            var rmId = 1l;
            while (rmId != 0) {
                PrintUtils.print("Pour modifier l'un des ingrédients veuillez saisir son id, sinon entrez 0");
                rmId = askUpdateRMId(List.copyOf(rawMaterials.keySet()));
                if (rmId != 0) {
                    var rm = rawMaterialRepository.findById(rmId).get();
                    PrintUtils.print("Pour modifier la quantité de cet ingrédient entrez la nouvelle valeur, sinon entrez 0 pour supprimer cet ingrédient : ");
                    var quantity = InputUtils.readIntegerInput();
                    if (quantity > 0) {
                        rawMaterials.put(rm, quantity);
                    } else {
                        rawMaterials.remove(rm);
                    }
                }
            }
            rmId = 1l;
            while (rmId != 0) {
                var rms = getRawMaterials();
                PrintUtils.print(displayRawMaterials(rms));
                PrintUtils.print("Pour ajouter un ingrédient, veuillez entrer son id, sinon entrez 0");
                rmId = askUpdateRMId(rms);
                var rmAdd = rawMaterialRepository.findById(rmId).get();
                if (rmId != 0) {
                    PrintUtils.print("Pour modifier la quantité de cet ingrédient entrez la nouvelle valeur, sinon entrez 0 pour supprimer cet ingrédient : ");
                    var quantityAdd = InputUtils.readIntegerInput();
                    if (quantityAdd > 0) {
                        rawMaterials.put(rmAdd, quantityAdd);
                    }
                }
            }
            updateDish(dish, dishName, category, menuType, price, rawMaterials);
        }
    }

    public void deleteDish(Dish dish){
        List<Order> orders = orderRepository.findByDish(dish.getDishId());
        if (orders.isEmpty()){
            dishRepository.delete(dish.getDishId());
        }
    }

    public void deleteDish(){
        var dishes = getDishes();
        if(dishes.isEmpty())
            PrintUtils.println("Il n'y a pas de plat pouvant être supprimé");
        else {
            PrintUtils.println(displayDishes(dishes));
            PrintUtils.print("Pour supprimer un repas veuillez entrez son id, sinon entrez 0 : ");
            var dishId = askDeleteDishId(dishes);
            if (dishId != 0) {
                var dish = dishRepository.findById(dishId).get();
                deleteDish(dish);
            }
        }
    }

    @Override
    public void callAction(int action) {
        switch (action) {
            case 0:
                auth.disconnect();
                break;
            case 1:
                prepareOrder();
                break;
            case 2:
                createDish();
                break;
            case 3:
                updateDish();
                break;
            case 4:
                deleteDish();
                break;
            case 5:
                PrintUtils.println(displayOrdersQueue());
                break;
            case 6:
                PrintUtils.println(displayDailyMenu());
                break;
            case 7:
                PrintUtils.println(displayStock());
                break;
            case 8:
                PrintUtils.println(averagePreparationTime());
                break;
            default:
                break;
        }
    }
}
