package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.MenuTypes;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.constants.Units;
import fr.ul.miage.gl_restaurant.model.*;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.repository.*;
import org.junit.jupiter.api.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestDirecteurController {

    static RawMaterialRepositoryImpl rawMaterialRepository;
    static TableRepositoryImpl tableRepository;
    static UserRepositoryImpl userRepository;
    static MealRepositoryImpl mealRepository;
    static OrderRepositoryImpl orderRepository;
    static DishRepositoryImpl dishRepository;
    static BillRepositoryImpl billRepository;
    static RawMaterial rm1;

    @BeforeAll
    static void initializeBeforeAll() {
        rawMaterialRepository = RawMaterialRepositoryImpl.getInstance();
        tableRepository = TableRepositoryImpl.getInstance();
        userRepository = UserRepositoryImpl.getInstance();
        mealRepository = MealRepositoryImpl.getInstance();
        orderRepository = OrderRepositoryImpl.getInstance();
        dishRepository = DishRepositoryImpl.getInstance();
        billRepository = BillRepositoryImpl.getInstance();

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
        RawMaterial rm = directeurController.addRawMaterial("Riz", 100, Units.KG);
        List<RawMaterial> result = rawMaterialRepository.findAll();
        assertThat(result.size(), is(1));
    }

    @Test
    @DisplayName("L'ingrédient est bien modifié")
    void testUpdateRawMaterialSucceed(){
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.updateRawMaterial(rm1, "Pâtes", 100, Units.KG);
        Optional<RawMaterial> result = rawMaterialRepository.findById(rm1.getRawMaterialId());
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
        assertThat(result.get().getRawMaterialName(), is("Riz"));
        assertThat(result.get().getStockQuantity(), is(100));
        assertThat(result.get().getUnit(), is(Units.KG));
    }

    @Test
    @DisplayName("L'ingrédient n'est pas modifié car il y a une commande en cours qui l'utilise")
    void testUpdateRawMaterialFailedOrder(){
        User user = userRepository.findByLogin("chaumontt").get();
        Table table = tableRepository.save(new Table(1, TableStates.LIBRE, 4, user));
        Bill bill = billRepository.save(new Bill(1L));
        Meal meal = mealRepository.save(new Meal(4, Timestamp.valueOf("2021-04-27 12:00:00"), 30L, table , bill));
        RawMaterial rawMaterial = rawMaterialRepository.findByName("Riz").get();
        HashMap<RawMaterial, Integer> rawMaterialHashMap = new HashMap<RawMaterial, Integer>();
        rawMaterialHashMap.put(rawMaterial,1);
        Dish dish = dishRepository.save(new Dish("Riz", "Céréales", MenuTypes.ADULTES, 1.0, true, rawMaterialHashMap));
        Map<Dish,Integer> dishIntegerMap = new HashMap<Dish,Integer>();
        dishIntegerMap.put(dish,1);
        Order order = orderRepository.save(new Order(Timestamp.from(Instant.now()), meal, dishIntegerMap));
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.updateRawMaterial(rm1, "Pâtes", 100, Units.KG);
        Optional<RawMaterial> result = rawMaterialRepository.findById(rm1.getRawMaterialId());
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
        RawMaterial rawMaterial = rawMaterialRepository.findByName("Riz").get();
        HashMap<RawMaterial, Integer> rawMaterialHashMap = new HashMap<RawMaterial, Integer>();
        rawMaterialHashMap.put(rawMaterial,1);
        Dish dish = dishRepository.save(new Dish("Riz", "Céréales", MenuTypes.ADULTES, 1.0, true, rawMaterialHashMap));
        DirecteurController directeurController = new DirecteurController(new Authentification());
        directeurController.deleteRawMaterial(rm1);
        Optional<RawMaterial> result = rawMaterialRepository.findById(rm1.getRawMaterialId());
        dishRepository.delete(dish.getDishId());
        assertThat(result.isEmpty(), is(false));
    }

    @AfterEach
    void tearDownAfterEach() {
        rawMaterialRepository.delete(rm1.getRawMaterialId());

    }



}
