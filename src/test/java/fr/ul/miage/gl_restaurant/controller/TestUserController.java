package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;
import fr.ul.miage.gl_restaurant.utilities.InputUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.Collectors;

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
        Authentification auth = new Authentification();
        auth.signIn("luct");
        userController = new CuisinierController(auth);
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
    @DisplayName("askTableId() retourne l'id de la table demandée")
    void verifyAskTableIdSucceed() {
        List<Table> tables = Arrays.asList(table1Floor1, table2Floor1, table1Floor2);
        List<String> tableIds = tables.stream().map(t -> t.getTableId().toString()).collect(Collectors.toList());
        try (MockedStatic<InputUtils> utilities = Mockito.mockStatic(InputUtils.class)) {
            utilities.when(() -> InputUtils.readInputInArray(tableIds)).thenReturn(table1Floor1.getTableId().toString());
            Long id = userController.askTableId(tables);
            assertThat(id, equalTo(table1Floor1.getTableId()));
        }
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

    @Test
    @DisplayName("displayActions() affiche les actions du cuisinier")
    void verifyDisplayActionsReturnsCuisinierActions() {
        var expected = new TextStringBuilder();
        expected.appendln("=".repeat(50))
                .appendln(StringUtils.center("ACCUEIL (Cuisinier)", 50))
                .appendln("=".repeat(50))
                .appendln("0 : Se déconnecter")
                .appendln("1 : Préparer une commande")
                .appendln("2 : Créer un plat")
                .appendln("3 : Modifier un plat")
                .appendln("4 : Supprimer un plat")
                .appendln("5 : Afficher les commandes à préparer")
                .appendln("6 : Afficher la carte du jour")
                .appendln("7 : Afficher les stocks")
                .appendln("8 : Afficher le temps de préparation moyen");
        assertThat(userController.displayActions(), equalTo(expected.toString()));
    }

    @Test
    @DisplayName("getActions() récupère les actions du cuisinier")
    void verifyGetActionsSucceed() {
        Set<String> actions = userController.getActions();
        assertThat(actions.size(), is(9));
    }

    @Test
    @DisplayName("Affiche les sous-actions du serveur")
    void verifyDisplaySubActionsReturnsServeurSubActions() {
        Authentification auth2 = new Authentification();
        auth2.signIn("chaumontt");
        UserController userController2 = new ServeurController(auth2);
        var expected = new TextStringBuilder();
        expected.appendln("0 : Retour")
                .appendln("1 : Afficher les informations de la table")
                .appendln("2 : Ajouter un article à la table")
                .appendln("3 : Valider et transmettre la commande en cours à la cuisine")
                .appendln("4 : Servir la commande");
        assertThat(userController2.displaySubActions(((ServeurController) userController2).getSubActions()), equalTo(expected.toString()));
    }

    @AfterEach
    void tearDownAfterEach() {
        tableRepository.delete(table1Floor1.getTableId());
        tableRepository.delete(table2Floor1.getTableId());
        tableRepository.delete(table1Floor2.getTableId());
    }
}
