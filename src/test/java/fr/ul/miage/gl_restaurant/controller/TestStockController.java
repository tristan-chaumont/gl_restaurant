package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.constants.MenuTypes;
import fr.ul.miage.gl_restaurant.constants.Units;
import fr.ul.miage.gl_restaurant.model.Dish;
import fr.ul.miage.gl_restaurant.model.RawMaterial;
import fr.ul.miage.gl_restaurant.repository.DishRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.RawMaterialRepositoryImpl;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TestStockController {

    private static StockController stockController;
    private static DishRepositoryImpl dishRepository;
    private static RawMaterialRepositoryImpl rawMaterialRepository;
    private Dish dish1, dish2, dish3;
    private RawMaterial rawMaterial1, rawMaterial2;

    @BeforeAll
    static void initializeBeforeAll() {
        stockController = new StockController();
        dishRepository = DishRepositoryImpl.getInstance();
        rawMaterialRepository = RawMaterialRepositoryImpl.getInstance();
    }

    @BeforeEach
    void initializeBeforeEach() {
        rawMaterial1 = new RawMaterial("Riz", 10, Units.KG);
        rawMaterial2 = new RawMaterial("Vin", 10, Units.L);
        rawMaterial1 = rawMaterialRepository.save(rawMaterial1);
        rawMaterial2 = rawMaterialRepository.save(rawMaterial2);
        HashMap<RawMaterial, Integer> rawMaterialsDish1 = new HashMap<>() {{
            put(rawMaterial1, 11);
            put(rawMaterial2, 9);
        }};
        HashMap<RawMaterial, Integer> rawMaterialsDish2 = new HashMap<>() {{
            put(rawMaterial1, 5);
            put(rawMaterial2, 12);
        }};
        HashMap<RawMaterial, Integer> rawMaterialsDish3 = new HashMap<>() {{
            put(rawMaterial1, 5);
            put(rawMaterial2, 5);
        }};
        dish1 = new Dish("Curry", "Viande", MenuTypes.ADULTES, 10.0, true, rawMaterialsDish1);
        dish2 = new Dish("Curry v2", "Viande", MenuTypes.ADULTES, 10.0, true, rawMaterialsDish2);
        dish3 = new Dish("Curry v3", "Viande", MenuTypes.ADULTES, 10.0, true, rawMaterialsDish3);
        dish1 = dishRepository.save(dish1);
        dish2 = dishRepository.save(dish2);
        dish3 = dishRepository.save(dish3);
    }

    @Test
    @DisplayName("Les plats sont retir??s de la carte du jour")
    void verifyDailyMenuIsFalseBecauseThereIsNoStock() {
        stockController.updateDailyMenuBasedOnRemainingStock();
        Optional<Dish> updatedDish1 = dishRepository.findById(dish1.getDishId());
        Optional<Dish> updatedDish2 = dishRepository.findById(dish2.getDishId());
        assertThat(updatedDish1.isPresent(), is(true));
        assertThat(updatedDish2.isPresent(), is(true));
        assertThat(updatedDish1.get().isDailyMenu(), is(false));
        assertThat(updatedDish2.get().isDailyMenu(), is(false));
    }

    @Test
    @DisplayName("Les plats ne sont pas retir??s car il reste du stock")
    void verifyDailyMenuStaysTrueBecauseThereIsEnoughStock() {
        stockController.updateDailyMenuBasedOnRemainingStock();
        Optional<Dish> updatedDish3 = dishRepository.findById(dish3.getDishId());
        assertThat(updatedDish3.isPresent(), is(true));
        assertThat(updatedDish3.get().isDailyMenu(), is(true));
    }

    @Test
    @DisplayName("Restock les mati??res premi??res en rupture de stock")
    void verifyRestockSucceed() {
        int rm1InitialStock = rawMaterial1.getStockQuantity();
        int rm2InitialStock = rawMaterial2.getStockQuantity();
        stockController.restock();
        Optional<RawMaterial> resRM1 = rawMaterialRepository.findById(rawMaterial1.getRawMaterialId());
        Optional<RawMaterial> resRM2 = rawMaterialRepository.findById(rawMaterial1.getRawMaterialId());
        assertThat(resRM1.isPresent(), is(true));
        assertThat(resRM2.isPresent(), is(true));
        assertThat(resRM1.get().getStockQuantity(), is(rm1InitialStock + StockController.RESTOCK_QUANTITY));
        assertThat(resRM2.get().getStockQuantity(), is(rm2InitialStock + StockController.RESTOCK_QUANTITY));
    }

    @AfterEach
    void tearDownAfterEach() {
        dishRepository.delete(dish1.getDishId());
        dishRepository.delete(dish2.getDishId());
        dishRepository.delete(dish3.getDishId());
        rawMaterialRepository.delete(rawMaterial1.getRawMaterialId());
        rawMaterialRepository.delete(rawMaterial2.getRawMaterialId());
    }
}
