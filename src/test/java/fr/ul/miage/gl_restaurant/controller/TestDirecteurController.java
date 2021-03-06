package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.MenuTypes;
import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.constants.Units;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.model.*;
import fr.ul.miage.gl_restaurant.repository.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestDirecteurController {

    @Spy
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
    void initializeBeforeEach(){
        rm1 = rawMaterialRepository.save(new RawMaterial("Riz", 100, Units.KG));
    }

    @Test
    @DisplayName("L'ingr??dient est bien ajout??")
    void testAddRawMaterialSucceed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        RawMaterial rm = directeurController.addRawMaterial("P??tes", 100, Units.KG);
        List<RawMaterial> result = rawMaterialRepository.findAll();
        rawMaterialRepository.delete(rm.getRawMaterialId());
        assertThat(result.size(), is(2));
    }

    @Test
    @DisplayName("L'ingr??dient n'est pas ajout?? car il y a une duplicat")
    void testAddRawMaterialFailed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.addRawMaterial("Riz", 100, Units.KG);
        List<RawMaterial> result = rawMaterialRepository.findAll();
        assertThat(result.size(), is(1));
    }

    @Test
    @DisplayName("L'ingr??dient est bien modifi??")
    void testUpdateRawMaterialSucceed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.updateRawMaterial(rm1, "P??tes", Units.KG);
        Optional<RawMaterial> result = rawMaterialRepository.findById(rm1.getRawMaterialId());
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getRawMaterialName(), is("P??tes"));
        assertThat(result.get().getUnit(), is(Units.KG));
    }

    @Test
    @DisplayName("L'ingr??dient n'est pas modifi?? car il y a un duplicat")
    void testUpdateRawMaterialFailedSameValue(){
        RawMaterial rm = rawMaterialRepository.save(new RawMaterial("P??tes", 100, Units.KG));
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.updateRawMaterial(rm1, "P??tes", Units.KG);
        Optional<RawMaterial> result = rawMaterialRepository.findById(rm1.getRawMaterialId());
        rawMaterialRepository.delete(rm.getRawMaterialId());
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getRawMaterialName(), is("Riz"));
        assertThat(result.get().getUnit(), is(Units.KG));
    }

    @Test
    @DisplayName("L'ingr??dient n'est pas modifi?? car il y a une commande en cours qui l'utilise")
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
        Dish dish = dishRepository.save(new Dish("Riz", "C??r??ales", MenuTypes.ADULTES, 1.0, true, rawMaterialHashMap));
        Map<Dish,Integer> dishIntegerMap = new HashMap<>();
        dishIntegerMap.put(dish,1);
        Order order = orderRepository.save(new Order(Timestamp.from(Instant.now()), meal, dishIntegerMap));
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.updateRawMaterial(rm1, "P??tes", Units.KG);
        Optional<RawMaterial> result = rawMaterialRepository.findById(rm1.getRawMaterialId());
        assertThat(result.isPresent(), is(true));
        orderRepository.delete(order.getOrderId());
        dishRepository.delete(dish.getDishId());
        mealRepository.delete(meal.getMealId());
        tableRepository.delete(table.getTableId());
        billRepository.delete(bill.getBillId());
        assertThat(result.get().getRawMaterialName(), is("Riz"));
        assertThat(result.get().getUnit(), is(Units.KG));
    }

    @Test
    @DisplayName("L'ingr??dient est bien supprim??")
    void testDeleteRawMaterialSucceed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.deleteRawMaterial(rm1);
        Optional<RawMaterial> result = rawMaterialRepository.findById(rm1.getRawMaterialId());
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    @DisplayName("L'ingr??dient n'est pas supprim?? car il y a un repas qui en a besoin")
    void testDeleteRawMaterialFailed(){
        Optional<RawMaterial> rawMaterial = rawMaterialRepository.findByName("Riz");
        assertThat(rawMaterial.isPresent(), is(true));
        HashMap<RawMaterial, Integer> rawMaterialHashMap = new HashMap<>();
        rawMaterialHashMap.put(rawMaterial.get(), 1);
        Dish dish = dishRepository.save(new Dish("Riz", "C??r??ales", MenuTypes.ADULTES, 1.0, true, rawMaterialHashMap));
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.deleteRawMaterial(rm1);
        Optional<RawMaterial> result = rawMaterialRepository.findById(rm1.getRawMaterialId());
        dishRepository.delete(dish.getDishId());
        assertThat(result.isEmpty(), is(false));
    }

    @Test
    @DisplayName("L'utilisateur est bien ajout??")
    void tesAddUserSucceed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.addUser("bouchev", "Bouch??", "Valentine", Roles.MAITRE_HOTEL);
        var res = userRepository.findAll();
        var user = userRepository.findByLogin("bouchev");
        assertThat(user.isPresent(), is(true));
        userRepository.delete(user.get().getUserId());
        assertThat(res.size(), is(6));
    }

    @Test
    @DisplayName("L'utilisateur n'est pas ajout?? car il y a d??j?? un utiisateur avec ce login")
    void tesAddUserFailed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.addUser("luct", "Luc", "Tristan", Roles.SERVEUR);
        var res = userRepository.findAll();
        var user = userRepository.findByLogin("luct").get();
        assertThat(user.getRole(), is(Roles.CUISINIER));
        assertThat(res.size(), is(5));
    }

    @Test
    @DisplayName("L'utilisateur est bien modifi??")
    void tesUpdateUserSucceed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        var user = userRepository.save(new User("bouchev", "Bouch??", "Valentine", Roles.MAITRE_HOTEL));
        directeurController.updateUser(user, "bouchev", "Bouch??", "Valentine", Roles.CUISINIER);
        var res = userRepository.findByLogin("bouchev").get();
        userRepository.delete(user.getUserId());
        assertThat(res.getRole(), is(Roles.CUISINIER));
    }

    @Test
    @DisplayName("L'utilisateur n'est pas modifi?? car il y a d??j?? un utiisateur avec ce login")
    void tesUpdateUserFailed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        var user = userRepository.save(new User("bouchev", "Bouch??", "Valentine", Roles.MAITRE_HOTEL));
        directeurController.updateUser(user, "luct", "Luc", "Thomas", Roles.SERVEUR);
        var res = userRepository.findByLogin("bouchev");
        userRepository.delete(user.getUserId());
        assertThat(res.isPresent(), is(true));
    }

    @Test
    @DisplayName("L'utilisateur est bien supprim??")
    void tesDeleteUserSucceed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        var user = userRepository.save(new User("bouchev", "Bouch??", "Valentine", Roles.MAITRE_HOTEL));
        directeurController.deleteUser(user);
        var res = userRepository.findByLogin("bouchev");
        userRepository.delete(user.getUserId());
        assertThat(res.isEmpty(), is(true));
    }

    @Test
    @DisplayName("L'utilisateur n'est pas supprim?? car l'utilisateur est affect?? ?? une table")
    void testDeleteUserFailed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        var user = userRepository.save(new User("bouchev", "Bouch??", "Valentine", Roles.SERVEUR));
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
    @DisplayName("G??n??re le profit de tous les plats")
    void verifyGenerateDishesProfitSucceed() {
        generateTwoOrders();
        Map<Dish, Double> profits = directeurController.generateDishesProfits();
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
        expected.appendln("- TESTDISH2 : 30.60???")
                .appendln("- TESTDISH : 25.00???")
                .appendln("- TESTDISH3 : 0.00???");
        assertThat(directeurController.displayDishesProfit(), equalTo(expected.toString()));
        deleteAllOrders();
    }

    @Test
    @DisplayName("Les profits du d??jeuner et du d??ner sont corrects")
    void verifyGenerateMealsProfitSucceed() {
        Table table = tableRepository.save(new Table(2, TableStates.LIBRE, 4, null));
        Bill newBill1 = billRepository.save(new Bill(50.0, true));
        Bill newBill2 = billRepository.save(new Bill(50.0, false));
        Bill newBill3 = billRepository.save(new Bill(100.0, true));
        Bill newBill4 = billRepository.save(new Bill(100.0, true));
        Bill newBill5 = billRepository.save(new Bill(40.0, true));
        Meal newMeal1 = mealRepository.save(new Meal(2, Timestamp.valueOf("2021-05-21 12:00:00"), 10L, table, newBill1));
        Meal newMeal2 = mealRepository.save(new Meal(2, Timestamp.valueOf("2021-05-21 14:00:00"), 10L, table, newBill2));
        Meal newMeal3 = mealRepository.save(new Meal(2, Timestamp.valueOf("2021-05-21 14:00:00"), 10L, table, newBill3));
        Meal newMeal4 = mealRepository.save(new Meal(2, Timestamp.valueOf("2021-05-21 18:00:00"), 10L, table, newBill4));
        Meal newMeal5 = mealRepository.save(new Meal(2, Timestamp.valueOf("2021-05-21 19:00:00"), 10L, table, newBill5));

        double[] profits = directeurController.generateMealsProfits();
        assertThat(profits.length, is(2));
        assertThat(profits[0], is(150.0));
        assertThat(profits[1], is(140.0));

        mealRepository.delete(newMeal1.getMealId());
        mealRepository.delete(newMeal2.getMealId());
        mealRepository.delete(newMeal3.getMealId());
        mealRepository.delete(newMeal4.getMealId());
        mealRepository.delete(newMeal5.getMealId());
        tableRepository.delete(table.getTableId());
        billRepository.delete(newBill1.getBillId());
        billRepository.delete(newBill2.getBillId());
        billRepository.delete(newBill3.getBillId());
        billRepository.delete(newBill4.getBillId());
        billRepository.delete(newBill5.getBillId());
    }

    @Test
    @DisplayName("Les profits du d??jeuner et du d??ner sont ??gaux ?? 0")
    void verifyGenerateMealsProfitFail() {
        double[] profits = directeurController.generateMealsProfits();
        assertThat(profits.length, is(2));
        assertThat(profits[0], is(0.0));
        assertThat(profits[1], is(0.0));
    }

    @Test
    @DisplayName("Les profits du jour sont corrects")
    void verifyGenerateDailyProfitSucceed() {
        // Dates
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = LocalDateTime.now().minus(1, ChronoUnit.DAYS);

        Table table = tableRepository.save(new Table(2, TableStates.LIBRE, 4, null));
        Bill newBill1 = billRepository.save(new Bill(50.0, true));
        Bill newBill2 = billRepository.save(new Bill(50.0, false));
        Bill newBill4 = billRepository.save(new Bill(100.0, true));
        Bill newBill5 = billRepository.save(new Bill(40.0, true));
        Meal newMeal1 = mealRepository.save(new Meal(2, Timestamp.valueOf(today), 10L, table, newBill1));
        Meal newMeal2 = mealRepository.save(new Meal(2, Timestamp.valueOf(today), 10L, table, newBill2));
        Meal newMeal4 = mealRepository.save(new Meal(2, Timestamp.valueOf(yesterday), 10L, table, newBill4));
        Meal newMeal5 = mealRepository.save(new Meal(2, Timestamp.valueOf(today), 10L, table, newBill5));

        double profits = directeurController.generateDailyProfits();
        assertThat(profits, is(90.0));

        mealRepository.delete(newMeal1.getMealId());
        mealRepository.delete(newMeal2.getMealId());
        mealRepository.delete(newMeal4.getMealId());
        mealRepository.delete(newMeal5.getMealId());
        tableRepository.delete(table.getTableId());
        billRepository.delete(newBill1.getBillId());
        billRepository.delete(newBill2.getBillId());
        billRepository.delete(newBill4.getBillId());
        billRepository.delete(newBill5.getBillId());
    }

    @Test
    @DisplayName("Les profits du jour sont nuls")
    void verifyGenerateDailyProfitFail() {
        double profits = directeurController.generateDailyProfits();
        assertThat(profits, is(0.0));
    }

    @Test
    @DisplayName("Les profits de la semaine sont corrects")
    void verifyGenerateWeeklyProfitSucceed() {
        //Dates
        var dayOfWeek = LocalDate.of(2021, 5, 27);
        var startOfWeek = LocalDate.of(2021, 5, 24);
        var beforeStartOfWeek = LocalDate.of(2021, 5, 23);
        var may25 = LocalDate.of(2021, 5, 25);

        Table table = tableRepository.save(new Table(2, TableStates.LIBRE, 4, null));
        Bill newBill1 = billRepository.save(new Bill(50.0, true));
        Bill newBill2 = billRepository.save(new Bill(50.0, false));
        Bill newBill3 = billRepository.save(new Bill(100.0, true));
        Bill newBill4 = billRepository.save(new Bill(100.0, true));
        Bill newBill5 = billRepository.save(new Bill(40.0, true));
        Meal newMeal1 = mealRepository.save(new Meal(2, Timestamp.valueOf(dayOfWeek.atStartOfDay()), 10L, table, newBill1));
        Meal newMeal2 = mealRepository.save(new Meal(2, Timestamp.valueOf(dayOfWeek.atStartOfDay()), 10L, table, newBill2));
        Meal newMeal3 = mealRepository.save(new Meal(2, Timestamp.valueOf(startOfWeek.atStartOfDay()), 10L, table, newBill3));
        Meal newMeal4 = mealRepository.save(new Meal(2, Timestamp.valueOf(may25.atStartOfDay()), 10L, table, newBill4));
        Meal newMeal5 = mealRepository.save(new Meal(2, Timestamp.valueOf(beforeStartOfWeek.atStartOfDay()), 10L, table, newBill5));

        double profits = directeurController.generateWeeklyProfits();
        assertThat(profits, is(250.0));

        mealRepository.delete(newMeal1.getMealId());
        mealRepository.delete(newMeal2.getMealId());
        mealRepository.delete(newMeal3.getMealId());
        mealRepository.delete(newMeal4.getMealId());
        mealRepository.delete(newMeal5.getMealId());
        tableRepository.delete(table.getTableId());
        billRepository.delete(newBill1.getBillId());
        billRepository.delete(newBill2.getBillId());
        billRepository.delete(newBill3.getBillId());
        billRepository.delete(newBill4.getBillId());
        billRepository.delete(newBill5.getBillId());
    }

    @Test
    @DisplayName("Les profits du mois sont corrects")
    void verifyGenerateMonthlyProfitSucceed() {
        //Dates
        var dayOfMonth = LocalDate.of(2021, 5, 5);
        var startOfMonth = LocalDate.of(2021, 5, 1);
        var beforeStartOfMonth = LocalDate.of(2021, 4, 30);
        var may4 = LocalDate.of(2021, 5, 4);

        Table table = tableRepository.save(new Table(2, TableStates.LIBRE, 4, null));
        Bill newBill1 = billRepository.save(new Bill(50.0, true));
        Bill newBill2 = billRepository.save(new Bill(50.0, false));
        Bill newBill3 = billRepository.save(new Bill(100.0, true));
        Bill newBill4 = billRepository.save(new Bill(100.0, true));
        Bill newBill5 = billRepository.save(new Bill(40.0, true));
        Meal newMeal1 = mealRepository.save(new Meal(2, Timestamp.valueOf(dayOfMonth.atStartOfDay()), 10L, table, newBill1));
        Meal newMeal2 = mealRepository.save(new Meal(2, Timestamp.valueOf(dayOfMonth.atStartOfDay()), 10L, table, newBill2));
        Meal newMeal3 = mealRepository.save(new Meal(2, Timestamp.valueOf(startOfMonth.atStartOfDay()), 10L, table, newBill3));
        Meal newMeal4 = mealRepository.save(new Meal(2, Timestamp.valueOf(may4.atStartOfDay()), 10L, table, newBill4));
        Meal newMeal5 = mealRepository.save(new Meal(2, Timestamp.valueOf(beforeStartOfMonth.atStartOfDay()), 10L, table, newBill5));

        double profits = directeurController.generateMonthlyProfits();
        assertThat(profits, is(250.0));

        mealRepository.delete(newMeal1.getMealId());
        mealRepository.delete(newMeal2.getMealId());
        mealRepository.delete(newMeal3.getMealId());
        mealRepository.delete(newMeal4.getMealId());
        mealRepository.delete(newMeal5.getMealId());
        tableRepository.delete(table.getTableId());
        billRepository.delete(newBill1.getBillId());
        billRepository.delete(newBill2.getBillId());
        billRepository.delete(newBill3.getBillId());
        billRepository.delete(newBill4.getBillId());
        billRepository.delete(newBill5.getBillId());
    }

    @Test
    @DisplayName("Les profits du mois sont nuls")
    void verifyGenerateMonthlyProfitFail() {
        double profits = directeurController.generateMonthlyProfits();
        assertThat(profits, is(0.0));
    }

    @Test
    @DisplayName("Les profits globaux sont corrects")
    void verifyGenerateGlobalProfitsSucceed() {
        Table table = tableRepository.save(new Table(2, TableStates.LIBRE, 4, null));
        Bill newBill1 = billRepository.save(new Bill(50.0, true));
        Bill newBill2 = billRepository.save(new Bill(50.0, false));
        Bill newBill3 = billRepository.save(new Bill(100.0, true));
        Bill newBill4 = billRepository.save(new Bill(100.0, true));
        Bill newBill5 = billRepository.save(new Bill(40.0, true));
        Meal newMeal1 = mealRepository.save(new Meal(2, Timestamp.valueOf("2021-05-21 12:00:00"), 10L, table, newBill1));
        Meal newMeal2 = mealRepository.save(new Meal(2, Timestamp.valueOf("2021-05-21 14:00:00"), 10L, table, newBill2));
        Meal newMeal3 = mealRepository.save(new Meal(2, Timestamp.valueOf("2021-05-21 14:00:00"), 10L, table, newBill3));
        Meal newMeal4 = mealRepository.save(new Meal(2, Timestamp.valueOf("2021-05-21 18:00:00"), 10L, table, newBill4));
        Meal newMeal5 = mealRepository.save(new Meal(2, Timestamp.valueOf("2021-05-21 19:00:00"), 10L, table, newBill5));

        double profits = directeurController.generateGlobalProfits();
        assertThat(profits, is(290.0));

        mealRepository.delete(newMeal1.getMealId());
        mealRepository.delete(newMeal2.getMealId());
        mealRepository.delete(newMeal3.getMealId());
        mealRepository.delete(newMeal4.getMealId());
        mealRepository.delete(newMeal5.getMealId());
        tableRepository.delete(table.getTableId());
        billRepository.delete(newBill1.getBillId());
        billRepository.delete(newBill2.getBillId());
        billRepository.delete(newBill3.getBillId());
        billRepository.delete(newBill4.getBillId());
        billRepository.delete(newBill5.getBillId());
    }

    @Test
    @DisplayName("Les profits globaux sont nuls")
    void verifyGenerateGlobalProfitsFail() {
        double profits = directeurController.generateGlobalProfits();
        assertThat(profits, is(0.0));
    }

    @Test
    @DisplayName("Analyse des ventes - pas de rentr??e d'argent")
    void verifyDisplaySalesAnalysisNoProfits() {
        var expected = new TextStringBuilder();

        when(directeurController.generateDailyProfits()).thenReturn(0.0);
        when(directeurController.generateWeeklyProfits()).thenReturn(0.0);
        when(directeurController.generateMonthlyProfits()).thenReturn(0.0);
        when(directeurController.generateMealsProfits()).thenReturn(new double[]{0.0, 0.0});
        when(directeurController.generateGlobalProfits()).thenReturn(0.0);

        expected.appendln("-".repeat(50))
                .appendln(StringUtils.center("Analyse des ventes", 50))
                .appendln("-".repeat(50))
                .appendNewLine()
                .appendln("Profits du jour : Pas encore de rentr??e d'argent aujourd'hui.")
                .appendln("Profits de la semaine : Pas encore de rentr??e d'argent cette semaine.")
                .appendln("Profits du mois : Pas encore de rentr??e d'argent ce mois.")
                .appendln("Profits globaux depuis l'ouverture du restaurant :")
                .appendln("\t- D??jeuner : Pas encore de rentr??e d'argent pour les d??jeuners.")
                .appendln("\t- D??ner : Pas encore de rentr??e d'argent pour les d??ners.")
                .appendln("\t- Total : Pas encore de rentr??e d'argent depuis l'ouverture du restaurant.");

        assertThat(directeurController.displaySalesAnalysis(), equalTo(expected.toString()));
    }

    @Test
    @DisplayName("Analyse des ventes - rentr??e d'argent")
    void verifyDisplaySalesAnalysisWithProfitsForEachFunction() {
        var expected = new TextStringBuilder();

        when(directeurController.generateDailyProfits()).thenReturn(5.0);
        when(directeurController.generateWeeklyProfits()).thenReturn(10.0);
        when(directeurController.generateMonthlyProfits()).thenReturn(30.5555);
        when(directeurController.generateMealsProfits()).thenReturn(new double[]{30.0, 0.5555});
        when(directeurController.generateGlobalProfits()).thenReturn(55.5555);

        expected.appendln("-".repeat(50))
                .appendln(StringUtils.center("Analyse des ventes", 50))
                .appendln("-".repeat(50))
                .appendNewLine()
                .appendln("Profits du jour : %.2f???", 5.0)
                .appendln("Profits de la semaine : %.2f???", 10.0)
                .appendln("Profits du mois : %.2f???", 30.5555)
                .appendln("Profits globaux depuis l'ouverture du restaurant :")
                .appendln("\t- D??jeuner : .%2f???", 30.0)
                .appendln("\t- D??ner : %.2f???", 0.5555)
                .appendln("\t- Total : %.2f???", 55.5555);

        assertThat(directeurController.displaySalesAnalysis(), equalTo(expected.toString()));
    }

    @AfterEach
    void tearDownAfterEach() {
        rawMaterialRepository.delete(rm1.getRawMaterialId());
    }
}
