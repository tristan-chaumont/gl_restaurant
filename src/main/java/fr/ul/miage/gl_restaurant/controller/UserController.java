package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class UserController {

    protected Authentification auth;

    protected final Set<String> actions;

    private static final String LOGOUT_STRING = "0 : Se déconnecter";

    protected UserController(Authentification auth) {
        this.auth = auth;
        actions = new LinkedHashSet<>();
    }

    public String displayActions() {
        return LOGOUT_STRING;
    }

    public abstract void callAction(int action);

    public Set<String> getActions() {
        return actions;
    }
}
