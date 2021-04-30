package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Reservation;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.model.User;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestReservationRepositoryImpl {

    static UserRepositoryImpl userRepository;
    static TableRepositoryImpl tableRepository;
    static ReservationRepositoryImpl reservationRepository;

    static Reservation reservation1;
    static Table table1;
    static User user1;

    @BeforeAll
    static void initializeBeforeAll() {
        userRepository = UserRepositoryImpl.getInstance();
        reservationRepository = ReservationRepositoryImpl.getInstance();
        tableRepository = TableRepositoryImpl.getInstance();
    }

    @BeforeEach
    void initializeBeforeEach() {
        user1 = userRepository.save(new User("testUserReservation1", "Test1", "UserReservation1", Roles.SERVEUR));
        table1 = tableRepository.save(new Table(1, TableStates.OCCUPEE, 5, user1));
        reservation1 = reservationRepository.save(new Reservation(true, table1));
    }

    @Test
    @DisplayName("findAll() récupère tous les éléments")
    void verifyFindAllReturnsAllElements() {
        List<Reservation> result = reservationRepository.findAll();
        assertThat(result.size(), is(1));
    }

    @Test
    @DisplayName("findById() récupère la bonne réservation")
    void verifyFindByIdGetsReservation() {
        Optional<Reservation> result = reservationRepository.findById(reservation1.getReservationId());
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getReservationId(), equalTo(reservation1.getReservationId()));
    }

    @Test
    @DisplayName("findById() ne récupère rien")
    void verifyFindByIdGetsNothing() {
        Optional<Reservation> result = reservationRepository.findById(999999999999999999L);
        assertThat(result.isPresent(), is(false));
        result = reservationRepository.findById(null);
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("L'insertion fonctionne")
    void verifySaveInsertElement() {
        User user = userRepository.save(new User("testInsertion1", "Test1", "Insertion1", Roles.DIRECTEUR));
        Table table = tableRepository.save(new Table(2, TableStates.LIBRE, 3, user));
        Reservation reservation = reservationRepository.save(new Reservation(true, table));
        assertNotNull(reservation.getReservationId());
        Reservation result = reservationRepository.findById(reservation.getReservationId()).get();
        assertThat(result.isLunch(), is(true));
        assertThat(result.getTable(), equalTo(table));
        reservationRepository.delete(reservation.getReservationId());
        tableRepository.delete(table.getTableId());
        userRepository.delete(user.getUserId());
    }

    @Test
    @DisplayName("La modification de la table fonctionne")
    void verifyUpdateSucceed() {
        reservation1.setLunch(false);
        reservationRepository.update(reservation1);
        Reservation result = reservationRepository.findById(reservation1.getReservationId()).get();
        assertThat(result.isLunch(), is(false));
    }

    @Test
    @DisplayName("La modification ne s'effectue pas car la table n'existe pas")
    void verifyUpdateFailBecauseReservationDoesNotExist() {
        Reservation reservation = new Reservation(false, table1);
        reservationRepository.update(reservation);
        Optional<Reservation> result = reservationRepository.findById(reservation.getReservationId());
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    @DisplayName("La suppression de la table fonctionne")
    void verifyDeleteSucceed() {
        Reservation reservation = new Reservation(false, table1);
        reservation = reservationRepository.save(reservation);
        int totalReservations = reservationRepository.findAll().size();
        reservationRepository.delete(reservation.getReservationId());
        int newTotalReservations = reservationRepository.findAll().size();
        assertThat(newTotalReservations, equalTo(totalReservations - 1));
    }

    @Test
    @DisplayName("La suppression de ne fonctionne pas car la table n'existe pas")
    void verifyDeleteFailBecauseReservationDoesNotExist() {
        Reservation reservation = new Reservation(false, table1);
        int totalReservations = userRepository.findAll().size();
        reservationRepository.delete(reservation.getReservationId());
        int newTotalReservations = userRepository.findAll().size();
        assertThat(newTotalReservations, equalTo(totalReservations));
    }

    @AfterEach
    void tearDownAfterEach() {
        reservationRepository.delete(reservation1.getReservationId());
        tableRepository.delete(table1.getTableId());
        userRepository.delete(user1.getUserId());
    }
}
