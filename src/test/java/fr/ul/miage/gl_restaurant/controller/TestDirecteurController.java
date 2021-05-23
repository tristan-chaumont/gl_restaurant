package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.MenuTypes;
import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.constants.Units;
import fr.ul.miage.gl_restaurant.model.*;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.repository.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

class TestDirecteurController {

    static DirecteurController directeurController;
    static RawMaterialRepositoryImpl rawMaterialRepository;
    static TableRepositoryImpl tableRepository;
    static UserRepositoryImpl userRepository;
    static MealRepositoryImpl mealRepository;
    static OrderRepositoryImpl orderRepository;
    static DishRepositoryImpl dishRepository;
    static BillRepositoryImpl billRepository;
    static RawMaterial rm1;
    Order order1, order2;
    RawMaterial rawMaterial;
    Dish dish1, dish2, dish3;
    Table table1, table2;
    Bill bill1, bill2;
    Meal meal1, meal2;

    @BeforeAll
    static void initializeBeforeAll() {
        rawMaterialRepository = RawMaterialRepositoryImpl.getInstance();
        tableRepository = TableRepositoryImpl.getInstance();
        userRepository = UserRepositoryImpl.getInstance();
        mealRepository = MealRepositoryImpl.getInstance();
        orderRepository = OrderRepositoryImpl.getInstance();
        dishRepository = DishRepositoryImpl.getInstance();
        billRepository = BillRepositoryImpl.getInstance();
        directeurController = new DirecteurController(new Authentification());
    }

    @BeforeEach
    void initiazeBeforeEach(){
        rm1 = rawMaterialRepository.save(new RawMaterial("Riz", 100, Units.KG));
    }

