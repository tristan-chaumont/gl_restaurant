package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.MenuTypes;
import fr.ul.miage.gl_restaurant.model.Dish;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.model.RawMaterial;
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
        list.add(0, "0");
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
        list.add(0, "0");
        return Long.parseLong(
                InputUtils.readInputInArray(
                        list
                )
        );
    }

    protected List<RawMaterial> getRawMaterials(){
        return rawMaterialRepository.findAll();
    }

    protected String displayRawMaterials(List<RawMaterial> rawMaterialList){
        var stringBuilder = new TextStringBuilder();
        rawMaterialList.forEach(rm -> stringBuilder.appendln("[%d] %s", rm.getRawMaterialId(), rm.getRawMaterialName()));
        return stringBuilder.toString();
    }

    protected List<Dish> getNotDailyMenuDishes(){
        return dishRepository.findNotDailyMenu();
    }

    protected String displayDishes(List<Dish> dishes){
        var stringBuilder = new TextStringBuilder();
        dishes.forEach(dish -> stringBuilder.appendln("[%d] %s", dish.getDishId(), dish.getDishName()));
        return stringBuilder.toString();
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

    public void prepareOrder() {
        var orders = getOrdersQueue();
        if (orders.isEmpty()) {
            PrintUtils.println("Il n'y aucune commande à préparer.%n");
        } else {
            var order = orders.first();
            prepareOrder(order);
            this.ordersQueue.remove(order);
            PrintUtils.println("La commande n°" + order.getOrderId() + " est prête à être servie.%n");
        }
    }

    public void createDish(String dishName, String category, MenuTypes menuType, Double price, Map<RawMaterial, Integer> rawMaterials){
        Optional<Dish> dish = dishRepository.findByName(dishName);
        if(dish.isEmpty()) {
            dishRepository.save(new Dish(dishName, category, menuType, price, false, rawMaterials));
            PrintUtils.println("%nLe plat a bien été créé.%n");
        } else {
            PrintUtils.println("%nUn plat du même nom existe déjà, annulation de la création.%n");
        }
    }

    protected Map<RawMaterial, Integer> selectRawMaterials(List<RawMaterial> rawMaterialList) {
        Map<RawMaterial,Integer> rawMaterials = new HashMap<>();
        var select = "y";
        do {
            PrintUtils.println(displayRawMaterials(rawMaterialList));
            PrintUtils.print("Pour ajouter un ingrédient, veuillez saisir son id : ");
            var rmId = askRMId(rawMaterialList);
            PrintUtils.print("Veuillez saisir la quantité de l'ingrédient nécessaire à la confection du plat : ");
            var quantity = InputUtils.readIntegerInputInRange(1, 21);
            Optional<RawMaterial> rm = rawMaterialRepository.findById(rmId);
            if (rm.isPresent()) {
                rawMaterials.put(rm.get(), quantity);
                PrintUtils.print("Voulez-vous ajoutez d'autres ingrédient ? [y]es ou [n]o : ");
                select = InputUtils.readInputConfirmation();
                PrintUtils.println();
            } else {
                PrintUtils.println("Cette matière première n'existe pas, veuillez réessayer.");
            }
        } while (select.equalsIgnoreCase("y"));
        return rawMaterials;
    }

    public void createDish() {
        PrintUtils.print("Veuillez saisir le nom du plat que vous voulez créer : ");
        var dishName = InputUtils.readInput();
        PrintUtils.print("Veuillez saisir la catégorie : ");
        var category = InputUtils.readInput();
        PrintUtils.print("Ce plat est-il un menu enfant ? [y]es ou [n]o : ");
        var choice = InputUtils.readInputConfirmation();
        var menuType = choice.equalsIgnoreCase("y") ? MenuTypes.ENFANTS : MenuTypes.ADULTES;
        List<RawMaterial> rawMaterialList = getRawMaterials();
        if (rawMaterialList.isEmpty()) {
            PrintUtils.println("%nIl n'existe aucune matière première pour le moment, veuillez en ajouter avant de confectionner votre plat.");
        } else {
            PrintUtils.println("Veuillez choisir les ingrédients que contient votre plat :%n");
            var rawMaterials = selectRawMaterials(rawMaterialList);
            if (rawMaterials.isEmpty()) {
                PrintUtils.println("%nImpossible de créer un plat qui n'a pas d'ingrédients.%n");
            } else {
                PrintUtils.print("Veuillez saisir le prix de ce plat : ");
                var price = InputUtils.readDoubleInputInRange(1.0, 51.0);
                createDish(dishName, category, menuType, price, rawMaterials);
            }
        }
    }

    public void updateDish(Dish dish, String dishName, String category, MenuTypes menuType, Double price, Map<RawMaterial, Integer> rawMaterials) {
        if (rawMaterials.isEmpty()) {
            PrintUtils.println("%nModification impossible si le plat n'a pas d'ingrédients.%n");
        } else {
            Optional<Dish> resDish = dishRepository.findByName(dishName);
            if(resDish.isEmpty() || dishName.equals(dish.getDishName())) {
                List<Order> orders = orderRepository.findByDish(dish.getDishId());
                if (orders.isEmpty()) {
                    dish.setDishName(dishName);
                    dish.setCategory(category);
                    dish.setPrice(price);
                    dish.setMenuType(menuType);
                    dish.setRawMaterials(rawMaterials);
                    dishRepository.update(dish);
                    PrintUtils.println("%nLe plat a bien été mis à jour.%n");
                } else {
                    PrintUtils.println("%nImpossible de modifier ce plat, car des commandes sont en cours");
                }
            } else {
                PrintUtils.println("%nUn plat du même nom existe déjà, annulation de la modification.%n");
            }
        }
    }

    public void updateDish() {
        List<Dish> dishes = getNotDailyMenuDishes();
        if (dishes.isEmpty())
            PrintUtils.println("Il n'y pas de plat modifiable.%n");
        else {
            PrintUtils.println("Voici la liste des plats que vous pouvez modifier :%n");
            PrintUtils.println(displayDishes(dishes));
            PrintUtils.print("Pour modifier l'un de ces plats, veuillez saisir son id : ");
            var dishId = askDishId(dishes);
            var dish = dishRepository.findById(dishId);
            if (dish.isPresent()) {
                updateDishInformation(dish.get());
            } else {
                PrintUtils.println("%nCe plat n'existe pas.%n");
            }
        }
    }

    protected void updateDishInformation(Dish dish) {
        PrintUtils.println(dish.toString());
        PrintUtils.print("Pour modifier le nom du plat, veuillez saisir un autre nom, sinon laissez le champ vide : ");
        var input = InputUtils.readInput();
        var dishName = input.length() == 0 ? dish.getDishName() : input;
        PrintUtils.print("Pour modifier la catégorie, veuillez saisir la nouvelle catégorie, sinon laissez le champ vide : ");
        input = InputUtils.readInput();
        var category = input.length() == 0 ? dish.getCategory() : input;
        PrintUtils.print("Voulez-vous modifier le type du menu ? [y]es ou [n]o : ");
        var choice = InputUtils.readInputConfirmation();
        var menuType = MenuTypes.ADULTES;
        if ((dish.getMenuType() == MenuTypes.ADULTES && choice.equals("y")) || (dish.getMenuType() == MenuTypes.ENFANTS && choice.equals("n"))) {
            menuType = MenuTypes.ENFANTS;
        }
        PrintUtils.print("Pour modifier le prix, veuillez saisir le nouveau prix, sinon entrez le prix initial : ");
        var price = InputUtils.readDoubleInputInRange(1.0, 51.0);
        var rawMaterials = dish.getRawMaterials();
        updateDishRawMaterials(rawMaterials);
        updateDishAddNewRawMaterials(rawMaterials);
        updateDish(dish, dishName, category, menuType, price, rawMaterials);
    }

    private void updateDishAddNewRawMaterials(Map<RawMaterial, Integer> rawMaterials) {
        var rmId = 1L;
        PrintUtils.println("%n| Ajout de nouveaux ingrédients%n");
        while (rmId != 0) {
            var rms = getRawMaterials();
            if (rms.isEmpty()) {
                PrintUtils.println("%nIl n'existe pas de matière première.%n");
            } else {
                PrintUtils.println(displayRawMaterials(rms));
                PrintUtils.print("Pour ajouter un nouvel ingrédient, veuillez entrer son id, sinon entrez 0 : ");
                rmId = askUpdateRMId(rms);
                if (rmId != 0) {
                    var rmAdd = rawMaterialRepository.findById(rmId);
                    if (rmAdd.isPresent()) {
                        PrintUtils.print("Entrez la quantité de cet ingrédient : ");
                        var quantityAdd = InputUtils.readIntegerInputInRange(1, 21);
                        rawMaterials.put(rmAdd.get(), quantityAdd);
                    } else {
                        PrintUtils.println("%nCette matière première n'existe pas.%n");
                    }
                }
            }
        }
    }

    private void updateDishRawMaterials(Map<RawMaterial, Integer> rawMaterials) {
        var rmId = 1L;
        PrintUtils.println("%n| Modification des ingrédients%n");
        while (rmId != 0 && !rawMaterials.isEmpty()) {
            var rms = getRawMaterials();
            if (rms.isEmpty()) {
                PrintUtils.println("%nIl n'existe pas de matière première.%n");
            } else {
                PrintUtils.println(displayRawMaterials(new ArrayList<>(rawMaterials.keySet())));
                PrintUtils.print("Pour modifier l'un des ingrédients veuillez saisir son id, sinon entrez 0 : ");
                rmId = askUpdateRMId(List.copyOf(rawMaterials.keySet()));
                if (rmId != 0) {
                    var rm = rawMaterialRepository.findById(rmId);
                    if (rm.isPresent()) {
                        PrintUtils.print("Pour modifier la quantité de cet ingrédient entrez la nouvelle valeur, sinon entrez 0 pour supprimer cet ingrédient : ");
                        var quantity = InputUtils.readIntegerInputInRange(0, 21);
                        if (quantity > 0) {
                            rawMaterials.put(rm.get(), quantity);
                        } else {
                            rawMaterials.remove(rm.get());
                        }
                    } else {
                        PrintUtils.println("%nCette matière première n'existe pas.%n");
                    }
                }
            }
        }
    }

    public void deleteDish(Dish dish){
        List<Order> orders = orderRepository.findByDish(dish.getDishId());
        if (orders.isEmpty()){
            dishRepository.delete(dish.getDishId());
            PrintUtils.println("%nLe plat a bien été supprimé.%n");
        } else {
            PrintUtils.println("%nLe plat ne peut pas être supprimé car il y a des commandes en cours.%n");
        }
    }

    public void deleteDish() {
        var dishes = getNotDailyMenuDishes();
        if(dishes.isEmpty())
            PrintUtils.println("Il n'y a pas de plat pouvant être supprimé.%n");
        else {
            PrintUtils.println(displayDishes(dishes));
            PrintUtils.print("Pour supprimer un repas veuillez entrez son id, sinon entrez 0 : ");
            var dishId = askDeleteDishId(dishes);
            if (dishId != 0) {
                var dish = dishRepository.findById(dishId);
                if (dish.isPresent()) {
                    deleteDish(dish.get());
                } else {
                    PrintUtils.println("%nCe plat n'existe pas.%n");
                }
            }
        }
    }

    @Override
    public void callAction(int action) {
        PrintUtils.println();
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
                PrintUtils.print(displayOrdersQueue());
                break;
            case 6:
                PrintUtils.print(displayDailyMenu());
                break;
            case 7:
                PrintUtils.print(displayStock());
                break;
            case 8:
                PrintUtils.println(averagePreparationTime());
                break;
            default:
                break;
        }
    }
}
