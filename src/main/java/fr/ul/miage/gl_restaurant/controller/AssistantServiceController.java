package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;
import fr.ul.miage.gl_restaurant.utilities.PrintUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class AssistantServiceController extends UserController {

    private final TableRepositoryImpl tableRepository = TableRepositoryImpl.getInstance();

    /**
     * ACTIONS DE L'UTILISATEUR
     */
    private static final String ACTION_1 = "1 : Afficher les tables sales";
    private static final String ACTION_2 = "2 : Desservir et dresser une table";

    public AssistantServiceController(Authentification auth) {
        super(auth);
        this.actions.addAll(Arrays.asList(ACTION_1, ACTION_2));
    }

    protected List<Table> getDirtyTables(List<Table> tables) {
        return tables.stream().filter(t -> t.getState().equals(TableStates.SALE)).collect(Collectors.toList());
    }

    /**
     * Dresse la table pour le prochain client.
     * @param table Table à dresser.
     */
    protected boolean layTable(Table table) {
        if (table.getState().equals(TableStates.SALE)) {
            table.changeState(TableStates.LIBRE);
            return true;
        }
        return false;
    }

    protected void layTable() {
        List<Table> dirtyTables = getDirtyTables(tableRepository.findAll());
        PrintUtils.print("%s%n", StringUtils.center("Liste des tables sales :", 50));
        PrintUtils.print("%s%n", displayTablesByFloor(dirtyTables));
        PrintUtils.print("Veuillez saisir le numéro de la table : ");
        var tableId = askTableId(dirtyTables);
        Optional<Table> table = tableRepository.findById(tableId);
        if (table.isPresent()) {
            if (layTable(table.get())) {
                PrintUtils.print("La table a bien été nettoyée et dressée.%n%n");
            } else {
                PrintUtils.print("Impossible de s'occuper de la table sélectionnée, veuillez réessayer.%n%n");
            }
        }
    }

    @Override
    public void callAction(int action) {
        PrintUtils.print("%n");
        switch (action) {
            case 0:
                auth.disconnect();
                break;
            case 1:
                PrintUtils.print("%s%n", displayTablesByFloor(getDirtyTables(tableRepository.findAll())));
                break;
            case 2:
                layTable();
                break;
            default:
                break;
        }
    }
}
