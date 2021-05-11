package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.model.User;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.UserRepositoryImpl;
import org.junit.jupiter.api.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class TestAssistantServiceController {

    static TableRepositoryImpl tableRepository;
    static UserRepositoryImpl userRepository;
    static AssistantServiceController assistantServiceController;
    static Table table, table2;
    static User user;

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
    @DisplayName("La table change de status car la table est sale")
    void verifyServingTableChangeStatus() {
        assistantServiceController.serveTable(table);
        Table result = tableRepository.findById(table.getTableId()).get();
        assertThat(result.getState(),equalTo(TableStates.LIBRE));
    }

    @Test
    @DisplayName("La table ne change pas de status car elle est occupée")
    void verifyServingTableDontChangeStatus() {
        assistantServiceController.serveTable(table2);
        Table result = tableRepository.findById(table2.getTableId()).get();
        assertThat(result.getState(),equalTo(TableStates.OCCUPEE));
    }

    @AfterEach
    void tearDownAfterEach() {
        tableRepository.delete(table.getTableId());
        tableRepository.delete(table2.getTableId());
        userRepository.delete(user.getUserId());
    }
}
