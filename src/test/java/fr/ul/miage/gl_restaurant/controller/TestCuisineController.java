package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.*;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.repository.*;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.SortedSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class TestCuisineController {

    static MealRepositoryImpl mealRepository;
    static OrderRepositoryImpl orderRepository;
    static TableRepositoryImpl tableRepository;
    static BillRepositoryImpl billRepository;
    static UserRepositoryImpl userRepository;
    static User user;
    static Meal meal;
    static Table table;
    static Bill bill;
    static Order order1, order2, order3, order4, order5;

    @BeforeAll
    static void initializeBeforeAll() {
        tableRepository = new TableRepositoryImpl(Environment.TEST);
        userRepository = new UserRepositoryImpl(Environment.TEST);
        billRepository = new BillRepositoryImpl(Environment.TEST);
        mealRepository = new MealRepositoryImpl(Environment.TEST);
        orderRepository = new OrderRepositoryImpl(Environment.TEST);
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
        CuisinierController cuisinierController = new CuisinierController();
        SortedSet<Order> ordersQueue = cuisinierController.getOrdersQueue();
        assertThat(ordersQueue.size(), is(3));
        assertThat(ordersQueue.first().getOrderDate(), equalTo(order5.getOrderDate()));
        assertThat(ordersQueue.last().getOrderDate(), equalTo(order4.getOrderDate()));
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

    @AfterAll
    static void tearDownAfterAll() {
        try {
            orderRepository.connection.close();
            orderRepository = null;
            mealRepository.connection.close();
            mealRepository = null;
            userRepository.connection.close();
            userRepository = null;
            tableRepository.connection.close();
            tableRepository = null;
            billRepository.connection.close();
            billRepository = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
