package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.MenuTypes;
import fr.ul.miage.gl_restaurant.constants.Units;
import fr.ul.miage.gl_restaurant.model.Dish;
import fr.ul.miage.gl_restaurant.model.Meal;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.model.RawMaterial;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestRawMaterialRepositoryImpl {

    static RawMaterialRepositoryImpl rawMaterialRepository;

    static RawMaterial rawMaterial1;

    @BeforeAll
    static void initializeBeforeAll() {
        rawMaterialRepository = RawMaterialRepositoryImpl.getInstance();
    }

    @BeforeEach
    void initializeBeforeEach() {
        rawMaterial1 = rawMaterialRepository.save(new RawMaterial("Riz", 100, Units.KG));
    }

    @Test
    @DisplayName("findAll() récupère tous les éléments")
    void verifyFindAllReturnsAllElements() {
        List<RawMaterial> result = rawMaterialRepository.findAll();
        assertThat(result.size(), is(1));
    }

    @Test
    @DisplayName("findById() récupère la bonne matière première")
    void verifyFindByIdGetsRawMaterial() {
        Optional<RawMaterial> result = rawMaterialRepository.findById(rawMaterial1.getRawMaterialId());
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getRawMaterialId(), equalTo(rawMaterial1.getRawMaterialId()));
    }

    @Test
    @DisplayName("findById() ne récupère rien")
    void verifyFindByIdGetsNothing() {
        Optional<RawMaterial> result = rawMaterialRepository.findById(999999999999999999L);
        assertThat(result.isPresent(), is(false));
        result = rawMaterialRepository.findById(null);
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("findByName() récupère la matière première")
    void verifyFindByNameGetsRawMaterial() {
        Optional<RawMaterial> result = rawMaterialRepository.findByName(rawMaterial1.getRawMaterialName());
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getRawMaterialName(), equalTo(rawMaterial1.getRawMaterialName()));
    }

    @Test
    @DisplayName("findByName() ne récupère rien")
    void verifyFindByLoginGetsNothing() {
        Optional<RawMaterial> result = rawMaterialRepository.findByName("this name does not exist :)");
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("findOutOfStock() récupère les matières premières en rupture de stock")
    void verifyFindOutOfStockGetsRightRawMaterials() {
        final int THRESHOLD = 20;
        RawMaterial newRM1 = rawMaterialRepository.save(new RawMaterial("Salade", 21, Units.KG));
        RawMaterial newRM2 = rawMaterialRepository.save(new RawMaterial("Lait", 10, Units.L));
        RawMaterial newRM3 = rawMaterialRepository.save(new RawMaterial("Oeufs", 19, Units.U));
        List<RawMaterial> result = rawMaterialRepository.findOutOfStock(THRESHOLD);
        assertThat(result.size(), is(2));
        assertThat(result.get(0).getRawMaterialName(), either(equalTo("Lait")).or(equalTo("Oeufs")));
        assertThat(result.get(1).getRawMaterialName(), either(equalTo("Lait")).or(equalTo("Oeufs")));
        rawMaterialRepository.delete(newRM1.getRawMaterialId());
        rawMaterialRepository.delete(newRM2.getRawMaterialId());
        rawMaterialRepository.delete(newRM3.getRawMaterialId());
    }

    @Test
    @DisplayName("L'insertion fonctionne")
    void verifySaveInsertElement() {
        RawMaterial rawMaterial = rawMaterialRepository.save(new RawMaterial("Farine", 100, Units.KG));
        assertNotNull(rawMaterial.getRawMaterialId());
        RawMaterial result = rawMaterialRepository.findById(rawMaterial.getRawMaterialId()).get();
        assertThat(result.getRawMaterialName(), equalTo("Farine"));
        assertThat(result.getStockQuantity(), is(100));
        assertThat(result.getUnit(), is(Units.KG));
        rawMaterialRepository.delete(rawMaterial.getRawMaterialId());
    }

    @Test
    @DisplayName("L'insertion échoue à cause d'un nom dupliqué")
    void verifySaveWithSameNameFail() {
        RawMaterial rawMaterial= new RawMaterial("Riz", 50, Units.U);
        RawMaterial result = rawMaterialRepository.save(rawMaterial);
        assertNull(result.getRawMaterialId());
    }

    @Test
    @DisplayName("La modification de la matière première fonctionne")
    void verifyUpdateSucceed() {
        rawMaterial1.setRawMaterialName("Farine");
        rawMaterialRepository.update(rawMaterial1);
        RawMaterial result = rawMaterialRepository.findById(rawMaterial1.getRawMaterialId()).get();
        assertThat(result.getRawMaterialName(), equalTo("Farine"));
    }

    @Test
    @DisplayName("La modification ne s'effectue pas car la matière première n'existe pas")
    void verifyUpdateFailBecauseRawMaterialDoesNotExist() {
        RawMaterial rawMaterial = new RawMaterial("Farine", 100, Units.KG);
        rawMaterialRepository.update(rawMaterial);
        Optional<RawMaterial> result = rawMaterialRepository.findById(rawMaterial.getRawMaterialId());
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    @DisplayName("La modification ne fonctionne pas car le nom existe déjà")
    void verifyUpdateFailBecauseNameAlreadyExists() {
        RawMaterial rawMaterial = new RawMaterial("Farine", 100, Units.KG);
        rawMaterialRepository.save(rawMaterial);
        rawMaterial.setRawMaterialName("Riz");
        rawMaterial = rawMaterialRepository.update(rawMaterial);
        assertThat(rawMaterial.getRawMaterialName(), equalTo("Farine"));
        rawMaterialRepository.delete(rawMaterial.getRawMaterialId());
    }

    @Test
    @DisplayName("Les matières premières en rupture de stocks sont réincrémentées de 100 unités")
    void verifyUpdateOutOfStockSucceed() {
        RawMaterial newRM1 = rawMaterialRepository.save(new RawMaterial("Salade", 21, Units.KG));
        RawMaterial newRM2 = rawMaterialRepository.save(new RawMaterial("Lait", 10, Units.L));
        RawMaterial newRM3 = rawMaterialRepository.save(new RawMaterial("Oeufs", 19, Units.U));
        final int THRESHOLD = 20;
        final int RESTOCK = 100;
        List<RawMaterial> outOfStockRawMaterials = rawMaterialRepository.findOutOfStock(THRESHOLD);
        rawMaterialRepository.updateOutOfStock(THRESHOLD, RESTOCK);
        List<RawMaterial> result = new ArrayList<>();
        outOfStockRawMaterials.forEach(
                rm -> result.add(rawMaterialRepository.findById(rm.getRawMaterialId()).get())
        );
        assertThat(result.get(0).getStockQuantity(), is(outOfStockRawMaterials.get(0).getStockQuantity() + RESTOCK));
        assertThat(result.get(1).getStockQuantity(), is(outOfStockRawMaterials.get(1).getStockQuantity() + RESTOCK));
        rawMaterialRepository.delete(newRM1.getRawMaterialId());
        rawMaterialRepository.delete(newRM2.getRawMaterialId());
        rawMaterialRepository.delete(newRM3.getRawMaterialId());
    }

    @Test
    @DisplayName("Le stock de matières premières de chaque plat d'une commande est retiré du stock total")
    void verifyUpdateStockBasedOnTakenOrderSucceed() {
        RawMaterial newRM1 = rawMaterialRepository.save(new RawMaterial("Salade", 50, Units.KG));
        RawMaterial newRM2 = rawMaterialRepository.save(new RawMaterial("Lait", 50, Units.L));
        RawMaterial newRM3 = rawMaterialRepository.save(new RawMaterial("Oeufs", 50, Units.U));
        Dish dish1 = new Dish("Test1", "Test1", MenuTypes.ADULTES, 5.0, true);
        dish1.addRawMaterial(newRM1, 10);
        dish1.addRawMaterial(newRM2, 20);
        Dish dish2 = new Dish("Test2", "Test2", MenuTypes.ADULTES, 5.0, true);
        dish2.addRawMaterial(newRM2, 2);
        dish2.addRawMaterial(newRM3, 50);
        Order order = new Order(Timestamp.from(Instant.now()), null, new Meal());
        order.addDish(dish1, 2);
        order.addDish(dish2, 1);
        rawMaterialRepository.updateStockBasedOnTakenOrder(order);
        newRM1 = rawMaterialRepository.findById(newRM1.getRawMaterialId()).get();
        newRM2 = rawMaterialRepository.findById(newRM2.getRawMaterialId()).get();
        newRM3 = rawMaterialRepository.findById(newRM3.getRawMaterialId()).get();
        assertThat(newRM1.getStockQuantity(), is(30));
        assertThat(newRM2.getStockQuantity(), is(8));
        assertThat(newRM3.getStockQuantity(), is(0));
        rawMaterialRepository.delete(newRM1.getRawMaterialId());
        rawMaterialRepository.delete(newRM2.getRawMaterialId());
        rawMaterialRepository.delete(newRM3.getRawMaterialId());
    }

    @Test
    @DisplayName("La suppression de la matière première fonctionne")
    void verifyDeleteSucceed() {
        RawMaterial rawMaterial = new RawMaterial("Farine", 100, Units.KG);
        rawMaterial = rawMaterialRepository.save(rawMaterial);
        int totalRawMaterials = rawMaterialRepository.findAll().size();
        rawMaterialRepository.delete(rawMaterial.getRawMaterialId());
        int newTotalRawMaterials = rawMaterialRepository.findAll().size();
        assertThat(newTotalRawMaterials, equalTo(totalRawMaterials - 1));
    }

    @Test
    @DisplayName("La suppression de ne fonctionne pas car la matière première n'existe pas")
    void verifyDeleteFailBecauseRawMaterialDoesNotExist() {
        RawMaterial rawMaterial = new RawMaterial("Farine", 100, Units.KG);
        int totalRawMaterials = rawMaterialRepository.findAll().size();
        rawMaterialRepository.delete(rawMaterial.getRawMaterialId());
        int newTotalRawMaterials = rawMaterialRepository.findAll().size();
        assertThat(newTotalRawMaterials, equalTo(totalRawMaterials));
    }

    @AfterEach
    void tearDownAfterEach() {
        rawMaterialRepository.delete(rawMaterial1.getRawMaterialId());
    }
}
