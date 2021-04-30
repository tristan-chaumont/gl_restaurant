package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Table;

public class AssistantServiceController {

    private final TableController tableController;

    public AssistantServiceController(TableController tableController) {
        this.tableController = tableController;
    }

    public void serveTable(Table table) {
        if (table.getState().equals(TableStates.SALE)) {
            tableController.changeState(table, TableStates.LIBRE);
        }
    }
}
