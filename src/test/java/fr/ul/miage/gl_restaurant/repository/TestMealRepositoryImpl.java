package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Bill;
import fr.ul.miage.gl_restaurant.model.Meal;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.model.User;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestMealRepositoryImpl {

    static MealRepositoryImpl mealRepository;
    static TableRepositoryImpl tableRepository;
    static BillRepositoryImpl billRepository;
    static UserRepositoryImpl userRepository;
    static User user;
    static Meal meal1, meal2;
    static Table table;
    static Bill bill1, bill2;

    @BeforeAll
    static void initializeBeforeAll() {
        tableRepository = TableRepositoryImpl.getInstance();
        userRepository = UserRepositoryImpl.getInstance();
        billRepository = BillRepositoryImpl.getInstance();
        mealRepository = MealRepositoryImpl.getInstance();
    }

    @BeforeEach
    void initializeBeforeEach() {
        user = userRepository.save(new User("userMeal1", "user", "meal", Roles.SERVEUR));
        table = tableRepository.save(new Table(1, TableStates.LIBRE, 4, user));
        bill1 = billRepository.save(new Bill(1L));
        bill2 = billRepository.save(new Bill(2L));
        meal1 = mealRepository.save(new Meal(4, Timestamp.valueOf("2021-04-27 12:00:00"), 30L, table ,bill1));
        meal2 = mealRepository.save(new Meal( 4, Timestamp.valueOf("2021-04-27 19:30:00"), 45L, table ,bill2));
    }

    /* FIND */

    @Test
    @DisplayName("findAll() récupère tous les éléments")
    void verifyFindAllReturnsAllElements() {
        List<Meal> result = mealRepository.findAll();
        assertThat(result.size(), is(2));
    }

    @Test
    @DisplayName("findById() récupère le bon élément")
    void verifyFindByIdSucceed() {
        Optional<Meal> result = mealRepository.findById(meal1.getMealId());
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getCustomersNb(), is(4));
    }

    @Test
    @DisplayName("findById() ne récupère rien")
    void verifyFindByIdGetsNothing() {
        Optional<Meal> result = mealRepository.findById(999999999999999999L);
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("L'insertion fonctionne")
    void verifySaveInsertElement() {
        Table tableInsert = tableRepository.save(new Table(1,TableStates.LIBRE, 4, user));
        Bill bill = billRepository.save(new Bill(3L));
        Meal meal = mealRepository.save(new Meal(4, Timestamp.valueOf("2021-04-26 12:00:00"), 30L, tableInsert ,bill));
        assertNotNull(meal.getMealId());
        Optional<Meal> result = mealRepository.findById(meal.getMealId());
        assertThat(result.isPresent(), is(true));
        mealRepository.delete(meal.getMealId());
        billRepository.delete(bill.getBillId());
        tableRepository.delete(tableInsert.getTableId());
        assertThat(result.get().getCustomersNb(), equalTo(meal.getCustomersNb()));
        assertThat(result.get().getStartDate(), equalTo(meal.getStartDate()));
        assertThat(result.get().getMealDuration(), equalTo(meal.getMealDuration()));
    }

    //Faire un test sur le fait que l'on ne peut insérer un objet Meal sur une table occupée

    @Test
    @DisplayName("La modification du repas fonctionne")
    void verifyUpdateSucceed() {
        meal1.setMealDuration(40L);
        mealRepository.update(meal1);
        Optional<Meal> result = mealRepository.findById(meal1.getMealId());
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getMealDuration(), equalTo(40L));
    }

    @Test
    @DisplayName("La modification ne s'effectue pas car le repas n'existe pas")
    void verifyUpdateFailBecauseUserDoesNotExist() {
        Table tableInsert = new Table(3L, 1,TableStates.LIBRE, 4, user);
        Bill bill = new Bill(3L);
        Meal meal = new Meal(3L,4, Timestamp.valueOf("2021-04-25 12:00:00"), 30L, tableInsert ,bill);
        mealRepository.update(meal);
        Optional<Meal> result = mealRepository.findById(meal.getMealId());
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    @DisplayName("La suppression du repas fonctionne")
    void verifyDeleteSucceed() {
        Table tableInsert = tableRepository.save(new Table(1,TableStates.LIBRE, 4, user));
        Bill bill = billRepository.save(new Bill(3L));
        Meal meal = mealRepository.save(new Meal(4, Timestamp.valueOf("2021-04-26 12:00:00"), 30L, tableInsert ,bill));
        int totalUsers = mealRepository.findAll().size();
        mealRepository.delete(meal.getMealId());
        billRepository.delete(bill.getBillId());
        tableRepository.delete(tableInsert.getTableId());
        int newTotalUsers = mealRepository.findAll().size();
        assertThat(newTotalUsers, equalTo(totalUsers - 1));
    }

    @Test
    @DisplayName("La suppression de ne fonctionne pas car le repas n'existe pas")
    void verifyDeleteFailBecauseUserDoesNotExist() {
        Table tableInsert = new Table(3L, 1,TableStates.LIBRE, 4, user);
        Bill bill = new Bill(3L);
        Meal meal = new Meal(3L,4, Timestamp.valueOf("2021-04-25 12:00:00"), 30L, tableInsert ,bill);
        int totalUsers = mealRepository.findAll().size();
        mealRepository.delete(meal.getMealId());
        int newTotalUsers = mealRepository.findAll().size();
        assertThat(newTotalUsers, equalTo(totalUsers));
    }

    @AfterEach
    void tearDownAfterEach() {
        mealRepository.delete(meal1.getMealId());
        mealRepository.delete(meal2.getMealId());
        tableRepository.delete(table.getTableId());
        userRepository.delete(user.getUserId());
        billRepository.delete(bill1.getBillId());
        billRepository.delete(bill2.getBillId());
    }
}
