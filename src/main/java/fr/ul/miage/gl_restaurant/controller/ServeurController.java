package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.*;
import fr.ul.miage.gl_restaurant.repository.*;
import fr.ul.miage.gl_restaurant.utilities.InputUtils;
import fr.ul.miage.gl_restaurant.utilities.PrintUtils;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServeurController extends UserController {

    @Getter
    private Order order;

    private final OrderRepositoryImpl orderRepository = OrderRepositoryImpl.getInstance();
    private final TableRepositoryImpl tableRepository = TableRepositoryImpl.getInstance();
    private final RawMaterialRepositoryImpl rawMaterialRepository = RawMaterialRepositoryImpl.getInstance();
    private final DishRepositoryImpl dishRepository = DishRepositoryImpl.getInstance();
    private final MealRepositoryImpl mealRepository = MealRepositoryImpl.getInstance();

    /**
     * ACTIONS DE L'UTILISATEUR
     */
    private static final String ACTION_1 = "1 : Afficher les tables qui m'ont été affectées ainsi que leur état";
    private static final String ACTION_2 = "2 : Gérer une table";

    private final Set<String> subActions;

    /**
     * SOUS-ACTIONS POUR UNE TABLE
     */
    private static final String SUB_ACTION_0 = "0 : Retour";
    private static final String SUB_ACTION_1 = "1 : Afficher les informations de la table";
    private static final String SUB_ACTION_2 = "2 : Ajouter un article à la table";
    private static final String SUB_ACTION_3 = "3 : Valider et transmettre la commande en cours à la cuisine";
    private static final String SUB_ACTION_4 = "4 : Servir la commande";

    public ServeurController(Authentification auth) {
        super(auth);
        this.actions.addAll(Arrays.asList(ACTION_1, ACTION_2));
        subActions = new LinkedHashSet<>();
        subActions.addAll(Arrays.asList(
                SUB_ACTION_0,
                SUB_ACTION_1,
                SUB_ACTION_2,
                SUB_ACTION_3,
                SUB_ACTION_4)
        );
    }

    protected String displayOrderRecap(Order order) {
        var stringBuilder = new TextStringBuilder();
        order.getDishes().forEach((k, v) ->
            stringBuilder.appendln("- %s (x%d)", k.getDishName(), v)
        );
        return stringBuilder.toString();
    }

    /**
     * Prend une commande et la sauvegarde en base de données.
     * @param order Commande à prendre.
     */
    protected boolean takeOrder(Order order) {
        rawMaterialRepository.updateStockBasedOnTakenOrder(order);
        order = orderRepository.save(order);
        if (order.getOrderId() != null) {
            this.order = null;
            return true;
        }
        return false;
    }

    protected void takeOrder() {
        if (this.order != null) {
            PrintUtils.println("-".repeat(50));
            PrintUtils.println(StringUtils.center("Récap. de la commande", 50));
            PrintUtils.println("-".repeat(50));
            PrintUtils.println(displayOrderRecap(this.order));
            PrintUtils.print("Voulez-vous transmettre la commande à la cuisine ? ([y]es ou [n]o) : ");
            String input = InputUtils.readInputConfirmation();
            if (input.equals("y")) {
                if (takeOrder(this.order)) {
                    PrintUtils.println("%nLa commande a bien été transmise à la cuisine.%n");
                } else {
                    PrintUtils.println("%nProblème lors de la validation de la commande.%n");
                }
            }
        } else {
            PrintUtils.println("%nIl n'y aucune commande existante pour cette table.%n");
        }
    }

    /**
     * Récupère la liste des tables d'un serveur.
     * @param user Serveur.
     * @return La liste des tables du serveur.
     */
    protected Set<Table> getTablesList(User user){
        Set<Table> tablesList = new HashSet<>();
        if (user.getRole().equals(Roles.SERVEUR)) {
            tablesList.addAll(tableRepository.findByUserId(user.getUserId()));
        }
        return tablesList;
    }

    /**
     * Indique que la commande a été servie par le serveur.
     * @param order Commande servie.
     */
    protected Order setOrderServed(Order order) {
        order.setServed(true);
        return orderRepository.update(order);
    }

    protected void addArticleToOrder(Meal meal, Dish dish, int quantity) {
        if (this.order == null) {
            this.order = new Order();
            this.order.setOrderDate(Timestamp.from(Instant.now()));
            this.order.setMeal(meal);
        }
        order.addDish(dish, quantity);
    }

    /**
     * Affiche toutes les tables affectées à un serveur ainsi que leur état.
     * @param tables Tables affectées au serveyr.
     * @return L'affichage des tables.
     */
    protected String displayServerTablesByFloor(List<Table> tables) {
        var stringBuilder = new TextStringBuilder();
        Map<Integer, Set<Table>> floors = getFloorsTables(tables);
        floors.forEach((k, v) -> {
            if (!v.isEmpty()) {
                stringBuilder.append("Étage %d : ", k);
                v.forEach(t -> {
                    if (t.getTableId() != null) {
                        stringBuilder.append("[n°%d - %s]", t.getTableId(), t.getState());
                    }
                });
                stringBuilder.appendNewLine();
            }
        });
        return stringBuilder.toString();
    }

    /**
     * Récupère les catégories des plats du menu du jour afin de les afficher lorsque le serveur veut ajouter un article.
     * @return La liste des catégories.
     */
    protected List<String> getDishesCategories() {
        List<Dish> dishes = dishRepository.findDailyMenu();
        return dishes.stream().map(Dish::getCategory).collect(Collectors.toList());
    }

    /**
     * Affiche les catégories des plats du menu du jour.
     * @param categories Catégories des plats.
     * @return Les catégories de manière plus visuelle.
     */
    protected String displayDishesCategories(List<String> categories) {
        var stringBuilder = new TextStringBuilder();
        stringBuilder.appendln("Catégories des articles :");
        IntStream.range(1, categories.size() + 1).forEach(i ->
                stringBuilder.appendln("\t%d. %s", i, categories.get(i-1)));
        return stringBuilder.toString();
    }

    /**
     * Affiche tous les plats d'une catégorie.
     * @param dishes Liste des plats de la catégorie.
     * @param category Catégorie.
     * @return Les plats de manière plus visuelle.
     */
    protected String displayDishesByCategory(List<Dish> dishes, String category) {
        var stringBuilder = new TextStringBuilder();
        stringBuilder.appendln("Liste des plats dans la catégorie '%s' :", category);
        IntStream.range(1, dishes.size() + 1).forEach(i ->
                stringBuilder.appendln("\t%d. %s", i, dishes.get(i-1).getDishName()));
        return stringBuilder.toString();
    }

    /**
     * Demande au serveur d'ajouter un article à la table.
     */
    protected void addArticle(Meal meal) {
        List<String> categories = getDishesCategories();
        if (!categories.isEmpty()) {
            PrintUtils.println(displayDishesCategories(categories));
            PrintUtils.print("Veuillez choisir le numéro de la catégorie : ");
            var categoryInput = InputUtils.readIntegerInputInRange(1, categories.size() + 1);
            String category = categories.get(categoryInput - 1);
            List<Dish> dishes = dishRepository.findByCategory(category);
            if (!dishes.isEmpty()) {
                PrintUtils.println();
                PrintUtils.println(displayDishesByCategory(dishes, category));
                PrintUtils.print("Veuillez choisir le numéro du plat à ajouter : ");
                var dishInput = InputUtils.readIntegerInputInRange(1, dishes.size() + 1);
                var dish = dishes.get(dishInput - 1);
                PrintUtils.print("Veuillez insérer la quantité de ce plat à ajouter : ");
                var quantity = InputUtils.readIntegerInputInRange(1, 10);
                addArticleToOrder(meal, dish, quantity);
                PrintUtils.println("%nL'article a bien été ajouté.%n");
            } else {
                PrintUtils.println("%nIl n'existe aucun plat dans cette catégorie.%n");
            }
        } else {
            PrintUtils.println("%nVous ne pouvez pas ajouter d'article à cette table, il n'en existe aucun.");
            PrintUtils.println("Veuillez vérifier que vous avez ajouter des plats et qu'ils ont une catégorie.%n");
        }
    }

    /**
     * Regarde si la table est occupée ou s'il y a déjà une commande avant de pouvoir ajouter un article.
     */
    protected void handleAddArticle(Table table) {
        List<Meal> meals = mealRepository.findAll();
        Optional<Meal> meal = meals.stream().filter(m ->
                m.getTable().getTableId().equals(table.getTableId()) && m.getBill() == null).findFirst();

        if (table.getState().equals(TableStates.OCCUPEE) && meal.isPresent()) {
            Optional<Order> optionalOrder = orderRepository.findByMeal(meal.get().getMealId());
            if (optionalOrder.isEmpty()) {
                addArticle(meal.get());
            } else {
                PrintUtils.println("%nImpossible d'ajouter un article, une commande a déjà été prise.%n");
            }
        } else {
            PrintUtils.println("%nImpossible d'ajouter un article à cette table, il n'y a aucun client.%n");
        }
    }

    protected void serveOrder(Table table) {
        List<Meal> meals = mealRepository.findAll();
        Optional<Meal> meal = meals.stream().filter(m ->
                m.getTable().getTableId().equals(table.getTableId()) && m.getBill() == null).findFirst();

        if (table.getState().equals(TableStates.OCCUPEE) && meal.isPresent()) {
            Optional<Order> optionalOrder = orderRepository.findByMeal(meal.get().getMealId());
            if (optionalOrder.isPresent()) {
                if (optionalOrder.get().getPreparationDate() == null) {
                    PrintUtils.println("La commande de cette table n'a pas encore été préparée.%n");
                } else if (optionalOrder.get().isServed()) {
                    PrintUtils.println("Toutes les commandes de cette table ont déjà été servies.%n");
                } else {
                    setOrderServed(optionalOrder.get());
                    PrintUtils.println("La commande a bien été servie.%n");
                }
            } else {
                PrintUtils.println("Aucune commande à servir pour cette table.%n");
            }
        } else {
            PrintUtils.println("Impossible de servir une table inoccupée.%n");
        }
    }

    protected void callSubAction(int action, Table table) {
        PrintUtils.println();
        switch (action) {
            case 1:
                System.out.println(table);
                break;
            case 2:
                handleAddArticle(table);
                break;
            case 3:
                takeOrder();
                break;
            case 4:
                serveOrder(table);
                break;
            default:
                break;
        }
    }

    protected void handleTable() {
        Set<Table> tables = getTablesList(auth.getUser());
        if (tables.isEmpty()) {
            PrintUtils.println("Vous n'avez été assigné à aucune table.%n");
        } else {
            PrintUtils.println(displayServerTablesByFloor(List.copyOf(tables)));
            var tableId = askTableId(List.copyOf(tables));
            Optional<Table> table = tableRepository.findById(tableId);
            if (table.isPresent()) {
                PrintUtils.println("%n%s", displaySubActions(this.subActions));
                PrintUtils.print("Veuillez renseigner le numéro de l'action à effectuer : ");
                callSubAction(InputUtils.readIntegerInputInRange(0, subActions.size() + 1), table.get());
            } else {
                PrintUtils.println("%nCette table n'existe pas.");
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
                System.out.println(displayServerTablesByFloor(List.copyOf(getTablesList(auth.getUser()))));
                break;
            case 2:
                handleTable();
                break;
            default:
                break;
        }
    }
}
