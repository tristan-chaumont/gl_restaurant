package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.MenuTypes;
import fr.ul.miage.gl_restaurant.constants.Units;
import fr.ul.miage.gl_restaurant.model.Dish;
import fr.ul.miage.gl_restaurant.model.RawMaterial;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestDishRepositoryImpl {

    static DishRepositoryImpl dishRepository;
    static RawMaterialRepositoryImpl rawMaterialRepository;
    static Dish dish1, dish2;
    static RawMaterial rawMaterial1;

    @BeforeAll
    static void initializeBeforeAll() {
        dishRepository = DishRepositoryImpl.getInstance();
        rawMaterialRepository = RawMaterialRepositoryImpl.getInstance();
    }

    @BeforeEach
    void initializeBeforeEach() {
        rawMaterial1 = rawMaterialRepository.save(new RawMaterial("Riz", 100, Units.KG));
        dish1 = new Dish("Saumon", "Poisson", MenuTypes.ADULTES, 8.5, false);
        dish2 = new Dish( "Steak Haché", "Viande", MenuTypes.ADULTES, 5.0, false);
        dish1.addRawMaterial(rawMaterial1, 1);
        dish2.addRawMaterial(rawMaterial1, 2);
        dish1 = dishRepository.save(dish1);
        dish2 = dishRepository.save(dish2);
    }

    /* FIND */

    @Test
    @DisplayName("findAll() récupère tous les éléments")
    void verifyFindAllReturnsAllElements() {
        List<Dish> result = dishRepository.findAll();
        assertThat(result.size(), is(2));
    }

    @Test
    @DisplayName("findById() récupère le bon plat")
    void verifyFindByIdGetsDish() {
        Optional<Dish> result = dishRepository.findById(dish1.getDishId());
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getDishName(), equalTo(dish1.getDishName()));
    }

    @Test
    @DisplayName("findById() ne récupère rien")
    void verifyFindByIdGetsNothing() {
        Optional<Dish> result = dishRepository.findById(999999999999999999L);
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("findByName() récupère le bon plat")
    void verifyFindByNameGetsDish() {
        Optional<Dish> result = dishRepository.findByName(dish1.getDishName());
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getDishName(), equalTo(dish1.getDishName()));
    }

    @Test
    @DisplayName("findByName() ne récupère rien")
    void verifyFindByLoginGetsNothing() {
        Optional<Dish> result = dishRepository.findByName("this dish does not exist :)");
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("findByCategory() récupère le bon plat")
    void verifyFindByCategoryGetsDish() {
        List<Dish> result = dishRepository.findByCategory("Poisson");
        assertThat(result.size(), is(1));
    }

    @Test
    @DisplayName("findByCategory() ne récupère rien")
    void verifyFindByCategoryGetsNothing() {
        List<Dish> result = dishRepository.findByCategory("this category does not exist :)");
        assertThat(result.size(), is(0));
    }

    @Test
    @DisplayName("findByRM() récupère le bon plat")
    void verifyFindByRMGetsDish() {
        List<Dish> result = dishRepository.findByRM(rawMaterial1.getRawMaterialId());
        assertThat(result.size(), is(2));
    }

    @Test
    @DisplayName("findByRM() ne récupère rien")
    void verifyFindByRMGetsNothing() {
        List<Dish> result = dishRepository.findByRM(9999L);
        assertThat(result.size(), is(0));
    }

    @Test
    @DisplayName("L'insertion fonctionne")
    void verifySaveInsertElement() {
        Dish dish = dishRepository.save(new Dish("Poisson pané", "Poisson", MenuTypes.ENFANTS, 4.0, false));
        assertNotNull(dish.getDishId());
        Dish result = dishRepository.findById(dish.getDishId()).get();
        assertThat(result.getDishName(), equalTo("Poisson pané"));
        assertThat(result.getCategory(), equalTo("Poisson"));
        assertThat(result.getMenuType(), is(MenuTypes.ENFANTS));
        assertThat(result.getPrice(), equalTo(4.0));
        dishRepository.delete(dish.getDishId());
    }

    @Test
    @DisplayName("L'insertion échoue à cause d'un nom dupliqué")
    void verifySaveWithSameWordingFail() {
        Dish dish1Bis = new Dish("Saumon", "Poisson", MenuTypes.ADULTES, 9.0, false);
        Dish result = dishRepository.save(dish1Bis);
        assertNull(result.getDishId());
    }

    @Test
    @DisplayName("La modification du plat fonctionne")
    void verifyUpdateSucceed() {
        dish1.setDishName("Poisson pané");
        dish1.setPrice(3.0);
        dishRepository.update(dish1);
        Dish result = dishRepository.findById(dish1.getDishId()).get();
        assertThat(result.getDishName(), equalTo("Poisson pané"));
        assertThat(result.getPrice(), equalTo(3.0));
    }

    @Test
    @DisplayName("La modification ne s'effectue pas car le plat n'existe pas")
    void verifyUpdateFailBecauseDishDoesNotExist() {
        Dish dish = new Dish(3L, "TestUpdate1", "TestUpdate1", MenuTypes.ADULTES, 2.0, false);
        dishRepository.update(dish);
        Optional<Dish> result = dishRepository.findById(dish.getDishId());
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    @DisplayName("La modification ne fonctionne pas car le nom existe déjà")
    void verifyUpdateFailBecauseLoginAlreadyExists() {
        dish1.setDishName("Steak Haché");
        dish1 = dishRepository.update(dish1);
        Optional<Dish> result = dishRepository.findById(dish1.getDishId());
        assertThat(result.get().getDishName(), equalTo("Saumon"));
    }

    @Test
    @DisplayName("La modification de la carte du jour fonctionne")
    void verifyUpdateDailyMenuSucceed() {
        dish1 = dishRepository.updateDailyMenu(dish1.getDishId(), true);
        Optional<Dish> result = dishRepository.findById(dish1.getDishId());
        assertThat(result.get().isDailyMenu(), is(true));
    }

    @Test
    @DisplayName("La modification de la carte du jour échoue, car le plat n'existe pas")
    void verifyUpdateDailyMenuFailBecauseDishDoesNotExist() {
        Dish newDish = new Dish("Saumon", "Poisson", MenuTypes.ADULTES, 8.5, false);
        newDish = dishRepository.updateDailyMenu(newDish.getDishId(), true);
        assertNull(newDish);
    }

    @Test
    @DisplayName("La suppression de l'utilisateur fonctionne")
    void verifyDeleteSucceed() {
        Dish dish = new Dish("TestDelete1", "TestDelete1", MenuTypes.ADULTES, 2.0, false);
        dish = dishRepository.save(dish);
        int totalUsers = dishRepository.findAll().size();
        dishRepository.delete(dish.getDishId());
        int newTotalUsers = dishRepository.findAll().size();
        assertThat(newTotalUsers, equalTo(totalUsers - 1));
    }

    @Test
    @DisplayName("La suppression de ne fonctionne pas car l'utilisateur n'existe pas")
    void verifyDeleteFailBecauseUserDoesNotExist() {
        Dish dish = new Dish(3L, "TestDelete1", "TestDelete1", MenuTypes.ADULTES, 2.0, false);
        int totalDishes = dishRepository.findAll().size();
        dishRepository.delete(dish.getDishId());
        int newTotalDishes = dishRepository.findAll().size();
        assertThat(newTotalDishes, equalTo(totalDishes));
    }

    /* RAW MATERIAL */

    @Test
    @DisplayName("Les matières premières qui composent un plat sont récupérées")
    void verifyFindRawMaterialsByDishIdReturnsAllElements() {
        Map<RawMaterial, Integer> rawMaterials = dishRepository.findRawMaterialsByDishId(dish1.getDishId());
        assertThat(rawMaterials.size(), is(1));
        assertThat(rawMaterials.get(rawMaterial1), is(1));
    }

    @Test
    @DisplayName("Les matières premières d'un nouveau plat sont sauvegardées")
    void verifySaveRawMaterialsByDishIdSucceed() {
        Map<RawMaterial, Integer> rawMaterials;
        Dish dish3 = new Dish("Poiscaille", "Poisson", MenuTypes.ADULTES, 8.5, false);
        dish3.addRawMaterial(rawMaterial1, 5);
        dish3 = dishRepository.save(dish3);
        assertNotNull(dish3.getDishId());
        rawMaterials = dishRepository.findRawMaterialsByDishId(dish3.getDishId());
        assertThat(rawMaterials.size(), is(1));
        assertThat(rawMaterials.get(rawMaterial1), is(5));
        dishRepository.delete(dish3.getDishId());
    }

    @Test
    @DisplayName("Les matières premières sont mises à jour correctement")
    void verifyUpdateRawMaterialByDishIdSucceed() {
        Dish dish3 = new Dish("Poiscaille", "Poisson", MenuTypes.ADULTES, 8.5, false);
        dish3.addRawMaterial(rawMaterial1, 5);
        dish3 = dishRepository.save(dish3);
        RawMaterial rawMaterial2 = new RawMaterial("Pastis", 50, Units.L);
        rawMaterialRepository.save(rawMaterial2);
        dish3.clearRawMaterials();
        dish3.addRawMaterial(rawMaterial2, 10);
        dish3 = dishRepository.update(dish3);
        Map<RawMaterial, Integer> rawMaterials = dishRepository.findRawMaterialsByDishId(dish3.getDishId());
        assertThat(rawMaterials.size(), is(1));
        assertThat(rawMaterials.get(rawMaterial2), is(10));
        dishRepository.delete(dish3.getDishId());
        rawMaterialRepository.delete(rawMaterial2.getRawMaterialId());
    }

    @Test
    @DisplayName("Les matières premières sont supprimées correctement")
    void verifyDeleteRawMaterialByDishIdSucceed() {
        dishRepository.delete(dish1.getDishId());
        Map<RawMaterial, Integer> rawMaterials = dishRepository.findRawMaterialsByDishId(dish1.getDishId());
        assertThat(rawMaterials.size(), is(0));
    }

    @AfterEach
    void tearDownAfterEach() {
        dishRepository.delete(dish1.getDishId());
        dishRepository.delete(dish2.getDishId());
        rawMaterialRepository.delete(rawMaterial1.getRawMaterialId());
    }
}
