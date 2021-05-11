package fr.ul.miage.gl_restaurant.utilities;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.controller.*;

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
            case CUISINIER:
                return new CuisinierController(auth);
            default:
                throw new IllegalArgumentException();
        }
    }
}