    @Test
    @DisplayName("L'ingrédient est bien ajouté")
    void testAddRawMaterialSucceed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        RawMaterial rm = directeurController.addRawMaterial("Pâtes", 100, Units.KG);
        List<RawMaterial> result = rawMaterialRepository.findAll();
        rawMaterialRepository.delete(rm.getRawMaterialId());
        assertThat(result.size(), is(2));
    }

    @Test
    @DisplayName("L'ingrédient n'est pas ajouté car il y a une duplicat")
    void testAddRawMaterialFailed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.addRawMaterial("Riz", 100, Units.KG);
        List<RawMaterial> result = rawMaterialRepository.findAll();
        assertThat(result.size(), is(1));
    }

    @Test
    @DisplayName("L'ingrédient est bien modifié")
    void testUpdateRawMaterialSucceed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.updateRawMaterial(rm1, "Pâtes", 100, Units.KG);
        Optional<RawMaterial> result = rawMaterialRepository.findById(rm1.getRawMaterialId());
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getRawMaterialName(), is("Pâtes"));
        assertThat(result.get().getStockQuantity(), is(100));
        assertThat(result.get().getUnit(), is(Units.KG));
    }

    @Test
    @DisplayName("L'ingrédient n'est pas modifié car il y a un duplicat")
    void testUpdateRawMaterialFailedSameValue(){
        RawMaterial rm = rawMaterialRepository.save(new RawMaterial("Pâtes", 100, Units.KG));
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.updateRawMaterial(rm1, "Pâtes", 100, Units.KG);
        Optional<RawMaterial> result = rawMaterialRepository.findById(rm1.getRawMaterialId());
        rawMaterialRepository.delete(rm.getRawMaterialId());
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getRawMaterialName(), is("Riz"));
        assertThat(result.get().getStockQuantity(), is(100));
        assertThat(result.get().getUnit(), is(Units.KG));
    }

    @Test
    @DisplayName("L'ingrédient n'est pas modifié car il y a une commande en cours qui l'utilise")
    void testUpdateRawMaterialFailedOrder(){
        Optional<User> user = userRepository.findByLogin("chaumontt");
        assertThat(user.isPresent(), is(true));
        Table table = tableRepository.save(new Table(1, TableStates.LIBRE, 4, user.get()));
        Bill bill = billRepository.save(new Bill(1L));
        Meal meal = mealRepository.save(new Meal(4, Timestamp.valueOf("2021-04-27 12:00:00"), 30L, table , bill));
        Optional<RawMaterial> rawMaterial = rawMaterialRepository.findByName("Riz");
        assertThat(rawMaterial.isPresent(), is(true));
        HashMap<RawMaterial, Integer> rawMaterialHashMap = new HashMap<>();
        rawMaterialHashMap.put(rawMaterial.get(), 1);
        Dish dish = dishRepository.save(new Dish("Riz", "Céréales", MenuTypes.ADULTES, 1.0, true, rawMaterialHashMap));
        Map<Dish,Integer> dishIntegerMap = new HashMap<>();
        dishIntegerMap.put(dish,1);
        Order order = orderRepository.save(new Order(Timestamp.from(Instant.now()), meal, dishIntegerMap));
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.updateRawMaterial(rm1, "Pâtes", 100, Units.KG);
        Optional<RawMaterial> result = rawMaterialRepository.findById(rm1.getRawMaterialId());
        assertThat(result.isPresent(), is(true));
        orderRepository.delete(order.getOrderId());
        dishRepository.delete(dish.getDishId());
        mealRepository.delete(meal.getMealId());
        tableRepository.delete(table.getTableId());
        billRepository.delete(bill.getBillId());
        assertThat(result.get().getRawMaterialName(), is("Riz"));
        assertThat(result.get().getStockQuantity(), is(100));
        assertThat(result.get().getUnit(), is(Units.KG));
    }

    @Test
    @DisplayName("L'ingrédient est bien supprimé")
    void testDeleteRawMaterialSucceed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.deleteRawMaterial(rm1);
        Optional<RawMaterial> result = rawMaterialRepository.findById(rm1.getRawMaterialId());
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    @DisplayName("L'ingrédient n'est pas supprimé car il y a un repas qui en a besoin")
    void testDeleteRawMaterialFailed(){
        Optional<RawMaterial> rawMaterial = rawMaterialRepository.findByName("Riz");
        assertThat(rawMaterial.isPresent(), is(true));
        HashMap<RawMaterial, Integer> rawMaterialHashMap = new HashMap<>();
        rawMaterialHashMap.put(rawMaterial.get(), 1);
        Dish dish = dishRepository.save(new Dish("Riz", "Céréales", MenuTypes.ADULTES, 1.0, true, rawMaterialHashMap));
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.deleteRawMaterial(rm1);
        Optional<RawMaterial> result = rawMaterialRepository.findById(rm1.getRawMaterialId());
        dishRepository.delete(dish.getDishId());
        assertThat(result.isEmpty(), is(false));
    }

    @Test
    @DisplayName("L'utilisateur est bien ajouté")
    void tesAddUserSucceed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.addUser("bouchev", "Bouché", "Valentine", Roles.MAITRE_HOTEL);
        var res = userRepository.findAll();
        var user = userRepository.findByLogin("bouchev");
        userRepository.delete(user.get().getUserId());
        assertThat(user.isPresent(), is(true));
        assertThat(res.size(), is(6));
    }

    @Test
    @DisplayName("L'utilisateur n'est pas ajouté car il y a déjà un utiisateur avec ce login")
    void tesAddUserFailed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.addUser("luct", "Luc", "Tristan", Roles.SERVEUR);
        var res = userRepository.findAll();
        var user = userRepository.findByLogin("luct").get();
        assertThat(user.getRole(), is(Roles.CUISINIER));
        assertThat(res.size(), is(5));
    }

    @Test
    @DisplayName("L'utilisateur est bien modifié")
    void tesUpdateUserSucceed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        var user = userRepository.save(new User("bouchev", "Bouché", "Valentine", Roles.MAITRE_HOTEL));
        directeurController.updateUser(user, "bouchev", "Bouché", "Valentine", Roles.CUISINIER);
        var res = userRepository.findByLogin("bouchev").get();
        userRepository.delete(user.getUserId());
        assertThat(res.getRole(), is(Roles.CUISINIER));
    }

    @Test
    @DisplayName("L'utilisateur n'est pas modifié car il y a déjà un utiisateur avec ce login")
    void tesUpdateUserFailed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        var user = userRepository.save(new User("bouchev", "Bouché", "Valentine", Roles.MAITRE_HOTEL));
        directeurController.updateUser(user, "luct", "Luc", "Thomas", Roles.SERVEUR);
        var res = userRepository.findByLogin("bouchev");
        userRepository.delete(user.getUserId());
        assertThat(res.isPresent(), is(true));
    }

    @Test
    @DisplayName("L'utilisateur est bien supprimé")
    void tesDeleteUserSucceed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        var user = userRepository.save(new User("bouchev", "Bouché", "Valentine", Roles.MAITRE_HOTEL));
        directeurController.deleteUser(user);
        var res = userRepository.findByLogin("bouchev");
        userRepository.delete(user.getUserId());
        assertThat(res.isEmpty(), is(true));
    }

    @Test
    @DisplayName("L'utilisateur n'est pas supprimé car l'utilisateur est affecté à une table")
    void tesDeleteUserFailed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        var user = userRepository.save(new User("bouchev", "Bouché", "Valentine", Roles.SERVEUR));
        var table = tableRepository.save(new Table(1,TableStates.LIBRE, 4,user));
        directeurController.deleteUser(user);
        var res = userRepository.findByLogin("bouchev");
        tableRepository.delete(table.getTableId());
        userRepository.delete(user.getUserId());
        assertThat(res.isPresent(), is(true));
    }

    private void generateTwoOrders() {
        Optional<User> user = userRepository.findByLogin("chaumontt");
        if (user.isPresent()) {
            table1 = tableRepository.save(new Table(1, TableStates.SALE, 4, user.get()));
            table2 = tableRepository.save(new Table(1, TableStates.SALE, 2, user.get()));
            rawMaterial = rawMaterialRepository.save(new RawMaterial("TEST", 500, Units.U));
            dish1 = new Dish("TESTDISH", "TESTCATEGORY", MenuTypes.ADULTES, 5.0, true);
            dish1.addRawMaterial(rawMaterial, 1);
            dish1 = dishRepository.save(dish1);
            dish2 = new Dish("TESTDISH2", "TESTCATEGORY", MenuTypes.ADULTES, 3.4, true);
            dish2.addRawMaterial(rawMaterial, 2);
            dish2 = dishRepository.save(dish2);
            dish3 = new Dish("TESTDISH3", "TESTCATEGORY", MenuTypes.ADULTES, 5.1, true);
            dish3.addRawMaterial(rawMaterial, 2);
            dish3 = dishRepository.save(dish3);
            bill1 = billRepository.save(new Bill(33.6, true));
            bill2 = billRepository.save(new Bill(22, true));
            meal1 = mealRepository.save(new Meal(4, Timestamp.from(Instant.now()), table1, bill1));
            meal2 = mealRepository.save(new Meal(2 , Timestamp.from(Instant.now()), table2, bill2));
            order1 = new Order(Timestamp.from(Instant.now()), true, meal1);
            order2 = new Order(Timestamp.from(Instant.now()), true, meal2);
            order1.addDish(dish1, 4);
            order1.addDish(dish2, 4);
            order2.addDish(dish1, 1);
            order2.addDish(dish2, 5);
            order1 = orderRepository.save(order1);
            order2 = orderRepository.save(order2);
        }
    }

    private void deleteAllOrders() {
        orderRepository.delete(order1.getOrderId());
        orderRepository.delete(order2.getOrderId());
        dishRepository.delete(dish1.getDishId());
        dishRepository.delete(dish2.getDishId());
        dishRepository.delete(dish3.getDishId());
        rawMaterialRepository.delete(rawMaterial.getRawMaterialId());
        mealRepository.delete(meal1.getMealId());
        mealRepository.delete(meal2.getMealId());
        billRepository.delete(bill1.getBillId());
        billRepository.delete(bill2.getBillId());
        tableRepository.delete(table1.getTableId());
        tableRepository.delete(table2.getTableId());
    }

    @Test
    @DisplayName("Génère le profit de tous les plats")
    void verifyGenerateDishesProfitSucceed() {
        generateTwoOrders();
        Map<Dish, Double> profits = directeurController.generateDishesProfit();
        assertThat(profits, hasEntry(equalTo(dish1), equalTo(25.0)));
        assertThat(profits, hasEntry(equalTo(dish2), equalTo(30.6)));
        assertThat(profits, hasEntry(equalTo(dish3), equalTo(0.0)));
        deleteAllOrders();
    }

    @Test
    @DisplayName("Affiche le profit de tous les plats")
    void verifyDisplayDishesProfitSucceed() {
        generateTwoOrders();
        var expected = new TextStringBuilder();
        expected.appendln("- TESTDISH2 : 30.60€")
                .appendln("- TESTDISH : 25.00€")
                .appendln("- TESTDISH3 : 0.00€");
        assertThat(directeurController.displayDishesProfit(), equalTo(expected.toString()));
        deleteAllOrders();
    }

    @AfterEach
    void tearDownAfterEach() {
        rawMaterialRepository.delete(rm1.getRawMaterialId());
    }
}
