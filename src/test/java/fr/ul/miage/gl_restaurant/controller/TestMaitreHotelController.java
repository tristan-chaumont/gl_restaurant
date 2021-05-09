package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Meal;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.model.User;
import fr.ul.miage.gl_restaurant.repository.MealRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.UserRepositoryImpl;
import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestMaitreHotelController {

    static MaitreHotelController maitreHotelController;
    static TableRepositoryImpl tableRepository;
    static MealRepositoryImpl mealRepository;
    static UserRepositoryImpl userRepository;
    static Table table1, table2;
    static User user;

    @BeforeAll
    static void initializeBeforeAll() {
        maitreHotelController = new MaitreHotelController();
        tableRepository = TableRepositoryImpl.getInstance();
        mealRepository = MealRepositoryImpl.getInstance();
        userRepository = UserRepositoryImpl.getInstance();
    }

    @BeforeEach
    void initializeBeforeEach() {
        user = userRepository.findByLogin("chaumontt").get();
        table1 = tableRepository.save(new Table(1, TableStates.LIBRE, 4, user));
        table2 = tableRepository.save(new Table(1, TableStates.RESERVEE, 4, user));
    }

    @Test
    @DisplayName("installClient change le statt de la table et crée le repas")
    void verifyInstallCustomer() {
        Meal meal = maitreHotelController.seatClient(table1,2);
        assertThat(meal, is(notNullValue()));
        Optional<Meal> res = mealRepository.findById(meal.getMealId());
        mealRepository.delete(meal.getMealId());
        assertThat(res.isPresent(), is(true));
        assertThat(res.get().getCustomersNb(), is(2));
        Table resTable = tableRepository.findById(table1.getTableId()).get();
        assertThat(resTable.getState(), is(TableStates.OCCUPEE));
    }

    @Test
    @DisplayName("Le repas ne peut être créé car la table est réservée")
    void verifyInstallCustomerFailedBecauseReserved(){
        Meal meal = maitreHotelController.seatClient(table2,2);
        assertThat(meal, is(nullValue()));
        assertThat(mealRepository.findAll().size(),is(0));
    }

    @Test
    @DisplayName("Le repas ne peut être créé car la table est trop petite")
    void verifyInstallCustomerFailedBecauseOfNbPlaces(){
        Meal meal = maitreHotelController.seatClient(table1,5);
        assertThat(meal, is(nullValue()));
        assertThat(mealRepository.findAll().size(),is(0));
    }

    @AfterEach
    void tearDownAfterEach() {
        tableRepository.delete(table1.getTableId());
        tableRepository.delete(table2.getTableId());
    }
}
