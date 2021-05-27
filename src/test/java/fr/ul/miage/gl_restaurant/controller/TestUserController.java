package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;
import org.apache.commons.text.TextStringBuilder;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TestUserController {

    static TableRepositoryImpl tableRepository;
    static UserController userController;
    Table table1Floor1, table2Floor1, table1Floor2;

    @BeforeAll
    static void initializeBeforeAll() {
        tableRepository = TableRepositoryImpl.getInstance();
        userController = new CuisinierController(new Authentification());
    }

    @BeforeEach
    void initializeBeforeEach() {
        table1Floor1 = new Table(1, TableStates.SALE, 4, null);
        tableRepository.save(table1Floor1);
        table2Floor1 = new Table(1, TableStates.SALE, 4, null);
        tableRepository.save(table2Floor1);
        table1Floor2 = new Table(2, TableStates.SALE, 4, null);
        tableRepository.save(table1Floor2);
    }

    @Test
    @DisplayName("getFloorsTable() retourne une map qui contient en clé l'étage et en valeur la liste des tables de l'étage")
    void verifyGetFloorsTableReturnsTwoFloors() {
        Map<Integer, Set<Table>> result = userController.getFloorsTables(Arrays.asList(table1Floor1, table2Floor1, table1Floor2));
        assertThat(result.size(), is(2));
        assertThat(result.get(1).size(), is(2));
        assertThat(result.get(2).size(), is(1));
        assertThat(result.get(1).contains(table1Floor1), is(true));
        assertThat(result.get(1).contains(table2Floor1), is(true));
        assertThat(result.get(2).contains(table1Floor2), is(true));
    }

    @Test
    @DisplayName("Affiche toutes les tables par étage")
    void verifyDisplayTablesByFloorSucceed() {
        var expected = new TextStringBuilder();
        expected.append("Étage 1 : ")
                .appendln("[n°%d][n°%d]", table1Floor1.getTableId(), table2Floor1.getTableId())
                .append("Étage 2 : ")
                .appendln("[n°%d]", table1Floor2.getTableId());
        String result = userController.displayTablesByFloor(Arrays.asList(table1Floor1, table2Floor1, table1Floor2));
        assertThat(result, equalTo(expected.toString()));
    }

    @Test
    @DisplayName("Affiche une chaine vide s'il n'y a aucune table")
    void verifyDisplayTablesByFloorReturnsEmptyString() {
        String result = userController.displayTablesByFloor(Collections.emptyList());
        assertThat(result, equalTo(""));
    }

    @AfterEach
    void tearDownAfterEach() {
        tableRepository.delete(table1Floor1.getTableId());
        tableRepository.delete(table2Floor1.getTableId());
        tableRepository.delete(table1Floor2.getTableId());
    }
}
