package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Meal;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.repository.MealRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;

import java.sql.Timestamp;
import java.time.Instant;

public class MaitreHotelController {

    private final TableRepositoryImpl tableRepository;
    private final MealRepositoryImpl mealRepository;

    public MaitreHotelController(){
        tableRepository = TableRepositoryImpl.getInstance();
        mealRepository = MealRepositoryImpl.getInstance();
    }

    public Meal installClient(Table table, int nbCustomers){
        Meal meal = null;
        if(table.getState().equals(TableStates.LIBRE) && table.getPlaces() >= nbCustomers){
            meal = mealRepository.save(new Meal(nbCustomers, Timestamp.from(Instant.now()), table));
            table.setState(TableStates.OCCUPEE);
            tableRepository.update(table);
        }
        return meal;
    }
}
