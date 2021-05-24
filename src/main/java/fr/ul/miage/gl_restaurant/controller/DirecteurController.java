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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DirecteurController extends UserController {

    private final DishRepositoryImpl dishRepository = DishRepositoryImpl.getInstance();
    private final RawMaterialRepositoryImpl rawMaterialRepository = RawMaterialRepositoryImpl.getInstance();
    private final OrderRepositoryImpl orderRepository = OrderRepositoryImpl.getInstance();
    private final StockController stockController;
    private final UserRepositoryImpl userRepository = UserRepositoryImpl.getInstance();
    private final TableRepositoryImpl tableRepository = TableRepositoryImpl.getInstance();

    /**
     * ACTION DE L'UTILISATEUR
     */
    private static final String ACTION_1 = "1 : Connaître le profit des plats";
    private static final String ACTION_2 = "2 : Gérer les matières premières";
    private static final String ACTION_3 = "3 : Restocker automatiquement les matières premières";
    private static final String ACTION_4 = "4 : Ajouter des plats au menu du jour";
    private static final String ACTION_5 = "5 : Retirer des plats du menu du jour";
    private static final String ACTION_6 = "6 : Gérer les utilisateurs";

    private static final String SUB_ACTION_0 = "0 : Retour";

    private final Set<String> rmSubActions;
    private final Set<String> usersSubActions;

    /**
     * SOUS-ACTIONS POUR LES MATIERES PREMIERES
     */
    private static final String RM_SUB_ACTION_1 = "1 : Créer une matière première";
    private static final String RM_SUB_ACTION_2 = "2 : Modifier une matière première";
    private static final String RM_SUB_ACTION_3 = "3 : Supprimer une matière première";

    /**
     * SOUS-ACTIONS POUR LES UTILISATEURS
     */
    private static final String USERS_SUB_ACTION_1 = "1 : Créer un utilisateur";
    private static final String USERS_SUB_ACTION_2 = "2 : Modifier un utilisateur";
    private static final String USERS_SUB_ACTION_3 = "3 : Supprimer un utilisateur";

    public DirecteurController(Authentification auth) {
        super(auth);
        stockController = new StockController();
        this.actions.addAll(Arrays.asList(
                ACTION_1,
                ACTION_2,
                ACTION_3,
                ACTION_4,
                ACTION_5,
                ACTION_6
        ));
        this.rmSubActions = new LinkedHashSet<>();
        this.rmSubActions.addAll(Arrays.asList(
                SUB_ACTION_0,
                RM_SUB_ACTION_1,
                RM_SUB_ACTION_2,
                RM_SUB_ACTION_3
        ));
        this.usersSubActions = new LinkedHashSet<>();
        this.usersSubActions.addAll(Arrays.asList(
                SUB_ACTION_0,
                USERS_SUB_ACTION_1,
                USERS_SUB_ACTION_2,
                USERS_SUB_ACTION_3
        ));
    }

    protected Long askUserId(List<User> users) {
        return Long.parseLong(
                InputUtils.readInputInArray(
                        users.stream().map(u -> u.getUserId().toString()).collect(Collectors.toList())
                )
        );
    }

    protected Long askRMId(List<RawMaterial> rawMaterials) {
        return Long.parseLong(
                InputUtils.readInputInArray(
                        rawMaterials.stream().map(rm -> rm.getRawMaterialId().toString()).collect(Collectors.toList())
                )
        );
    }

    protected String displayRoles(Roles[] roles){
        var i = 1;
        var stringBuilder = new TextStringBuilder();
        for (Roles role : roles) {
            stringBuilder.appendln("%d. %s", i++, role.getRole());
        }
        return stringBuilder.toString();
    }

    protected String displayUsers(List<User> users) {
        var stringBuilder = new TextStringBuilder();
        users.forEach(user -> stringBuilder.appendln("%d - %s %s (%s)", user.getUserId(), user.getFirstName(), user.getLastName(), user.getLogin()));
        return stringBuilder.toString();
    }

    protected String displayDishes(List<Dish> dishes) {
        var stringBuilder = new TextStringBuilder();
        IntStream.range(1, dishes.size() + 1).forEach(i ->
                stringBuilder.appendln("\t%d. %s", i, dishes.get(i-1).getDishName()));
        return stringBuilder.toString();
    }

    /**
     * Vérifie si le plat a encore assez de stocks de matières premières pour être préparé.
     * @param dish Plat.
     * @return True si le stock est suffisant, false sinon.
     */
    public boolean verifyIfEnoughStock(Dish dish) {
        var result = new AtomicBoolean(true);
        dish.getRawMaterials().forEach((rm, quantity) -> {
            // on récupère la matière première nécessaire au repas
            Optional<RawMaterial> rawMaterialUsed = rawMaterialRepository.findById(rm.getRawMaterialId());
            if (rawMaterialUsed.isPresent() && quantity > rm.getStockQuantity()) {
                result.set(false);
            }
        });
        return result.get();
    }

    protected void addDishToDailyMenu(Long dishId) {
        dishRepository.updateDailyMenu(dishId, true);
    }

    protected void addDishToDailyMenu() {
        var dishes = dishRepository.findNotDailyMenu().stream().filter(this::verifyIfEnoughStock).collect(Collectors.toList());
        if (dishes.isEmpty()) {
            PrintUtils.println("Aucun plat ne peut être ajouté au menu du jour.%n");
        } else {
            PrintUtils.println(displayDishes(dishes));
            PrintUtils.print("Veuillez sélectionner l'id du plat que vous souhaitez ajouter au menu du jour : ");
            var dishInput = InputUtils.readIntegerInputInRange(1, dishes.size() + 1);
            var dish = dishes.get(dishInput - 1);
            addDishToDailyMenu(dish.getDishId());
            PrintUtils.println("%nLe plat a bien été ajouté au menu du jour.%n");
        }
    }

    protected void removeDishFromDailyMenu(Long dishId) {
        dishRepository.updateDailyMenu(dishId, false);
    }

    protected void removeDishFromDailyMenu() {
        var dishes = dishRepository.findDailyMenu();
        if (dishes.isEmpty()) {
            PrintUtils.println("Il n'y aucun plat à retirer du menu du jour.%n");
        } else {
            PrintUtils.println(displayDishes(dishes));
            PrintUtils.print("Veuillez sélectionner l'id du plat que vous souhaitez retirer du menu du jour : ");
            var dishInput = InputUtils.readIntegerInputInRange(1, dishes.size() + 1);
            var dish = dishes.get(dishInput - 1);
            removeDishFromDailyMenu(dish.getDishId());
            PrintUtils.println("%nLe plat a bien été retiré du menu du jour.%n");
        }
    }

    public void restock() {
        stockController.restock();
        PrintUtils.println("Les stocks des matières première en rupture de stock ont bien été augmentés.%n");
    }

    protected String displayRawMaterials(List<RawMaterial> rawMaterials) {
        var stringBuilder = new TextStringBuilder();
        rawMaterials.forEach(rm -> stringBuilder.appendln("[%d] %s", rm.getRawMaterialId(), rm.getRawMaterialName()));
        return stringBuilder.toString();
    }

    public RawMaterial addRawMaterial(String rawMaterialName, Integer stockQuantity, Units unit){
        Optional<RawMaterial> result = rawMaterialRepository.findByName(rawMaterialName);
        RawMaterial rm = null;
        if(result.isEmpty()){
            rm = rawMaterialRepository.save(new RawMaterial(rawMaterialName, stockQuantity, unit));
            PrintUtils.println("%nLa matière première a bien été créée.%n");
        } else {
            PrintUtils.println("%nUne matère première du même nom existe déjà, annulation de la création.%n");
        }
        return rm;
    }

    protected void createRawMaterial() {
        PrintUtils.print("Veuillez saisir le nom de la matière première que vous voulez créer : ");
        var rmName = InputUtils.readInput();
        PrintUtils.print("Veuillez saisir l'unité ('kg', 'u' ou 'L') : ");
        var unit = InputUtils.readInputInArray(Arrays.asList("kg", "u", "L"));
        PrintUtils.print("Veuillez saisir la quantité de stock que vous possédez pour cette matière première : ");
        var stockQuantity = InputUtils.readIntegerInputInRange(50, 1001);
        addRawMaterial(rmName, stockQuantity, Units.getUnit(unit));
    }

    public void updateRawMaterial(RawMaterial rawMaterial, String rawMaterialName, Units unit){
        Optional<RawMaterial> result = rawMaterialRepository.findByName(rawMaterialName);
        if(result.isEmpty() || rawMaterialName.equals(rawMaterial.getRawMaterialName())) {
            List<Dish> dishes = dishRepository.findByRM(rawMaterial.getRawMaterialId());
            var updatable = true;
            for (Dish dish: dishes) {
                List<Order> orders = orderRepository.findByDish(dish.getDishId());
                if(!orders.isEmpty()) {
                    updatable = false;
                    break;
                }
            }
            if (updatable) {
                rawMaterial.setRawMaterialName(rawMaterialName);
                rawMaterial.setUnit(unit);
                rawMaterialRepository.update(rawMaterial);
                PrintUtils.println("%nLa matière première a bien été modifiée.%n");
            }
        } else {
            PrintUtils.println("%nUne matière première du même nom existe déjà, annulation de la modification.%n");
        }
    }

    protected void updateRawMaterialInformation(RawMaterial rawMaterial) {
        PrintUtils.println(rawMaterial.toString());
        PrintUtils.print("Pour modifier le nom de la matière première, veuillez saisir un autre nom, sinon laissez le champ vide : ");
        var input = InputUtils.readInput();
        var rmName = input.length() == 0 ? rawMaterial.getRawMaterialName() : input;
        PrintUtils.print("Pour modifier l'unité, veuillez faire votre choix entre 'kg', 'u' ou 'L', sinon laissez le champ vide : ");
        input = InputUtils.readInputInArray(Arrays.asList("kg", "u", "L", ""));
        var unit = input.length() == 0 ? rawMaterial.getUnit() : Units.getUnit(input);
        updateRawMaterial(rawMaterial, rmName, unit);
    }

    protected void updateRawMaterial() {
        var rawMaterials = rawMaterialRepository.findAll();
        PrintUtils.println(displayRawMaterials(rawMaterials));
        PrintUtils.print("Veuillez saisir l'id de la matière première à modifier : ");
        var rmId = askRMId(rawMaterials);
        PrintUtils.println();
        var rawMaterial = rawMaterialRepository.findById(rmId);
        if (rawMaterial.isPresent()) {
            updateRawMaterialInformation(rawMaterial.get());
        } else {
            PrintUtils.println("Cette matière première n'existe pas.%n");
        }
    }

    public void deleteRawMaterial(RawMaterial rawMaterial){
        List<Dish> dishes = dishRepository.findByRM(rawMaterial.getRawMaterialId());
        if(dishes.isEmpty()){
            rawMaterialRepository.delete(rawMaterial.getRawMaterialId());
            PrintUtils.println("%nLa matière première a bien été supprimée.%n");
        } else {
            PrintUtils.println("%nImpossible de supprimer une matière première qui appartient à un plat, supprimez la d'abord du plat.%n");
        }
    }

    protected void deleteRawMaterial() {
        var rawMaterials = rawMaterialRepository.findAll();
        if (rawMaterials.isEmpty()) {
            PrintUtils.println("%nIl n'existe aucune matière première.%n");
        } else {
            PrintUtils.println(displayRawMaterials(rawMaterials));
            PrintUtils.print("Veuillez entrer l'id de la matière première à supprimer : ");
            var rmId = askRMId(rawMaterials);
            var rawMaterial = rawMaterialRepository.findById(rmId);
            if (rawMaterial.isPresent()) {
                deleteRawMaterial(rawMaterial.get());
            } else {
                PrintUtils.println("%nCette matière première n'existe pas.%n");
            }
        }
    }

    public void addUser(String login, String lastname, String firstname, Roles role){
        var user = userRepository.findByLogin(login);
        if (user.isEmpty()){
            userRepository.save(new User(login,firstname,lastname,role));
            PrintUtils.println("%nLe compte utilisateur a bien été créé.%n");
        }else{
            PrintUtils.println("%nLe compte utilisateur n'a pas pu être créé.%n");
        }
    }

    public void addUser(){
        PrintUtils.print("Veuillez saisir le login de l'utilisateur : ");
        var login = InputUtils.readNonEmptyInput();
        PrintUtils.print("Veuillez saisir le nom de l'utilisateur : ");
        var lastname = InputUtils.readNonEmptyInput();
        PrintUtils.print("Veuillez saisir le prénom de l'utilisateur : ");
        var firstname = InputUtils.readNonEmptyInput();
        var roles = Roles.values();
        PrintUtils.println("%nListe des rôles disponibles :%n");
        PrintUtils.println(displayRoles(roles));
        PrintUtils.print("Veuillez sélectionner le numéro du rôle : ");
        var role = roles[InputUtils.readIntegerInputInRange(1, roles.length + 1) - 1];
        addUser(login,lastname,firstname,role);
    }

    public void updateUser(User user, String login, String lastname, String firstname, Roles role){
        var userTemp = userRepository.findByLogin(login);
        if (userTemp.isEmpty() || user.getLogin().equals(login)){
            user.setLogin(login);
            user.setLastName(lastname);
            user.setFirstName(firstname);
            user.setRole(role);
            userRepository.update(user);
            PrintUtils.println("%nLe compte utilisateur a bien été modifié.%n");
        }else{
            PrintUtils.println("%nLe compte utilisateur n'a pas pu être modifié.%n");
        }
    }

    protected void updateUserInformation(User user) {
        PrintUtils.println("%n%s", user.toString());
        PrintUtils.print("Veuillez saisir le login de l'utilisateur ou laissez le champ vide : ");
        var input = InputUtils.readInput();
        var login = input.length() == 0 ? user.getLogin() : input;
        PrintUtils.print("Veuillez saisir le nom de l'utilisateur ou laissez le champ vide : ");
        input = InputUtils.readInput();
        var lastname = input.length() == 0 ? user.getLastName() : input;
        PrintUtils.print("Veuillez saisir le prénom de l'utilisateur ou laissez le champ vide : ");
        input = InputUtils.readInput();
        var firstname = input.length() == 0 ? user.getFirstName() : input;
        var tables = tableRepository.findByUserId(user.getUserId());
        var role = user.getRole();
        if(role != Roles.SERVEUR || tables.isEmpty()) {
            var roles = Roles.values();
            PrintUtils.println("%nListe des rôles disponibles :%n");
            PrintUtils.println(displayRoles(roles));
            PrintUtils.print("Veuillez sélectionner le numéro du rôle : ");
            role = roles[InputUtils.readIntegerInputInRange(1, roles.length + 1) - 1];
        }
        updateUser(user, login, lastname, firstname, role);
    }

    public void updateUser(){
        var users = userRepository.findAll();
        PrintUtils.println(displayUsers(users));
        PrintUtils.print("Pour modifier l'un des utilisateurs, veuillez saisir son id : ");
        var userId = askUserId(users);
        var user = userRepository.findById(userId);
        if (user.isPresent()) {
            updateUserInformation(user.get());
        } else {
            PrintUtils.println("Cet utilisateur n'existe pas.%n");
        }
    }

    public void deleteUser(User user){
        var tables = tableRepository.findByUserId(user.getUserId());
        if(tables.isEmpty()){
            userRepository.delete(user.getUserId());
            PrintUtils.println("%nLe compte utilisateur a bien été supprimé.%n");
        }else{
            PrintUtils.println("%nLe compte utilisateur n'a pas pu être supprimé.%n");
        }
    }

    public void deleteUser() {
        var users = userRepository.findAll();
        PrintUtils.println(displayUsers(users));
        PrintUtils.print("Pour supprimer l'un des utilisateurs, veuillez saisir son id : ");
        var userId = askUserId(users);
        var user = userRepository.findById(userId);
        if (user.isPresent()) {
            deleteUser(user.get());
        } else {
            PrintUtils.println("Cet utilisateur n'existe pas.%n");
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
            if (meal.getBill() != null && meal.getBill().isPaid()) {
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

    protected void handleUsers() {
        PrintUtils.println(displaySubActions(this.usersSubActions));
        PrintUtils.print("Veuillez renseigner le numéro de l'action à effectuer : ");
        callUsersSubActions(InputUtils.readIntegerInputInRange(0, this.usersSubActions.size() + 1));
    }

    public void callUsersSubActions(int subAction) {
        PrintUtils.println();
        switch (subAction) {
            case 1:
                addUser();
                break;
            case 2:
                updateUser();
                break;
            case 3:
                deleteUser();
                break;
            default:
                break;
        }
    }

    protected void handleRawMaterials() {
        PrintUtils.println(displaySubActions(this.rmSubActions));
        PrintUtils.print("Veuillez renseigner le numéro de l'action à effectuer : ");
        callRMSubActions(InputUtils.readIntegerInputInRange(0, this.rmSubActions.size() + 1));
    }

    public void callRMSubActions(int subAction) {
        PrintUtils.println();
        switch (subAction) {
            case 1:
                createRawMaterial();
                break;
            case 2:
                updateRawMaterial();
                break;
            case 3:
                deleteRawMaterial();
                break;
            default:
                break;
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
                PrintUtils.println(displayDishesProfit());
                break;
            case 2:
                handleRawMaterials();
                break;
            case 3:
                restock();
                break;
            case 4:
                addDishToDailyMenu();
                break;
            case 5:
                removeDishFromDailyMenu();
                break;
            case 6:
                handleUsers();
                break;
            default:
                break;
        }
    }
}
