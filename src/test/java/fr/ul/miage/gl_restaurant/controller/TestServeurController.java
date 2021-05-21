package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.MenuTypes;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Dish;
import fr.ul.miage.gl_restaurant.model.Meal;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.model.User;
import fr.ul.miage.gl_restaurant.repository.DishRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.MealRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.UserRepositoryImpl;
import org.apache.commons.text.TextStringBuilder;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class TestServeurController {

    static TableRepositoryImpl tableRepository;
    static UserRepositoryImpl userRepository;
    static ServeurController serveurController;
    static DishRepositoryImpl dishRepository;
    static MealRepositoryImpl mealRepository;
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
    @DisplayName("La commande prise par le serveur est sauvegardée en DB")
    void verifyTakeOrderSucceed() {
        // Rien pour le moment puisqu'on ne testerait qu'un appel à une autre méthode déjà testée.
        // À voir si la méthode évolue par la suite.
    }

    @Test
    @DisplayName("La liste des tables correspond à celle du serveur")
    void verifyGetTablesList() {
        // Rien pour le moment puisqu'on ne testerait qu'un appel à une autre méthode déjà testée.
        // À voir si la méthode évolue par la suite.
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
        boolean result = serveurController.addArticleToOrder(table2Floor1, dish1, 2);
        assertThat(result, is(true));
        assertThat(serveurController.getOrder().getMeal().getMealId(), equalTo(meal1.getMealId()));
    }

    @Test
    @DisplayName("addArticleToOrder() n'ajoute pas l'article car il n'y a pas de clients")
    void verifyAddArticleToOrderFail() {
        boolean result = serveurController.addArticleToOrder(table1Floor1, dish1, 2);
        assertThat(result, is(false));
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
