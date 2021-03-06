package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.model.User;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestTableRepositoryImpl {

    static UserRepositoryImpl userRepository;
    static TableRepositoryImpl tableRepository;

    static Table table1, table2, table3;
    static User user1, user2;

    @BeforeAll
    static void initializeBeforeAll() {
        userRepository = UserRepositoryImpl.getInstance();
        tableRepository = TableRepositoryImpl.getInstance();
    }

    @BeforeEach
    void initializeBeforeEach() {
        user1 = userRepository.save(new User("userTableTest1", "Test1", "UserTable1", Roles.SERVEUR));
        user2 = userRepository.save(new User("userTableTest2", "Test2", "UserTable2", Roles.SERVEUR));
        table1 = tableRepository.save(new Table(1, TableStates.OCCUPEE, 5, user1));
        table2 = tableRepository.save(new Table(2, TableStates.LIBRE, 3, user2));
        table3 = tableRepository.save(new Table(3, TableStates.RESERVEE, 2, user2));
    }

    @Test
    @DisplayName("findAll() récupère tous les éléments")
    void verifyFindAllReturnsAllElements() {
        List<Table> result = tableRepository.findAll();
        assertThat(result.size(), is(3));
    }

    @Test
    @DisplayName("findByUsrId() récupère tous les éléments lié à cet utilisateur")
    void verifyFindByUserIdReturnsOneElement() {
        List<Table> result = tableRepository.findByUserId(user1.getUserId());
        assertThat(result.size(), is(1));
    }

    @Test
    @DisplayName("findById() récupère la bonne table")
    void verifyFindByIdGetsTable() {
        Optional<Table> result = tableRepository.findById(table1.getTableId());
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getTableId(), equalTo(table1.getTableId()));
    }

    @Test
    @DisplayName("findById() ne récupère rien")
    void verifyFindByIdGetsNothing() {
        Optional<Table> result = tableRepository.findById(999999999999999999L);
        assertThat(result.isPresent(), is(false));
        result = tableRepository.findById(null);
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("findByState() récupère les bons éléments")
    void verifyFindByStateGetsRightElements() {
        List<Table> result = tableRepository.findByState(TableStates.RESERVEE);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getTableId(), equalTo(table3.getTableId()));
    }

    @Test
    @DisplayName("L'insertion fonctionne")
    void verifySaveInsertElement() {
        User user = userRepository.save(new User("testInsertion1", "Test1", "Insertion1", Roles.DIRECTEUR));
        Table table = tableRepository.save(new Table(2, TableStates.LIBRE, 3, user));
        assertNotNull(table.getTableId());
        Optional<Table> result = tableRepository.findById(table.getTableId());
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getFloor(), equalTo(2));
        assertThat(result.get().getState(), is(TableStates.LIBRE));
        assertThat(result.get().getPlaces(), equalTo(3));
        assertThat(result.get().getUser(), equalTo(user));
        tableRepository.delete(table.getTableId());
        userRepository.delete(user.getUserId());
    }

    @Test
    @DisplayName("La modification de la table fonctionne")
    void verifyUpdateSucceed() {
        table1.setFloor(4);
        table1.setState(TableStates.LIBRE);
        tableRepository.update(table1);
        Optional<Table> result = tableRepository.findById(table1.getTableId());
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getFloor(), is(4));
        assertThat(result.get().getState(), is(TableStates.LIBRE));
    }

    @Test
    @DisplayName("La modification ne s'effectue pas car la table n'existe pas")
    void verifyUpdateFailBecauseTableDoesNotExist() {
        Table table = new Table(5, TableStates.SALE, 5, null);
        tableRepository.update(table);
        Optional<Table> result = tableRepository.findById(table.getTableId());
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    @DisplayName("La suppression de la table fonctionne")
    void verifyDeleteSucceed() {
        Table table = new Table(1, TableStates.SALE, 2, user1);
        table = tableRepository.save(table);
        int totalTables = tableRepository.findAll().size();
        tableRepository.delete(table.getTableId());
        int newTotalTables = tableRepository.findAll().size();
        assertThat(newTotalTables, equalTo(totalTables - 1));
    }

    @Test
    @DisplayName("La suppression de ne fonctionne pas car la table n'existe pas")
    void verifyDeleteFailBecauseTableDoesNotExist() {
        Table table = new Table(2, TableStates.SALE, 5, null);
        int totalTables = userRepository.findAll().size();
        tableRepository.delete(table.getTableId());
        int newTotalTables = userRepository.findAll().size();
        assertThat(newTotalTables, equalTo(totalTables));
    }

    @AfterEach
    void tearDownAfterEach() {
        tableRepository.delete(table1.getTableId());
        tableRepository.delete(table2.getTableId());
        tableRepository.delete(table3.getTableId());
        userRepository.delete(user1.getUserId());
        userRepository.delete(user2.getUserId());
    }
}
