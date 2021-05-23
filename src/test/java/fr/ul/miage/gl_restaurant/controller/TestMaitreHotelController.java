package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.*;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.repository.*;
import fr.ul.miage.gl_restaurant.utilities.DateUtils;
import fr.ul.miage.gl_restaurant.utilities.InputUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class TestMaitreHotelController {

    static MaitreHotelController maitreHotelController;
    static TableRepositoryImpl tableRepository;
    static MealRepositoryImpl mealRepository;
    static UserRepositoryImpl userRepository;
    static OrderRepositoryImpl orderRepository;
    static BillRepositoryImpl billRepository;
    static ReservationRepositoryImpl reservationRepository;
    Table table1, table2, table3;
    User user;

    @BeforeAll
    static void initializeBeforeAll() {
        maitreHotelController = new MaitreHotelController(new Authentification());
        tableRepository = TableRepositoryImpl.getInstance();
        mealRepository = MealRepositoryImpl.getInstance();
        userRepository = UserRepositoryImpl.getInstance();
        orderRepository = OrderRepositoryImpl.getInstance();
        billRepository = BillRepositoryImpl.getInstance();
        reservationRepository = ReservationRepositoryImpl.getInstance();
    }

    @BeforeEach
    void initializeBeforeEach() {
        user = userRepository.findByLogin("chaumontt").orElse(null);
        table1 = tableRepository.save(new Table(1, TableStates.LIBRE, 4, user));
        table2 = tableRepository.save(new Table(1, TableStates.RESERVEE, 4, user));
        table3 = tableRepository.save(new Table(1, TableStates.OCCUPEE, 4, user));
    }

    @Test
    @DisplayName("seatClient change le statt de la table et crée le repas")
    void verifySeatCustomer() {
        Meal meal = maitreHotelController.seatClient(table1,2);
        assertThat(meal, is(notNullValue()));
        Optional<Meal> res = mealRepository.findById(meal.getMealId());
        mealRepository.delete(meal.getMealId());
        assertThat(res.isPresent(), is(true));
        assertThat(res.get().getCustomersNb(), is(2));
        Optional<Table> resTable = tableRepository.findById(table1.getTableId());
        assertThat(resTable.isPresent(), is(true));
        assertThat(resTable.get().getState(), is(TableStates.OCCUPEE));
    }

    @Test
    @DisplayName("Le repas ne peut être créé car la table est réservée")
    void verifySeatCustomerFailedBecauseReserved() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate dateNow = now.toLocalDate();
        boolean isLunch = DateUtils.isDateLunch(now);
        Reservation reservation1 = reservationRepository.save(new Reservation(isLunch, table2, dateNow));
        Meal meal = maitreHotelController.seatClient(table2,2);
        assertThat(meal, is(nullValue()));
        assertThat(mealRepository.findAll().size(),is(0));
        reservationRepository.delete(reservation1.getReservationId());
    }

    @Test
    @DisplayName("Le repas ne peut être créé car la table est trop petite")
    void verifySeatCustomerFailedBecauseOfNbPlaces(){
        Meal meal = maitreHotelController.seatClient(table1,5);
        assertThat(meal, is(nullValue()));
        assertThat(mealRepository.findAll().size(),is(0));
    }

    @Test
    @DisplayName("Le serveur a bien été affecté")
    void verifyAssignServerSucceed() {
        User userTest = new User("ttcUser2", "ttc", "User2", Roles.SERVEUR);
        userTest = userRepository.save(userTest);
        boolean result = maitreHotelController.assignServer(table1, userTest);
        assertThat(result, is(true));
        Optional<Table> tableResult = tableRepository.findById(table1.getTableId());
        assertThat(tableResult.isPresent(), is(true));
        assertThat(tableResult.get().getUser(), equalTo(userTest));
        tableRepository.delete(table1.getTableId());
        userRepository.delete(userTest.getUserId());
    }

    @Test
    @DisplayName("Le serveur n'a pas été affecté car ce n'est pas un serveur")
    void verifyAssignServerFailedBecauseItsNotAServer() {
        User userTest = new User("ttcUser2", "ttc", "User2", Roles.CUISINIER);
        userRepository.save(userTest);
        boolean result = maitreHotelController.assignServer(table1, userTest);
        assertThat(result, is(false));
        Optional<Table> tableResult = tableRepository.findById(table1.getTableId());
        assertThat(tableResult.isPresent(), is(true));
        assertThat(tableResult.get().getUser(), equalTo(user));
        tableRepository.delete(table1.getTableId());
        userRepository.delete(userTest.getUserId());
    }

    @Test
    @DisplayName("Le serveur n'est pas affecté car il n'existe pas")
    void verifyAssignServerFailedBecauseUserDoesNotExist() {
        User userTest = new User(999999L,"ttcUser2", "ttc", "User2", Roles.SERVEUR);
        boolean result = maitreHotelController.assignServer(table1, userTest);
        assertThat(result, is(false));
        Optional<Table> tableResult = tableRepository.findById(table1.getTableId());
        assertThat(tableResult.isPresent(), is(true));
        assertThat(tableResult.get().getUser(), equalTo(user));
        tableRepository.delete(table1.getTableId());
        userRepository.delete(userTest.getUserId());
    }

    @Test
    @DisplayName("Retourne la liste des tables par étage")
    void verifyGetFloorsTables() {
        List<Table> tables = new ArrayList<>() {{
            add(new Table(20L, 1, TableStates.LIBRE, 4, null));
            add(new Table(100L, 2, TableStates.OCCUPEE, 5, null));
            add(new Table(59L, 2, TableStates.LIBRE, 6, null));
            add(new Table(77L, 1, TableStates.LIBRE, 7, null));
            add(new Table(3L, 1, TableStates.SALE, 8, null));
            add(new Table(7L, 3, TableStates.LIBRE, 9, null));
            add(new Table(12L, 3, TableStates.LIBRE, 10, null));
        }};
        Map<Integer, Set<Table>> availableTables = maitreHotelController.getFloorsTables(tables);
        assertThat(availableTables.size(), is(3));
        assertThat(availableTables.get(1).size(), is(3));
        assertThat(availableTables.get(2).size(), is(2));
        assertThat(availableTables.get(3).size(), is(2));
    }

    @Test
    @DisplayName("Retourne la liste des tables disponibles")
    void verifyGetAvailableTablesSucceed() {
        List<Table> tables = new ArrayList<>() {{
            add(new Table(20L, 1, TableStates.LIBRE, 4, null));
            add(new Table(100L, 2, TableStates.OCCUPEE, 5, null));
            add(new Table(59L, 2, TableStates.LIBRE, 6, null));
            add(new Table(77L, 1, TableStates.LIBRE, 7, null));
            add(new Table(3L, 1, TableStates.SALE, 8, null));
            add(new Table(7L, 3, TableStates.LIBRE, 9, null));
            add(new Table(12L, 3, TableStates.LIBRE, 10, null));
        }};
        List<Table> availableTables = maitreHotelController.getAvailableTables(tables);
        assertThat(availableTables.size(), is(5));
    }

    @Test
    @DisplayName("Calcule le montant total de la facture")
    void verifyCalculateBillTotalReturnsRightTotal() {
        Order order = new Order();
        Dish dish1 = new Dish();
        dish1.setPrice(5.0);
        Dish dish2 = new Dish();
        dish2.setPrice(2.2);
        order.addDish(dish1, 2);
        order.addDish(dish2, 4);
        assertThat(maitreHotelController.calculateBillTotal(order), is(18.8));
    }

    @Test
    @DisplayName("La création de la facture fonctionne et est déjà payée")
    void verifyCreateBillSucceedAndIsPaid() {
        Order order = new Order();
        order.setOrderDate(Timestamp.from(Instant.now()));
        Dish dish1 = new Dish();
        dish1.setPrice(5.0);
        Dish dish2 = new Dish();
        dish2.setPrice(2.2);
        order.addDish(dish1, 2);
        order.addDish(dish2, 4);
        Meal meal = mealRepository.save(new Meal(2, Timestamp.from(Instant.now().minus(5, ChronoUnit.MINUTES)), table3));
        order.setMeal(meal);
        order = orderRepository.save(order);
        try (MockedStatic<InputUtils> utilities = Mockito.mockStatic(InputUtils.class)) {
            utilities.when(InputUtils::readInputConfirmation).thenReturn("y", "y");
            boolean result = maitreHotelController.createBill(meal, order, table3);
            assertThat(result, is(true));
            assertNotNull(meal.getBill());
            assertNotNull(meal.getBill().getBillId());
            assertThat(meal.getBill().getTotal(), is(18.8));
            assertThat(meal.getBill().isPaid(), is(true));
            assertNotNull(meal.getMealDuration());
            assertThat(meal.getMealDuration(), is(greaterThan(0L)));
            assertThat(table3.getState(), is(TableStates.SALE));
        }
        orderRepository.delete(order.getOrderId());
        mealRepository.delete(meal.getMealId());
        billRepository.delete(meal.getBill().getBillId());
    }

    @Test
    @DisplayName("La création de la facture fonctionne et n'est pas payée")
    void verifyCreateBillSucceedAndIsNotPaid() {
        Order order = new Order();
        order.setOrderDate(Timestamp.from(Instant.now()));
        Dish dish1 = new Dish();
        dish1.setPrice(5.0);
        Dish dish2 = new Dish();
        dish2.setPrice(2.2);
        order.addDish(dish1, 2);
        order.addDish(dish2, 4);
        Meal meal = mealRepository.save(new Meal(2, Timestamp.from(Instant.now().minus(5, ChronoUnit.MINUTES)), table3));
        order.setMeal(meal);
        order = orderRepository.save(order);
        try (MockedStatic<InputUtils> utilities = Mockito.mockStatic(InputUtils.class)) {
            utilities.when(InputUtils::readInputConfirmation).thenReturn("y", "n");
            boolean result = maitreHotelController.createBill(meal, order, table3);
            assertThat(result, is(true));
            assertNotNull(meal.getBill());
            assertNotNull(meal.getBill().getBillId());
            assertThat(meal.getBill().getTotal(), is(18.8));
            assertThat(meal.getBill().isPaid(), is(false));
            assertNull(meal.getMealDuration());
            assertThat(table3.getState(), is(TableStates.OCCUPEE));
        }
        orderRepository.delete(order.getOrderId());
        mealRepository.delete(meal.getMealId());
        billRepository.delete(meal.getBill().getBillId());
    }

    @Test
    @DisplayName("La création de la facture est annulée")
    void verifyCreateBillIsCancelled() {
        Order order = new Order();
        Meal meal = new Meal(2, Timestamp.from(Instant.now().minus(5, ChronoUnit.MINUTES)), table3);
        try (MockedStatic<InputUtils> utilities = Mockito.mockStatic(InputUtils.class)) {
            utilities.when(InputUtils::readInputConfirmation).thenReturn("n");
            boolean result = maitreHotelController.createBill(meal, order, table3);
            assertThat(result, is(false));
        }
    }

    @Test
    @DisplayName("La table est libre car son état est libre")
    void verifyTableIsFreeNowSucceedBecauseTableStateIsLibre() {
        boolean result = maitreHotelController.verifyTableIsFree(table1, LocalDateTime.now());
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("La table est libre car elle est réservée mais pas pour cette période")
    void verifyTableIsFreeNowSucceedBecauseTableIsNotReservedNow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate dateNow = now.toLocalDate();
        boolean isLunch = DateUtils.isDateLunch(now);
        Reservation reservationLunch;
        Reservation reservationDinner;
        if (isLunch) {
            reservationDinner = reservationRepository.save(new Reservation(false, table2, dateNow));
            boolean result = maitreHotelController.verifyTableIsFree(table2, now);
            assertThat(result, is(true));
            reservationRepository.delete(reservationDinner.getReservationId());
        } else {
            reservationLunch = reservationRepository.save(new Reservation(true, table2, dateNow));
            boolean result = maitreHotelController.verifyTableIsFree(table2, now);
            assertThat(result, is(true));
            reservationRepository.delete(reservationLunch.getReservationId());
        }
    }

    @Test
    @DisplayName("La table est non libre car son état n'est pas libre ni réservée")
    void verifyTableIsFreeNowFailBecauseTableStateIsntLibreOrReservee() {
        boolean result = maitreHotelController.verifyTableIsFree(table3, LocalDateTime.now());
        assertThat(result, is(false));
    }

    @Test
    @DisplayName("La table n'est pas libre car elle est réservée pour cette période")
    void verifyTableIsFreeNowFailBecauseTableIsReservedNow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate dateNow = now.toLocalDate();
        boolean isLunch = DateUtils.isDateLunch(now);
        Reservation reservationLunch;
        Reservation reservationDinner;
        if (isLunch) {
            reservationLunch = reservationRepository.save(new Reservation(true, table2, dateNow));
            boolean result = maitreHotelController.verifyTableIsFree(table2, now);
            assertThat(result, is(false));
            reservationRepository.delete(reservationLunch.getReservationId());
        } else {
            reservationDinner = reservationRepository.save(new Reservation(false, table2, dateNow));
            boolean result = maitreHotelController.verifyTableIsFree(table2, now);
            assertThat(result, is(false));
            reservationRepository.delete(reservationDinner.getReservationId());
        }
    }

    @AfterEach
    void tearDownAfterEach() {
        tableRepository.delete(table1.getTableId());
        tableRepository.delete(table2.getTableId());
        tableRepository.delete(table3.getTableId());
    }
}
