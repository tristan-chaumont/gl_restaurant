package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Table;

public class AssistantServiceController extends UserController {

    private final TableController tableController;

    public AssistantServiceController(Authentification auth) {
        super(auth);
        this.tableController = new TableController();
    }

    public void serveTable(Table table) {
        if (table.getState().equals(TableStates.SALE)) {
            tableController.changeState(table, TableStates.LIBRE);
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
