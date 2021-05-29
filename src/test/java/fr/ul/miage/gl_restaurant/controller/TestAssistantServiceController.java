package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.model.User;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.UserRepositoryImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestAssistantServiceController {

    static TableRepositoryImpl tableRepository;
    static UserRepositoryImpl userRepository;

    @Mock
    Authentification auth;

    @Spy
    @InjectMocks
    AssistantServiceController assistantServiceController = new AssistantServiceController(new Authentification());

    Table table, table2;
    User user;

    @BeforeAll
    static void initializeBeforeAll(){
        tableRepository = TableRepositoryImpl.getInstance();
        userRepository = UserRepositoryImpl.getInstance();
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

    @Test
    @DisplayName("callAction exécute la déconnexion")
    void verifyCallActionDisonnectSucceed() {
        doNothing().when(auth).disconnect();
        assistantServiceController.callAction(0);
        verify(auth, times(1)).disconnect();
    }

    @Test
    @DisplayName("callAction exécute l'affiche des tables par étage")
    void verifyCallActionPrintUtilsSucceed() {
        doNothing().when(assistantServiceController).printTablesByFloor();
        assistantServiceController.callAction(1);
        verify(assistantServiceController, times(1)).printTablesByFloor();
    }

    @Test
    @DisplayName("callAction exécute layTable")
    void verifyCallActionLayTableSucceed() {
        doNothing().when(assistantServiceController).layTable();
        assistantServiceController.callAction(2);
        verify(assistantServiceController, times(1)).layTable();
    }

    @Test
    @DisplayName("callAction n'exécute rien")
    void verifyCallActionExecutesNothing() {
        assistantServiceController.callAction(-1);
        verify(auth, times(0)).disconnect();
        verify(assistantServiceController, times(0)).printTablesByFloor();
        verify(assistantServiceController, times(0)).layTable();
    }

    @AfterEach
    void tearDownAfterEach() {
        tableRepository.delete(table.getTableId());
        tableRepository.delete(table2.getTableId());
        userRepository.delete(user.getUserId());
    }
}
