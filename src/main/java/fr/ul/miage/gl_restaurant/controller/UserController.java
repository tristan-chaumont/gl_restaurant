package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import org.apache.commons.text.TextStringBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class UserController {

    protected Authentification auth;

    protected final Set<String> actions;

    private static final String LOGOUT_STRING = "0 : Se déconnecter";

    protected UserController(Authentification auth) {
        this.auth = auth;
        actions = new LinkedHashSet<>();
        actions.add(LOGOUT_STRING);
    }

    public String displayActions() {
        var stringBuilder = new TextStringBuilder();
        for (String action : actions) {
            stringBuilder.appendln(action);
        }
        return stringBuilder.toString();
    }

    public abstract void callAction(int action);

    public Set<String> getActions() {
        return actions;
    }
}
