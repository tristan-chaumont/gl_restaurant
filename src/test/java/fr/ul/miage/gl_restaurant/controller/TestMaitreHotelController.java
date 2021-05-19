package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Meal;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.model.User;
import fr.ul.miage.gl_restaurant.repository.MealRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.UserRepositoryImpl;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class TestMaitreHotelController {

    static MaitreHotelController maitreHotelController;
    static TableRepositoryImpl tableRepository;
    static MealRepositoryImpl mealRepository;
    static UserRepositoryImpl userRepository;
    Table table1, table2;
    User user;

    @BeforeAll
    static void initializeBeforeAll() {
        maitreHotelController = new MaitreHotelController(new Authentification());
        tableRepository = TableRepositoryImpl.getInstance();
        mealRepository = MealRepositoryImpl.getInstance();
        userRepository = UserRepositoryImpl.getInstance();
    }

    @BeforeEach
    void initializeBeforeEach() {
        user = userRepository.findByLogin("chaumontt").orElse(null);
        table1 = tableRepository.save(new Table(1, TableStates.LIBRE, 4, user));
        table2 = tableRepository.save(new Table(1, TableStates.RESERVEE, 4, user));
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
    void verifySeatCustomerFailedBecauseReserved(){
        Meal meal = maitreHotelController.seatClient(table2,2);
        assertThat(meal, is(nullValue()));
        assertThat(mealRepository.findAll().size(),is(0));
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

    @AfterEach
    void tearDownAfterEach() {
        tableRepository.delete(table1.getTableId());
        tableRepository.delete(table2.getTableId());
    }
}
