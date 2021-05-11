package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Meal;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.repository.MealRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;
import org.apache.commons.text.TextStringBuilder;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;

public class MaitreHotelController extends UserController {

    private final TableRepositoryImpl tableRepository;
    private final MealRepositoryImpl mealRepository;

    /**
     * ACTIONS DE L'UTILISATEUR
     */
    private static final String ACTION_1 = "1 : Prendre une réservation";
    private static final String ACTION_2 = "2 : Affecter un client à une table";
    private static final String ACTION_3 = "3 : Affecter un serveur à une table";

    public MaitreHotelController(Authentification auth) {
        super(auth);
        tableRepository = TableRepositoryImpl.getInstance();
        mealRepository = MealRepositoryImpl.getInstance();
        this.actions.addAll(Arrays.asList(ACTION_1, ACTION_2, ACTION_3));
    }

    public Meal seatClient(Table table, int nbCustomers){
        Meal meal = null;
        if(table.getState().equals(TableStates.LIBRE) && table.getPlaces() >= nbCustomers){
            meal = mealRepository.save(new Meal(nbCustomers, Timestamp.from(Instant.now()), table));
            table.setState(TableStates.OCCUPEE);
            tableRepository.update(table);
        }
        return meal;
    }

    @Override
    public String displayActions() {
        var stringBuilder = new TextStringBuilder();
        stringBuilder.appendln(super.displayActions());
        for (String action : actions) {
            stringBuilder.appendln(action);
        }
        return stringBuilder.toString();
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
