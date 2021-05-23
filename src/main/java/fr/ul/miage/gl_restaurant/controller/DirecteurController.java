package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.constants.Units;
import fr.ul.miage.gl_restaurant.model.Dish;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.model.RawMaterial;

import fr.ul.miage.gl_restaurant.model.User;
import fr.ul.miage.gl_restaurant.repository.*;
import fr.ul.miage.gl_restaurant.utilities.InputUtils;

import fr.ul.miage.gl_restaurant.repository.DishRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.OrderRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.RawMaterialRepositoryImpl;

import fr.ul.miage.gl_restaurant.utilities.PrintUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;


import javax.management.relation.Role;
import java.util.*;


public class DirecteurController extends UserController {

    private final DishRepositoryImpl dishRepository;
    private final RawMaterialRepositoryImpl rawMaterialRepository;
    private final OrderRepositoryImpl orderRepository;
    private final StockController stockController;
    private final UserRepositoryImpl userRepository;
    private final TableRepositoryImpl tableRepository;

    /**
     * ACTION DE L'UTILISATEUR
     */
    private static final String ACTION_1 = "1 : Connaître le profit des plats";

    public DirecteurController(Authentification auth) {
        super(auth);
        this.dishRepository = DishRepositoryImpl.getInstance();
        this.rawMaterialRepository = RawMaterialRepositoryImpl.getInstance();
        this.orderRepository = OrderRepositoryImpl.getInstance();
        this.userRepository = UserRepositoryImpl.getInstance();
        this.tableRepository = TableRepositoryImpl.getInstance();
        stockController = new StockController();
        this.actions.addAll(Arrays.asList(
            ACTION_1
        ));
    }

    protected Long askUserId(List<User> users) {
        return Long.parseLong(
                InputUtils.readInputInArray(
                        users.stream().map(u -> u.getUserId().toString()).collect(Collectors.toList())
                )
        );
    }

    protected String displayRoles(Roles[] roles){
        var i = 1;
        var stringBuilder = new TextStringBuilder();
        for (Roles role : roles) {
            stringBuilder.appendln("[%d] %s", i++, role.getRole());
        }
        return stringBuilder.toString();
    }

    protected String displayUsers(List<User> users){
        var stringBuilder = new TextStringBuilder();
        users.forEach(user -> stringBuilder.appendln("[%d] %s", user.getUserId(), user.getLogin()));
        return stringBuilder.toString();
    }

    protected String displayUser(User user){
        var stringBuilder = new TextStringBuilder();
        stringBuilder.appendln("-".repeat(20))
                .appendln("|" + StringUtils.center("User N°" + user.getUserId(), 18) + "|")
                .appendln("-".repeat(20))
                .appendln("Login : %s", user.getLogin())
                .appendln("Nom : %s", user.getLastName())
                .appendln("Prénom : %s", user.getFirstName())
                .appendln("Role : ", user.getRole().toString());
        return stringBuilder.toString();
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

    public String addUser(String login, String lastname, String firstname, Roles role){
        var user = userRepository.findByLogin(login);
        if (user.isEmpty()){
            userRepository.save(new User(login,firstname,lastname,role));
            return "Le compte utilisateur a bien été créé";
        }else{
            return "Le compte utilisateur n'a pas pu être créé";
        }
    }

    public String addUser(){
        PrintUtils.print("Veuillez saisir le login de l'utilisateur : ");
        var login = InputUtils.readInput();
        PrintUtils.print("Veuillez saisir le nom de l'utilisateur : ");
        var lastname = InputUtils.readInput();
        PrintUtils.print("Veuillez saisir le prénom de l'utilisateur");
        var firstname = InputUtils.readInput();
        var roles = Roles.values();
        PrintUtils.println("Voici la liste des roles disponibles :");
        PrintUtils.println(displayRoles(roles));
        PrintUtils.print("Veuillez sélectionner l'un de ces rôles en saisissant le numéro correspondant : ");
        var role = roles[InputUtils.readIntegerInputInRange(1,roles.length)];
        return addUser(login,lastname,firstname,role);
    }

    public String updateUser(User user, String login, String lastname, String firstname, Roles role){
        var userTemp = userRepository.findByLogin(login);
        if (userTemp.isEmpty() || user.getLogin().equals(login)){
            user.setLogin(login);
            user.setLastName(lastname);
            user.setFirstName(firstname);
            user.setRole(role);
            userRepository.update(user);
            return "Le compte utilisateur a bien été modifié";
        }else{
            return "Le compte utilisateur n'a pas pu être modifié";
        }
    }

    public String updateUser(){
        PrintUtils.println("Voici la liste des utilisateurs");
        var users = userRepository.findAll();
        PrintUtils.println(displayUsers(users));
        PrintUtils.print("Pour modifier l'un de ses utilisateur, veuillez saisir l'id correspondant : ");
        var userId = askUserId(users);
        var user = userRepository.findById(userId).get();
        PrintUtils.println(displayUser(user));
        PrintUtils.print("Veuillez saisir le login de l'utilisateur : ");
        var login = InputUtils.readInput();
        PrintUtils.print("Veuillez saisir le nom de l'utilisateur : ");
        var lastname = InputUtils.readInput();
        PrintUtils.print("Veuillez saisir le prénom de l'utilisateur");
        var firstname = InputUtils.readInput();
        var roles = Roles.values();
        PrintUtils.println("Voici la liste des roles disponibles :");
        PrintUtils.println(displayRoles(roles));
        PrintUtils.print("Veuillez sélectionner l'un de ces rôles en saisissant le numéro correspondant : ");
        var role = roles[InputUtils.readIntegerInputInRange(1,roles.length)];
        return updateUser(user,login,lastname,firstname,role);
    }

    public String deleteUser(User user){
        var tables = tableRepository.findByUserId(user.getUserId());
        if(tables.size() == 0){
            userRepository.delete(user.getUserId());
            return "Le compte utilisateur a bien été supprimé";
        }else{
            return "Le compte n'a pas pu être supprimé";
        }
    }

    public String deleteUser(){
        PrintUtils.println("Voici la liste des utilisateurs");
        var users = userRepository.findAll();
        PrintUtils.println(displayUsers(users));
        PrintUtils.print("Pour supprimer l'un de ses utilisateur, veuillez saisir l'id correspondant : ");
        var userId = askUserId(users);
        var user = userRepository.findById(userId).get();
        return deleteUser(user);
    }

    public String displayUser(){
        PrintUtils.println("Voici la liste des utilisateurs");
        var users = userRepository.findAll();
        PrintUtils.println(displayUsers(users));
        PrintUtils.print("Pour afficher les informations d'un utilisateur, veuillez saisir l'id correspondant : ");
        var userId = askUserId(users);
        var user = userRepository.findById(userId).get();
        return displayUser(user);

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
