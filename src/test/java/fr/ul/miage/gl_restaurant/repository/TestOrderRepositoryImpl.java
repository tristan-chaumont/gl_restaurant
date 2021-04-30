package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.MenuTypes;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.constants.Units;
import fr.ul.miage.gl_restaurant.model.*;
import fr.ul.miage.gl_restaurant.model.Order;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestOrderRepositoryImpl {

    static MealRepositoryImpl mealRepository;
    static OrderRepositoryImpl orderRepository;
    static TableRepositoryImpl tableRepository;
    static BillRepositoryImpl billRepository;
    static UserRepositoryImpl userRepository;
    static DishRepositoryImpl dishRepository;
    static RawMaterialRepositoryImpl rawMaterialRepository;
    static User user;
    static Meal meal1, meal2;
    static Table table;
    static Bill bill1, bill2;
    static Order order1, order2;
    static Dish dish;
    static RawMaterial rawMaterial;

    @BeforeAll
    static void initializeBeforeAll() {
        tableRepository = TableRepositoryImpl.getInstance();
        userRepository = UserRepositoryImpl.getInstance();
        billRepository = BillRepositoryImpl.getInstance();
        mealRepository = MealRepositoryImpl.getInstance();
        orderRepository = OrderRepositoryImpl.getInstance();
        dishRepository = DishRepositoryImpl.getInstance();
        rawMaterialRepository = RawMaterialRepositoryImpl.getInstance();
    }

    @BeforeEach
    void initializeBeforeEach() {
        user = userRepository.findByLogin("chaumontt").get();
        table = tableRepository.save(new Table(1, TableStates.LIBRE, 4, user));
        bill1 = billRepository.save(new Bill(1L));
        bill2 = billRepository.save(new Bill(2L));
        meal1 = mealRepository.save(new Meal(4, Timestamp.valueOf("2021-04-27 12:00:00"), 30L, table ,bill1));
        meal2 = mealRepository.save(new Meal( 4, Timestamp.valueOf("2021-04-27 19:30:00"), 45L, table ,bill2));
        rawMaterial = rawMaterialRepository.save(new RawMaterial("Riz", 100, Units.KG));
        dish = new Dish("Saumon", "Poisson", MenuTypes.ADULTES, 8.5, false);
        dish.addRawMaterial(rawMaterial, 1);
        dish = dishRepository.save(dish);
        order1 = new Order(Timestamp.valueOf("2021-04-27 12:05:00"), Timestamp.valueOf("2021-04-27 12:10:00"), meal1);
        order1.addDish(dish, 2);
        order1 = orderRepository.save(order1);
        order2 = orderRepository.save(new Order(Timestamp.valueOf("2021-04-27 19:35:00"), Timestamp.valueOf("2021-04-27 19:45:00"), meal2));
    }

    @Test
    @DisplayName("findAll() récupère tous les éléments")
    void verifyFindAllReturnsAllElements() {
        List<Order> result = orderRepository.findAll();
        assertThat(result.size(), is(2));
    }

    @Test
    @DisplayName("findCurrentOrders() récupère toutes les commandes qui ont une preparationDate à null")
    void verifyFindCurrentOrdersReturnsRightElements() {
        Order order3 = orderRepository.save(new Order(Timestamp.valueOf("2021-04-27 12:05:00"), null, meal1));
        Order order4 = orderRepository.save(new Order(Timestamp.valueOf("2021-04-27 19:35:00"), null, meal2));
        List<Order> result = orderRepository.findCurrentOrders();
        assertThat(result.size(), is(2));
        orderRepository.delete(order3.getOrderId());
        orderRepository.delete(order4.getOrderId());
    }

    @Test
    @DisplayName("findById() récupère le bon utilisateur")
    void verifyFindByIdGetsOrder() {
        Optional<Order> result = orderRepository.findById(order1.getOrderId());
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getOrderDate(), equalTo(order1.getOrderDate()));
    }

    @Test
    @DisplayName("findById() ne récupère rien")
    void verifyFindByIdGetsNothing() {
        Optional<Order> result = orderRepository.findById(999999999999999999L);
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("L'insertion fonctionne")
    void verifySaveInsertElement() {
        Table tableInsert = tableRepository.save(new Table(1,TableStates.LIBRE, 4, user));
        Bill bill = billRepository.save(new Bill(3L));
        Meal meal = mealRepository.save(new Meal(4, Timestamp.valueOf("2021-04-26 12:00:00"), 30L, tableInsert ,bill));
        Order order = orderRepository.save(new Order(Timestamp.valueOf("2021-04-26 12:05:00"),Timestamp.valueOf("2021-04-26 12:15:00"),meal));
        assertNotNull(order.getOrderId());
        Order result = orderRepository.findById(order.getOrderId()).get();
        orderRepository.delete(order.getOrderId());
        mealRepository.delete(meal.getMealId());
        billRepository.delete(bill.getBillId());
        tableRepository.delete(tableInsert.getTableId());
        assertThat(result.getOrderDate(), equalTo(order.getOrderDate()));
        assertThat(result.getPreparationDate(), equalTo(order.getPreparationDate()));
    }

    //Faire un test sur le fait que l'on ne peut insérer un objet Meal sur une table occupée

    @Test
    @DisplayName("La modification de la commande fonctionne")
    void verifyUpdateSucceed() {
        order1.setOrderDate(Timestamp.valueOf("2021-04-26 12:03:00"));
        orderRepository.update(order1);
        Order result = orderRepository.findById(order1.getOrderId()).get();
        assertThat(result.getOrderDate(), equalTo(Timestamp.valueOf("2021-04-26 12:03:00")));
    }

    @Test
    @DisplayName("La modification ne s'effectue pas car la commande n'existe pas")
    void verifyUpdateFailBecauseOrderDoesNotExist() {
        Table tableInsert = new Table(3L, 1,TableStates.LIBRE, 4, user);
        Bill bill = new Bill(3L);
        Meal meal = new Meal(3L,4, Timestamp.valueOf("2021-04-25 12:00:00"), 30L, tableInsert ,bill);
        Order order = new Order(3L, Timestamp.valueOf("2021-04-26 12:05:00"),Timestamp.valueOf("2021-04-26 12:15:00"),meal);
        orderRepository.update(order);
        Optional<Order> result = orderRepository.findById(order.getOrderId());
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    @DisplayName("La suppression de la commande fonctionne")
    void verifyDeleteSucceed() {
        Table tableInsert = tableRepository.save(new Table(1,TableStates.LIBRE, 4, user));
        Bill bill = billRepository.save(new Bill(3L));
        Meal meal = mealRepository.save(new Meal(4, Timestamp.valueOf("2021-04-26 12:00:00"), 30L, tableInsert ,bill));
        Order order = orderRepository.save(new Order(Timestamp.valueOf("2021-04-26 12:05:00"),Timestamp.valueOf("2021-04-26 12:15:00"),meal));
        int totalOrders = orderRepository.findAll().size();
        orderRepository.delete(order.getOrderId());
        mealRepository.delete(meal.getMealId());
        billRepository.delete(bill.getBillId());
        tableRepository.delete(tableInsert.getTableId());
        int newTotalOrders = orderRepository.findAll().size();
        assertThat(newTotalOrders, equalTo(totalOrders - 1));
    }

    @Test
    @DisplayName("La suppression ne fonctionne pas car la commande n'existe pas")
    void verifyDeleteFailBecauseOrderDoesNotExist() {
        Table tableInsert = new Table(3L, 1,TableStates.LIBRE, 4, user);
        Bill bill = new Bill(3L);
        Meal meal = new Meal(3L,4, Timestamp.valueOf("2021-04-25 12:00:00"), 30L, tableInsert ,bill);
        Order order = new Order(3L,Timestamp.valueOf("2021-04-26 12:05:00"),Timestamp.valueOf("2021-04-26 12:15:00"),meal);
        int totalOrders = orderRepository.findAll().size();
        orderRepository.delete(order.getOrderId());
        int newTotalOrders = orderRepository.findAll().size();
        assertThat(newTotalOrders, equalTo(totalOrders));
    }

    /* DISH */

    @Test
    @DisplayName("Les plats qui composent une commande sont récupérés")
    void verifyFindDishesByOrderIdReturnsAllElements() {
        Map<Dish, Integer> dishes = orderRepository.findDishesByOrderId(order1.getOrderId());
        assertThat(dishes.size(), is(1));
        assertThat(dishes.get(dish), is(2));
    }

    @Test
    @DisplayName("Les plats d'une nouvelle commande sont insérés")
    void verifySaveDishesByOrderIdSucceed() {
        Map<Dish, Integer> dishes;
        Order newOrder = new Order(Timestamp.valueOf("2021-04-27 12:05:00"), Timestamp.valueOf("2021-04-27 12:10:00"), meal1);
        newOrder.addDish(dish, 50);
        newOrder = orderRepository.save(newOrder);
        assertNotNull(newOrder.getOrderId());
        dishes = orderRepository.findDishesByOrderId(newOrder.getOrderId());
        assertThat(dishes.size(), is(1));
        assertThat(dishes.get(dish), is(50));
        orderRepository.delete(newOrder.getOrderId());
    }

    @Test
    @DisplayName("Les plats sont mis à jour correctement")
    void verifyUpdateDishesByOrderIdSucceed() {
        Order newOrder = new Order(Timestamp.valueOf("2021-04-27 12:05:00"), Timestamp.valueOf("2021-04-27 12:10:00"), meal1);
        newOrder.addDish(dish, 50);
        newOrder = orderRepository.save(newOrder);
        Dish newDish = new Dish("Pastis", "Vinasse", MenuTypes.ADULTES, 8.5, false);
        newDish.addRawMaterial(rawMaterial, 1);
        newDish = dishRepository.save(newDish);
        newOrder.clearDishes();
        newOrder.addDish(newDish, 100);
        newOrder = orderRepository.update(newOrder);
        Map<Dish, Integer> dishes = orderRepository.findDishesByOrderId(newOrder.getOrderId());
        assertThat(dishes.size(), is(1));
        assertThat(dishes.get(newDish), is(100));
        orderRepository.delete(newOrder.getOrderId());
        dishRepository.delete(newDish.getDishId());
    }

    @Test
    @DisplayName("Les plats sont supprimés correctement")
    void verifyDeleteDishesByOrderIdSucceed() {
        orderRepository.delete(order1.getOrderId());
        Map<Dish, Integer> dishes = orderRepository.findDishesByOrderId(order1.getOrderId());
        assertThat(dishes.size(), is(0));
    }

    @AfterEach
    void tearDownAfterEach() {
        orderRepository.delete(order1.getOrderId());
        orderRepository.delete(order2.getOrderId());
        mealRepository.delete(meal1.getMealId());
        mealRepository.delete(meal2.getMealId());
        tableRepository.delete(table.getTableId());
        billRepository.delete(bill1.getBillId());
        billRepository.delete(bill2.getBillId());
        dishRepository.delete(dish.getDishId());
        rawMaterialRepository.delete(rawMaterial.getRawMaterialId());
    }
}
