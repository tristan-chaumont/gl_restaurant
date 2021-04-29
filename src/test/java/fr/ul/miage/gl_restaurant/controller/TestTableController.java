package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.constants.MenuTypes;
import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Dish;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.model.User;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.UserRepositoryImpl;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

public class TestTableController {

    static TableRepositoryImpl tableRepository;
    static UserRepositoryImpl userRepository;
    static TableController tableController;
    static Table table;
    static User user;

    @BeforeAll
    static void initializeBeforeAll(){
        tableRepository = new TableRepositoryImpl(Environment.TEST);
        userRepository = new UserRepositoryImpl(Environment.TEST);
    }

    @BeforeEach
    void initializeBeforeEach() {
        user = new User("ttcUser1", "ttc", "User1", Roles.SERVEUR);
        userRepository.save(user);
        table = new Table(1, TableStates.LIBRE, 4, user);
        tableRepository.save(table);
        tableController = new TableController();
    }

    @Test
    @DisplayName("Le serveur a bien été affecté")
    void verifyAffectServerSucceed() {
        User userTest = new User("ttcUser2", "ttc", "User2", Roles.SERVEUR);
        userRepository.save(userTest);
        tableController.affectServer(table,userTest);
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
        tableController.affectServer(table,userTest);
        tableRepository.update(table);
        Table result = tableRepository.findById(table.getTableId()).get();
        assertThat(result.getUser(), equalTo(user));
        userRepository.delete(userTest.getUserId());
    }

    @Test
    @DisplayName("Le serveur n'est pas affecté car il n'existe pas")
    void verifyAffectServerFailedBecauseUserDoesNotExist() {
        User userTest = new User(999999L,"ttcUser2", "ttc", "User2", Roles.SERVEUR);
        tableController.affectServer(table,userTest);
        tableRepository.update(table);
        Table result = tableRepository.findById(table.getTableId()).get();
        assertThat(result.getUser(), equalTo(user));
        userRepository.delete(userTest.getUserId());
    }



    @AfterEach
    void tearDownAfterEach() {
        tableRepository.delete(table.getTableId());
        userRepository.delete(user.getUserId());
    }

    @AfterAll
    static void tearDownAfterAll() {
        try {
            userRepository.connection.close();
            userRepository = null;
            tableRepository.connection.close();
            tableRepository = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
