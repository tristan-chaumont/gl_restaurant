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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class TestCuisineController {

    static MealRepositoryImpl mealRepository;
    static OrderRepositoryImpl orderRepository;
    static TableRepositoryImpl tableRepository;
    static BillRepositoryImpl billRepository;
    static UserRepositoryImpl userRepository;
    static RawMaterialRepositoryImpl rawMaterialRepository;
    static DishRepositoryImpl dishRepository;
    static User user;
    static Meal meal;
    static Table table;
    static Bill bill;
    static Order order1, order2, order3, order4, order5;

    @BeforeAll
    static void initializeBeforeAll() {
        tableRepository = TableRepositoryImpl.getInstance();
        userRepository = UserRepositoryImpl.getInstance();
        billRepository = BillRepositoryImpl.getInstance();
        mealRepository = MealRepositoryImpl.getInstance();
        orderRepository = OrderRepositoryImpl.getInstance();
        rawMaterialRepository = RawMaterialRepositoryImpl.getInstance();
        dishRepository = DishRepositoryImpl.getInstance();
    }

    @BeforeEach
    void initializeBeforeEach() {
        user = userRepository.findByLogin("chaumontt").get();
        table = tableRepository.save(new Table(1, TableStates.LIBRE, 4, user));
        bill = billRepository.save(new Bill(1L));
        meal = mealRepository.save(new Meal(4, Timestamp.valueOf("2021-04-27 12:00:00"), 30L, table , bill));
        order1 = orderRepository.save(new Order(Timestamp.valueOf("2021-04-27 21:05:00"), Timestamp.valueOf("2021-04-27 12:10:00"), meal));
        order2 = orderRepository.save(new Order(Timestamp.valueOf("2021-04-27 22:35:00"), Timestamp.valueOf("2021-04-27 19:45:00"), meal));
        order3 = orderRepository.save(new Order(Timestamp.valueOf("2021-04-27 12:05:00"), null, meal));
        order4 = orderRepository.save(new Order(Timestamp.valueOf("2021-04-27 19:35:00"), null, meal));
        order5 = orderRepository.save(new Order(Timestamp.valueOf("2021-04-27 12:00:00"), null, meal));
    }

    @Test
    @DisplayName("La file d'attente des commandes est triée et ne comporte que les commandes non préparées")
    void verifyGetOrdersQueueReturnsOrderedQueue() {
        CuisinierController cuisinierController = new CuisinierController(new Authentification());

    }

    @Test
    @DisplayName("Le plat est bien ajouté à la base de données")
    void verifyAddDishSucceed() {
        CuisinierController cuisinierController = new CuisinierController(new Authentification());
        RawMaterial rm = rawMaterialRepository.save(new RawMaterial("Riz", 100, Units.KG));
        HashMap<RawMaterial, Integer> rawMaterialHashMap = new HashMap<RawMaterial, Integer>();
        rawMaterialHashMap.put(rm,1);
        cuisinierController.createDish("Riz", "PLat", MenuTypes.ADULTES, 5.0, rawMaterialHashMap);
        List<Dish> dishes = dishRepository.findAll();
        dishRepository.delete(dishes.get(0).getDishId());
        rawMaterialRepository.delete(rm.getRawMaterialId());
        assertThat(dishes.size(),is(1));
    }

    @Test
    @DisplayName("Le plat n'est pas ajouté à la base de données")
    void verifyAddDishFailedDoublon() {
        CuisinierController cuisinierController = new CuisinierController(new Authentification());
        RawMaterial rm = rawMaterialRepository.save(new RawMaterial("Riz", 100, Units.KG));
        HashMap<RawMaterial, Integer> rawMaterialHashMap = new HashMap<RawMaterial, Integer>();
        rawMaterialHashMap.put(rm,1);
        Dish dish = new Dish("Riz", "PLat", MenuTypes.ADULTES, 5.0, false);
        dish.addRawMaterial(rm,1);
        dish = dishRepository.save(dish);
        cuisinierController.createDish("Riz", "PLat", MenuTypes.ADULTES, 5.0, rawMaterialHashMap);
        List<Dish> dishes = dishRepository.findAll();
        dishRepository.delete(dish.getDishId());
        rawMaterialRepository.delete(rm.getRawMaterialId());
        assertThat(dishes.size(),is(1));
    }

    @Test
    @DisplayName("Le plat est bien modifié")
    void verifyUpdateDishSucceed() {
        CuisinierController cuisinierController = new CuisinierController(new Authentification());
        RawMaterial rm = rawMaterialRepository.save(new RawMaterial("Riz", 100, Units.KG));
        HashMap<RawMaterial, Integer> rawMaterialHashMap = new HashMap<RawMaterial, Integer>();
        rawMaterialHashMap.put(rm,1);
        Dish dish = new Dish("Riz", "Plat", MenuTypes.ADULTES, 5.0, false);
        dish.addRawMaterial(rm,1);
        dish = dishRepository.save(dish);
        cuisinierController.updateDish(dish, "Riz", "Plat", MenuTypes.ADULTES, 6.0, rawMaterialHashMap);
        Dish res = dishRepository.findById(dish.getDishId()).get();
        dishRepository.delete(dish.getDishId());
        rawMaterialRepository.delete(rm.getRawMaterialId());
        assertThat(res.getPrice(), is(6.0));
    }

    @Test
    @DisplayName("Le plat n'est pas modifié car il y a une commande en cours")
    void verifyUpdateDishFailedOrder() {
        User user = userRepository.findByLogin("chaumontt").get();
        Table table = tableRepository.save(new Table(1, TableStates.LIBRE, 4, user));
        Bill bill = billRepository.save(new Bill(2L));
        Meal meal = mealRepository.save(new Meal(4, Timestamp.valueOf("2021-04-27 12:00:00"), 30L, table , bill));
        RawMaterial rawMaterial = rawMaterialRepository.save(new RawMaterial("Riz", 100, Units.KG));
        HashMap<RawMaterial, Integer> rawMaterialHashMap = new HashMap<RawMaterial, Integer>();
        rawMaterialHashMap.put(rawMaterial,1);
        Dish dish = dishRepository.save(new Dish("Riz", "Plat", MenuTypes.ADULTES, 5.0, true, rawMaterialHashMap));
        Map<Dish,Integer> dishIntegerMap = new HashMap<Dish,Integer>();
        dishIntegerMap.put(dish,1);
        Order order = orderRepository.save(new Order(Timestamp.from(Instant.now()), meal, dishIntegerMap));
        CuisinierController cuisinierController = new CuisinierController(new Authentification());
        cuisinierController.updateDish(dish, "Riz", "PLat", MenuTypes.ADULTES, 6.0, rawMaterialHashMap);
        Dish result = dishRepository.findById(dish.getDishId()).get();
        orderRepository.delete(order.getOrderId());
        dishRepository.delete(dish.getDishId());
        rawMaterialRepository.delete(rawMaterial.getRawMaterialId());
        mealRepository.delete(meal.getMealId());
        tableRepository.delete(table.getTableId());
        billRepository.delete(bill.getBillId());
        assertThat(result.getPrice(), is(5.0));
    }

    @Test
    @DisplayName("Le plat n'est pas modifié car le plat existe déjà")
    void verifyUpdateDishFailedDoublon() {
        CuisinierController cuisinierController = new CuisinierController(new Authentification());
        RawMaterial rm = rawMaterialRepository.save(new RawMaterial("Riz", 100, Units.KG));
        HashMap<RawMaterial, Integer> rawMaterialHashMap = new HashMap<RawMaterial, Integer>();
        rawMaterialHashMap.put(rm,1);
        Dish dish = new Dish("Riz", "PLat", MenuTypes.ADULTES, 5.0, false);
        dish.addRawMaterial(rm,1);
        dish = dishRepository.save(dish);
        Dish dish2 = new Dish("Riz blanc", "PLat", MenuTypes.ADULTES, 6.0, false);
        dish2.addRawMaterial(rm,1);
        dish2 = dishRepository.save(dish2);
        cuisinierController.updateDish(dish2, "Riz", "PLat", MenuTypes.ADULTES, 6.0, rawMaterialHashMap);
        Dish res = dishRepository.findById(dish2.getDishId()).get();
        dishRepository.delete(dish.getDishId());
        dishRepository.delete(dish2.getDishId());
        rawMaterialRepository.delete(rm.getRawMaterialId());
        assertThat(res.getDishName(), is("Riz blanc"));
    }

    @Test
    @DisplayName("Le plat est bien supprimé")
    void verifyDeleteDishSucceed() {
        CuisinierController cuisinierController = new CuisinierController(new Authentification());
        RawMaterial rm = rawMaterialRepository.save(new RawMaterial("Riz", 100, Units.KG));
        HashMap<RawMaterial, Integer> rawMaterialHashMap = new HashMap<RawMaterial, Integer>();
        rawMaterialHashMap.put(rm,1);
        Dish dish = new Dish("Riz", "PLat", MenuTypes.ADULTES, 5.0, false);
        dish.addRawMaterial(rm,1);
        dish = dishRepository.save(dish);
        cuisinierController.deleteDish(dish);
        List<Dish> dishes = dishRepository.findAll();
        dishRepository.delete(dish.getDishId());
        rawMaterialRepository.delete(rm.getRawMaterialId());
        assertThat(dishes.size(), is(0));
    }

    @Test
    @DisplayName("Le plat n'est pas supprimé car le plat est dans une commande")
    void verifyDeleteDishFailedOrder() {
        User user = userRepository.findByLogin("chaumontt").get();
        Table table = tableRepository.save(new Table(1, TableStates.LIBRE, 4, user));
        Bill bill = billRepository.save(new Bill(2L));
        Meal meal = mealRepository.save(new Meal(4, Timestamp.valueOf("2021-04-27 12:00:00"), 30L, table , bill));
        RawMaterial rawMaterial = rawMaterialRepository.save(new RawMaterial("Riz", 100, Units.KG));
        HashMap<RawMaterial, Integer> rawMaterialHashMap = new HashMap<RawMaterial, Integer>();
        rawMaterialHashMap.put(rawMaterial,1);
        Dish dish = dishRepository.save(new Dish("Riz", "Plat", MenuTypes.ADULTES, 5.0, false, rawMaterialHashMap));
        Map<Dish,Integer> dishIntegerMap = new HashMap<Dish,Integer>();
        dishIntegerMap.put(dish,1);
        Order order = orderRepository.save(new Order(Timestamp.from(Instant.now()), meal, dishIntegerMap));
        CuisinierController cuisinierController = new CuisinierController(new Authentification());
        cuisinierController.deleteDish(dish);
        List<Dish> dishes = dishRepository.findAll();
        orderRepository.delete(order.getOrderId());
        dishRepository.delete(dish.getDishId());
        rawMaterialRepository.delete(rawMaterial.getRawMaterialId());
        mealRepository.delete(meal.getMealId());
        tableRepository.delete(table.getTableId());
        billRepository.delete(bill.getBillId());
        assertThat(dishes.size(), is(1));
    }

    @Test
    @DisplayName("La liste des plats s'affiche correctement")
    void verifyDisplayDishes() {
        CuisinierController cuisinierController = new CuisinierController(new Authentification());
        var expected = new TextStringBuilder();
        RawMaterial rawMaterial = rawMaterialRepository.save(new RawMaterial("Riz", 100, Units.KG));
        HashMap<RawMaterial, Integer> rawMaterialHashMap = new HashMap<RawMaterial, Integer>();
        rawMaterialHashMap.put(rawMaterial,1);
        Dish dish = dishRepository.save(new Dish("Riz", "Plat", MenuTypes.ADULTES, 5.0, false, rawMaterialHashMap));
        expected.appendln("[%d] %s", dish.getDishId(), "Riz");
        var dishes = cuisinierController.getNotDailyMenuDishes();
        var res = cuisinierController.displayDishes(dishes);
        dishRepository.deleteRawMaterialsByDishId(dish.getDishId());
        dishRepository.delete(dish.getDishId());
        rawMaterialRepository.delete(rawMaterial.getRawMaterialId());
        assertThat(res, is(expected.toString()));
    }

    @AfterEach
    void tearDownAfterEach() {
        orderRepository.delete(order1.getOrderId());
        orderRepository.delete(order2.getOrderId());
        orderRepository.delete(order3.getOrderId());
        orderRepository.delete(order4.getOrderId());
        orderRepository.delete(order5.getOrderId());
        mealRepository.delete(meal.getMealId());
        tableRepository.delete(table.getTableId());
        billRepository.delete(bill.getBillId());
    }
}
