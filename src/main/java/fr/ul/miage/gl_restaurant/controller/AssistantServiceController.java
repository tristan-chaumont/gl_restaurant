package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Table;

public class AssistantServiceController extends UserController {

    public AssistantServiceController(Authentification auth) {
        super(auth);
    }

    public void serveTable(Table table) {
        if (table.getState().equals(TableStates.SALE)) {
            table.changeState(TableStates.LIBRE);
        }
    }

    @Override
    public String displayActions() {
        //TODO
        return "";
    }

    @Override
    public void callAction(int action) {
        switch (action) {
            case 0:
                auth.disconnect();
                break;
            default:
                break;
        }
    }
}
