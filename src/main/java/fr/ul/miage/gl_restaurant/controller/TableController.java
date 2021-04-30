package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.model.User;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.UserRepositoryImpl;

import java.util.Optional;

public class TableController {

    private final TableRepositoryImpl tableRepository;
    private final UserRepositoryImpl userRepository;

    public TableController(TableRepositoryImpl tableRepository, UserRepositoryImpl userRepository) {
        this.tableRepository = tableRepository;
        this.userRepository = userRepository;
    }

    public void assignServer(Table table, User user) {
        Optional<User> userTemp = userRepository.findById(user.getUserId());
        if(userTemp.isPresent()) {
            if (user.getRole().equals(Roles.SERVEUR)) {
                table.setUser(user);
                tableRepository.update(table);
            }
        }
    }

    public void changeState(Table table, TableStates tableStates) {
        table.setState(tableStates);
        tableRepository.update(table);
    }
}
