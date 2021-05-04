package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.model.User;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.UserRepositoryImpl;
import org.junit.jupiter.api.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class TestTableController {

    static TableRepositoryImpl tableRepository;
    static UserRepositoryImpl userRepository;
    static TableController tableController;
    static Table table;
    static User user;

    @BeforeAll
    static void initializeBeforeAll(){
        tableRepository = TableRepositoryImpl.getInstance();
        userRepository = UserRepositoryImpl.getInstance();
        tableController = new TableController();
    }

    @BeforeEach
    void initializeBeforeEach() {
        user = new User("ttcUser1", "ttc", "User1", Roles.SERVEUR);
        userRepository.save(user);
        table = new Table(1, TableStates.LIBRE, 4, user);
        tableRepository.save(table);
    }

    @Test
    @DisplayName("Le serveur a bien été affecté")
    void verifyAffectServerSucceed() {
        User userTest = new User("ttcUser2", "ttc", "User2", Roles.SERVEUR);
        userRepository.save(userTest);
        tableController.assignServer(table,userTest);
        tableRepository.update(table);
        Table result = tableRepository.findById(table.getTableId()).get();
        assertThat(result.getUser(), equalTo(userTest));
        tableRepository.delete(table.getTableId());
        userRepository.delete(userTest.getUserId());
    }

    @Test
    @DisplayName("Le serveur n'a pas été affecté car ce n'est pas un serveur")
    void verifyAffectServerFailedBecauseItsNotAServer() {
        User userTest = new User("ttcUser2", "ttc", "User2", Roles.CUISINIER);
        userRepository.save(userTest);
        tableController.assignServer(table,userTest);
        tableRepository.update(table);
        Table result = tableRepository.findById(table.getTableId()).get();
        assertThat(result.getUser(), equalTo(user));
        userRepository.delete(userTest.getUserId());
    }

    @Test
    @DisplayName("Le serveur n'est pas affecté car il n'existe pas")
    void verifyAffectServerFailedBecauseUserDoesNotExist() {
        User userTest = new User(999999L,"ttcUser2", "ttc", "User2", Roles.SERVEUR);
        tableController.assignServer(table,userTest);
        tableRepository.update(table);
        Table result = tableRepository.findById(table.getTableId()).get();
        assertThat(result.getUser(), equalTo(user));
        userRepository.delete(userTest.getUserId());
    }

    @Test
    @DisplayName("La table change de status")
    void verifyChangeStatus() {
        tableController.changeState(table, TableStates.OCCUPEE);
        Table result = tableRepository.findById(table.getTableId()).get();
        assertThat(result.getState(),equalTo(TableStates.OCCUPEE));
    }



    @AfterEach
    void tearDownAfterEach() {
        tableRepository.delete(table.getTableId());
        userRepository.delete(user.getUserId());
    }
}
