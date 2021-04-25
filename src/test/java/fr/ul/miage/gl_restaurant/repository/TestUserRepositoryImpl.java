package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.model.User;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

class TestUserRepositoryImpl {

    static UserRepositoryImpl userRepository;
    static User user1, user2;

    @BeforeAll
    static void initializeBeforeAll() {
        userRepository = new UserRepositoryImpl(Environment.TEST);
    }

    @BeforeEach
    void initializeBeforeEach() {
        user1 = userRepository.save(new User("userTest1", "Test1", "User1", Roles.SERVEUR));
        user2 = userRepository.save(new User("userTest2", "Test2", "User2", Roles.DIRECTEUR));
    }

    @Test
    @DisplayName("findAll() récupère tous les éléments")
    void verifyFindAllReturnsAllElements() {
        List<User> result = userRepository.findAll();
        // Déjà 5 données de test + les deux insérées
        assertThat(result.size(), is(7));
    }

    @Test
    @DisplayName("findById() récupère le bon utilisateur")
    void verifyFindByIdGetsUser() {
        Optional<User> result = userRepository.findById(user1.getUserId());
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getLogin(), equalTo(user1.getLogin()));
    }

    @Test
    @DisplayName("findById() ne récupère rien")
    void verifyFindByIdGetsNothing() {
        Optional<User> result = userRepository.findById(999999999999999999L);
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("findByLogin() récupère l'utilisateur")
    void verifyFindByLoginGetsUser() {
        Optional<User> result = userRepository.findByLogin(user1.getLogin());
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getLogin(), equalTo(user1.getLogin()));
    }

    @Test
    @DisplayName("findByLogin() ne récupère rien")
    void verifyFindByLoginGetsNothing() {
        Optional<User> result = userRepository.findByLogin("this login does not exist :)");
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("L'insertion fonctionne")
    void verifySaveInsertElement() {
        User user = userRepository.save(new User("testInsertion1", "Test1", "Insertion1", Roles.DIRECTEUR));
        assertNotNull(user.getUserId());
        User result = userRepository.findById(user.getUserId()).get();
        assertThat(result.getLogin(), equalTo("testInsertion1"));
        assertThat(result.getLastName(), equalTo("Test1"));
        assertThat(result.getFirstName(), equalTo("Insertion1"));
        assertThat(result.getRole(), is(Roles.DIRECTEUR));
        userRepository.delete(user.getUserId());
    }

    @Test
    @DisplayName("L'insertion échoue à cause d'un login dupliqué")
    void verifySaveWithSameLoginFail() {
        User user1Bis = new User("userTest1", "Test1Bis", "User1Bis", Roles.SERVEUR);
        User result = userRepository.save(user1Bis);
        assertNull(result.getUserId());
    }

    @Test
    @DisplayName("La modification de l'utilisateur fonctionne")
    void verifyUpdateSucceed() {
        user1.setLogin("TestUpdate1");
        user1.setRole(Roles.ASSISTANT_SERVICE);
        userRepository.update(user1);
        User result = userRepository.findById(user1.getUserId()).get();
        assertThat(result.getLogin(), equalTo("TestUpdate1"));
        assertThat(result.getRole(), is(Roles.ASSISTANT_SERVICE));
    }

    @Test
    @DisplayName("La modification ne s'effectue pas car l'utilisateur n'existe pas")
    void verifyUpdateFailBecauseUserDoesNotExist() {
        User user = new User("TestUpdate1", "TestUpdate1", "TestUpdate1", Roles.SERVEUR);
        userRepository.update(user);
        Optional<User> result = userRepository.findById(user.getUserId());
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    @DisplayName("La modification ne fonctionne pas car le login existe déjà")
    void verifyUpdateFailBecauseLoginAlreadyExists() {
        user1.setLogin("userTest2");
        user1 = userRepository.update(user1);
        assertThat(user1.getLogin(), equalTo("userTest1"));
    }

    @Test
    @DisplayName("La suppression de l'utilisateur fonctionne")
    void verifyDeleteSucceed() {
        User user = new User("TestDelete1", "TestDelete1", "TestDelete1", Roles.SERVEUR);
        user = userRepository.save(user);
        int totalUsers = userRepository.findAll().size();
        userRepository.delete(user.getUserId());
        int newTotalUsers = userRepository.findAll().size();
        assertThat(newTotalUsers, equalTo(totalUsers - 1));
    }

    @Test
    @DisplayName("La suppression de ne fonctionne pas car l'utilisateur n'existe pas")
    void verifyDeleteFailBecauseUserDoesNotExist() {
        User user = new User("TestDelete1", "TestDelete1", "TestDelete1", Roles.SERVEUR);
        int totalUsers = userRepository.findAll().size();
        userRepository.delete(user.getUserId());
        int newTotalUsers = userRepository.findAll().size();
        assertThat(newTotalUsers, equalTo(totalUsers));
    }

    @AfterEach
    void tearDownAfterEach() {
        userRepository.delete(user1.getUserId());
        userRepository.delete(user2.getUserId());
    }

    @AfterAll
    static void tearDownAfterAll() {
        try {
            userRepository.connection.close();
            userRepository = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
