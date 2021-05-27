package fr.ul.miage.gl_restaurant.utilities;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.controller.*;
import fr.ul.miage.gl_restaurant.model.Reservation;
import fr.ul.miage.gl_restaurant.repository.ReservationRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;

import java.time.LocalDateTime;
import java.util.List;

public class ControllerUtils {

    private ControllerUtils() {}

    public static UserController getController(Authentification auth) {
        switch (auth.getUser().getRole()) {
            case DIRECTEUR:
                return new DirecteurController(auth);
            case MAITRE_HOTEL:
                return new MaitreHotelController(auth);
            case SERVEUR:
                return new ServeurController(auth);
            case ASSISTANT_SERVICE:
                return new AssistantServiceController(auth);
            default:
                return new CuisinierController(auth);
        }
    }

    /**
     * Vérifie si une table est réservée aujourd'hui et si oui, passe l'état de la table à RESERVEE.
     */
    public static void verifyReservationsForToday() {
        var today = LocalDateTime.now();
        boolean isLunch = DateUtils.isDateLunch(today);
        List<Reservation> reservations = ReservationRepositoryImpl.getInstance().findByReservationDateEquals(today.toLocalDate());
        reservations.forEach(r -> {
            var table = r.getTable();
            if (!table.getState().equals(TableStates.RESERVEE) && r.isLunch() == isLunch) {
                table.setState(TableStates.RESERVEE);
                TableRepositoryImpl.getInstance().update(table);
            }
        });
    }
}
