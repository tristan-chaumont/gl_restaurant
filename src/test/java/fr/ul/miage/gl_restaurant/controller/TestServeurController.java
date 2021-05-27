package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.MenuTypes;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.constants.Units;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.model.*;
import fr.ul.miage.gl_restaurant.repository.*;
import org.apache.commons.text.TextStringBuilder;
import org.junit.jupiter.api.*;
import org.mockito.internal.verification.Times;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestServeurController {

    static TableRepositoryImpl tableRepository;
    static UserRepositoryImpl userRepository;
    static ServeurController serveurController;
    static DishRepositoryImpl dishRepository;
    static MealRepositoryImpl mealRepository;
    static OrderRepositoryImpl orderRepository;
    static RawMaterialRepositoryImpl rawMaterialRepository;
    Table table1Floor1, table2Floor1, table1Floor2, table2Floor2;
    Meal meal1;
    Dish dish1, dish2;
    static User user;

    @BeforeAll
    static void initializeBeforeAll() {
        tableRepository = TableRepositoryImpl.getInstance();
        userRepository = UserRepositoryImpl.getInstance();
        dishRepository = DishRepositoryImpl.getInstance();
        mealRepository = MealRepositoryImpl.getInstance();
        orderRepository = OrderRepositoryImpl.getInstance();
        rawMaterialRepository = RawMaterialRepositoryImpl.getInstance();
        serveurController = new ServeurController(new Authentification());
        user = userRepository.findByLogin("chaumontt").orElse(null);
    }

    @BeforeEach
    void initializeBeforeEach() {
        table1Floor1 = new Table(1, TableStates.SALE, 4, user);
        table1Floor1 = tableRepository.save(table1Floor1);
        table2Floor1 = new Table(1, TableStates.OCCUPEE, 4, user);
        table2Floor1 = tableRepository.save(table2Floor1);
        table1Floor2 = new Table(2, TableStates.LIBRE, 4, user);
        tableRepository.save(table1Floor2);
        table2Floor2 = new Table(2, TableStates.RESERVEE, 4, null);
        tableRepository.save(table1Floor2);
        dish1 = new Dish("Saumon", "Poisson", MenuTypes.ADULTES, 8.5, true);
        dish2 = new Dish( "Steak Haché", "Viande", MenuTypes.ADULTES, 5.0, true);
        dish1 = dishRepository.save(dish1);
        dish2 = dishRepository.save(dish2);
        meal1 = new Meal(2, Timestamp.from(Instant.now()), table2Floor1);
        meal1 = mealRepository.save(meal1);
    }

    @Test
    @DisplayName("La commande est servir avec succès")
    void verifySetOrderServedSucceed() {
        Order order = new Order(Timestamp.from(Instant.now()), meal1, new HashMap<>());
        order = orderRepository.save(order);
        order = serveurController.setOrderServed(order);
        assertThat(order.isServed(), is(true));
        orderRepository.delete(order.getOrderId());
    }

    @Test
    @DisplayName("Affiche toutes les tables affectées à un serveur par étage")
    void verifyDisplayServerTablesByFloorSucceed() {
        var expected = new TextStringBuilder();
        expected.append("Étage 1 : ")
                .appendln("[n°%d - %s][n°%d - %s]", table1Floor1.getTableId(), table1Floor1.getState(), table2Floor1.getTableId(), table2Floor1.getState())
                .append("Étage 2 : ")
                .appendln("[n°%d - %s]", table1Floor2.getTableId(), table1Floor2.getState());
        String result = serveurController.displayServerTablesByFloor(tableRepository.findByUserId(user.getUserId()));
        assertThat(result, equalTo(expected.toString()));
    }

    @Test
    @DisplayName("Affiche une chaine vide s'il n'y a aucune table")
    void verifyDisplayTablesByFloorReturnsEmptyString() {
        String result = serveurController.displayServerTablesByFloor(Collections.emptyList());
        assertThat(result, equalTo(""));
    }

    @Test
    @DisplayName("Récupère les catégories des plats")
    void verifyGetDishesCategoriesGetsAllCategories() {
        List<String> categories = serveurController.getDishesCategories();
        assertThat(categories, hasItems(dish1.getCategory(), dish2.getCategory()));
    }

    @Test
    @DisplayName("Affiche les catégories des plats")
    void verifyDisplayDishesCategoriesSucceed() {
        var expected = new TextStringBuilder();
        expected.appendln("Catégories des articles :")
                .appendln("\t1. Poisson")
                .appendln("\t2. Viande");
        String actual = serveurController.displayDishesCategories(serveurController.getDishesCategories());
        assertThat(actual, equalTo(expected.toString()));
    }

    @Test
    @DisplayName("Affiche les plats d'une catégorie")
    void verifyDisplayDishesByCategorySucceed() {
        var expected = new TextStringBuilder();
        expected.appendln("Liste des plats dans la catégorie 'Poisson' :")
                .appendln("\t1. Saumon");
        String actual = serveurController.displayDishesByCategory(dishRepository.findByCategory("Poisson"), "Poisson");
        assertThat(actual, equalTo(expected.toString()));
    }

    @Test
    @DisplayName("addArticleToOrder() ajoute l'article à la commande de la table")
    void verifyAddArticleToOrderSucceed() {
        serveurController.addArticleToOrder(meal1, dish1, 2);
        assertThat(serveurController.getOrder().getMeal().getMealId(), equalTo(meal1.getMealId()));
    }

    @Test
    @DisplayName("Affiche le récap des plats de la commande")
    void verifyDisplayOrderRecap() {
        Order order = new Order();
        order.addDish(dish1, 3);
        order.addDish(dish2, 1);
        var expected1 = new TextStringBuilder();
        expected1.appendln("- Saumon (x3)")
            .appendln("- Steak Haché (x1)");
        var expected2 = new TextStringBuilder();
        expected2.appendln("- Steak Haché (x1)")
                .appendln("- Saumon (x3)");
        assertThat(serveurController.displayOrderRecap(order), anyOf(equalTo(expected1.toString()), equalTo(expected2.toString())));
    }

    @Test
    @DisplayName("N'affiche pas le récap des plats de la commande car il n'y en a pas")
    void verifyDisplayOrderRecapFail() {
        Order order = new Order();
        assertThat(serveurController.displayOrderRecap(order), equalTo(""));
    }

    @Test
    @DisplayName("takeOrder() sauvegarde la commande et change les stocks")
    void verifyTakeOrderSucceed() {
        Order order = new Order(Timestamp.from(Instant.now()), false, meal1);
        RawMaterial rawMaterial1 = new RawMaterial("Saumon", 100, Units.KG);
        RawMaterial rawMaterial2 = new RawMaterial("Pâtes", 100, Units.KG);
        Dish dish3 = new Dish("Saumon", "Poisson", MenuTypes.ADULTES, 5.0, true);
        Dish dish4 = new Dish("Pâtes", "Pâtes", MenuTypes.ADULTES, 5.0, true);
        rawMaterial1 = rawMaterialRepository.save(rawMaterial1);
        rawMaterial2 = rawMaterialRepository.save(rawMaterial2);
        dish3.addRawMaterial(rawMaterial1, 1);
        dish4.addRawMaterial(rawMaterial2, 2);
        dish3 = dishRepository.save(dish3);
        dish4 = dishRepository.save(dish4);
        order.addDish(dish3, 5);
        order.addDish(dish4, 5);

        boolean result = serveurController.takeOrder(order);
        assertNotNull(order.getOrderId());
        assertThat(result, is(true));

        orderRepository.delete(order.getOrderId());
        dishRepository.delete(dish3.getDishId());
        dishRepository.delete(dish4.getDishId());
        rawMaterialRepository.delete(rawMaterial1.getRawMaterialId());
        rawMaterialRepository.delete(rawMaterial2.getRawMaterialId());
    }

    @Test
    @DisplayName("getTableList retourne la liste des tables de l'utilisateur chaumontt")
    void verifyGetTablesListSucceed() {
        Set<Table> tables = serveurController.getTablesList(user);
        assertThat(tables.size(), is(3));
    }

    @Test
    @DisplayName("getTableList retourne une liste vide car l'utilisateur n'est pas un serveur")
    void verifyGetTablesListFail() {
        User tempUser = userRepository.findByLogin("luct").orElse(null);
        Set<Table> tables = serveurController.getTablesList(tempUser);
        assertThat(tables.isEmpty(), is(true));
    }

    @AfterEach
    void tearDownAfterEach() {
        mealRepository.delete(meal1.getMealId());
        tableRepository.delete(table1Floor1.getTableId());
        tableRepository.delete(table2Floor1.getTableId());
        tableRepository.delete(table1Floor2.getTableId());
        tableRepository.delete(table2Floor2.getTableId());
        dishRepository.delete(dish1.getDishId());
        dishRepository.delete(dish2.getDishId());
    }
}
