package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;

public class AssistantServiceController {

    private TableController tableController;

    public AssistantServiceController(){
        tableController = new TableController();
    }

    public void servingTable(Table table){
        if(table.getState().equals(TableStates.SALE)){
            tableController.changeState(table, TableStates.LIBRE);
        }
    }

}
