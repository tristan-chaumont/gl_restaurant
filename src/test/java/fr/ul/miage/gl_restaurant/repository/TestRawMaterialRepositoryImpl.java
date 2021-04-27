package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.constants.Units;
import fr.ul.miage.gl_restaurant.model.RawMaterial;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestRawMaterialRepositoryImpl {

    static RawMaterialRepositoryImpl rawMaterialRepository;

    static RawMaterial rawMaterial1;

    @BeforeAll
    static void initializeBeforeAll() {
        rawMaterialRepository = new RawMaterialRepositoryImpl(Environment.TEST);
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
    @DisplayName("L'insertion fonctionne")
    void verifySaveInsertElement() {
        RawMaterial rawMaterial = rawMaterialRepository.save(new RawMaterial("Farine", 100, Units.KG));
        assertNotNull(rawMaterial.getRawMaterialId());
        RawMaterial result = rawMaterialRepository.findById(rawMaterial.getRawMaterialId()).get();
        assertThat(result.getRawMaterialName(), equalTo("Farine"));
        assertThat(result.getStockQuantity(), is(100));
        assertThat(result.getUnit(), equalTo("kg"));
        rawMaterialRepository.delete(rawMaterial.getRawMaterialId());
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

    @AfterAll
    static void tearDownAfterAll() {
        try {
            rawMaterialRepository.connection.close();
            rawMaterialRepository = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
