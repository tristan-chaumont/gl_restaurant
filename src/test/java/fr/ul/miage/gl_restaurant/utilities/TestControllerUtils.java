package fr.ul.miage.gl_restaurant.utilities;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.controller.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

class TestControllerUtils {

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
}
