package fr.ul.miage.gl_restaurant.utilities;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.controller.*;
import fr.ul.miage.gl_restaurant.model.Reservation;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.repository.ReservationRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TestControllerUtils {

    TableRepositoryImpl tableRepository = TableRepositoryImpl.getInstance();
    ReservationRepositoryImpl reservationRepository = ReservationRepositoryImpl.getInstance();

    @Test
    @DisplayName("getController() récupère le bon controller")
    void verifyGetControllerGetsRightController() {
        Authentification auth = new Authentification();
        auth.signIn("chaumontt");
        assertThat(ControllerUtils.getController(auth), instanceOf(ServeurController.class));
        auth.signIn("boncij");
        assertThat(ControllerUtils.getController(auth), instanceOf(MaitreHotelController.class));
        auth.signIn("corrigeuxl");
        assertThat(ControllerUtils.getController(auth), instanceOf(DirecteurController.class));
        auth.signIn("noirotq");
        assertThat(ControllerUtils.getController(auth), instanceOf(AssistantServiceController.class));
        auth.signIn("luct");
        assertThat(ControllerUtils.getController(auth), instanceOf(CuisinierController.class));
    }

    @Test
    @DisplayName("Une réservation pour aujourd'hui")
    void verifyReservationsForTodaySucceedBecauseThereIsAReservationTodayForNow() {
        Table table1 = tableRepository.save(new Table(1, TableStates.LIBRE, 4, null));
        Table table2 = tableRepository.save(new Table(2, TableStates.LIBRE, 5, null));
        LocalDateTime now = LocalDateTime.now();
        LocalDate dateNow = now.toLocalDate();
        boolean isLunch = DateUtils.isDateLunch(now);
        Reservation reservation1 = reservationRepository.save(new Reservation(isLunch, table1, dateNow));
        ControllerUtils.verifyReservationsForToday();
        Optional<Table> resTable1 = tableRepository.findById(table1.getTableId());
        Optional<Table> resTable2 = tableRepository.findById(table2.getTableId());

        assertThat(resTable1.isPresent(), is(true));
        assertThat(resTable2.isPresent(), is(true));
        assertThat(resTable1.get().getState(), is(TableStates.RESERVEE));
        assertThat(resTable2.get().getState(), is(TableStates.LIBRE));

        reservationRepository.delete(reservation1.getReservationId());
        tableRepository.delete(table1.getTableId());
        tableRepository.delete(table2.getTableId());
    }

    @Test
    @DisplayName("Il n'y a pas de table réservée aujourd'hui")
    void verifyReservationsForTodaySucceedAndThereIsNoReservation() {
        Table table1 = tableRepository.save(new Table(1, TableStates.LIBRE, 4, null));
        Table table2 = tableRepository.save(new Table(2, TableStates.LIBRE, 5, null));
        ControllerUtils.verifyReservationsForToday();
        Optional<Table> resTable1 = tableRepository.findById(table1.getTableId());
        Optional<Table> resTable2 = tableRepository.findById(table2.getTableId());

        assertThat(resTable1.isPresent(), is(true));
        assertThat(resTable2.isPresent(), is(true));
        assertThat(resTable1.get().getState(), is(TableStates.LIBRE));
        assertThat(resTable2.get().getState(), is(TableStates.LIBRE));

        tableRepository.delete(table1.getTableId());
        tableRepository.delete(table2.getTableId());
    }

    @Test
    @DisplayName("Il y a une table réservée aujourd'hui mais pas pour l'heure actuelle")
    void verifyReservationsForTodaySucceedBecauseThereIsAReservationTodayButNotForNow() {
        Table table1 = tableRepository.save(new Table(1, TableStates.LIBRE, 4, null));
        Table table2 = tableRepository.save(new Table(2, TableStates.LIBRE, 5, null));
        LocalDateTime now = LocalDateTime.now();
        LocalDate dateNow = now.toLocalDate();
        boolean isLunch = DateUtils.isDateLunch(now);
        Reservation reservation1 = reservationRepository.save(new Reservation(!isLunch, table1, dateNow));
        ControllerUtils.verifyReservationsForToday();
        Optional<Table> resTable1 = tableRepository.findById(table1.getTableId());
        Optional<Table> resTable2 = tableRepository.findById(table2.getTableId());

        assertThat(resTable1.isPresent(), is(true));
        assertThat(resTable2.isPresent(), is(true));
        assertThat(resTable1.get().getState(), is(TableStates.LIBRE));
        assertThat(resTable2.get().getState(), is(TableStates.LIBRE));

        reservationRepository.delete(reservation1.getReservationId());
        tableRepository.delete(table1.getTableId());
        tableRepository.delete(table2.getTableId());
    }
}
