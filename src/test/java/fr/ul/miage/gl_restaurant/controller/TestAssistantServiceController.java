package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.model.User;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.UserRepositoryImpl;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TestAssistantServiceController {

    static TableRepositoryImpl tableRepository;
    static UserRepositoryImpl userRepository;
    static AssistantServiceController assistantServiceController;
    Table table, table2;
    User user;

    @BeforeAll
    static void initializeBeforeAll(){
        tableRepository = TableRepositoryImpl.getInstance();
        userRepository = UserRepositoryImpl.getInstance();
        assistantServiceController = new AssistantServiceController(new Authentification());
    }

    @BeforeEach
    void initializeBeforeEach() {
        user = new User("ttcUser1", "ttc", "User1", Roles.SERVEUR);
        userRepository.save(user);
        table = new Table(1, TableStates.SALE, 4, user);
        tableRepository.save(table);
        table2 = new Table(1, TableStates.OCCUPEE, 4, user);
        tableRepository.save(table2);
    }

    @Test
    @DisplayName("Récupère toutes les tables sales")
    void verifyGetDirtyTablesSucceed() {
        List<Table> tables = Arrays.asList(table, table2);
        List<Table> result = assistantServiceController.getDirtyTables(tables);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getState(), is(TableStates.SALE));
    }

    @Test
    @DisplayName("La table change de statut car la table est sale (sale vers libre)")
    void verifyLayTableChangeStatus() {
        boolean result = assistantServiceController.layTable(table);
        Optional<Table> tableResult = tableRepository.findById(table.getTableId());
        assertThat(tableResult.isPresent(), is(true));
        assertThat(result, is(true));
        assertThat(tableResult.get().getState(),equalTo(TableStates.LIBRE));
    }

    @Test
    @DisplayName("La table ne change pas de statut car elle est occupée (sale vers libre)")
    void verifyLayTableDoesntChangeStatus() {
        boolean result = assistantServiceController.layTable(table2);
        Optional<Table> tableResult = tableRepository.findById(table2.getTableId());
        assertThat(tableResult.isPresent(), is(true));
        assertThat(result, is(false));
        assertThat(tableResult.get().getState(),equalTo(TableStates.OCCUPEE));
    }

    @AfterEach
    void tearDownAfterEach() {
        tableRepository.delete(table.getTableId());
        tableRepository.delete(table2.getTableId());
        userRepository.delete(user.getUserId());
    }
}
