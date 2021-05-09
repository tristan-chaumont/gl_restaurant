package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.model.User;
import fr.ul.miage.gl_restaurant.repository.OrderRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.RawMaterialRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;

import java.util.HashSet;

public class ServeurController {

    private final CuisinierController cuisinierController;
    private final OrderRepositoryImpl orderRepository;
    private final TableRepositoryImpl tableRepository;
    private final RawMaterialRepositoryImpl rawMaterialRepository;

    public ServeurController(CuisinierController cuisinierController) {
        this.cuisinierController = cuisinierController;
        this.orderRepository = OrderRepositoryImpl.getInstance();
        this.tableRepository = TableRepositoryImpl.getInstance();
        rawMaterialRepository = RawMaterialRepositoryImpl.getInstance();
    }

    public void takeOrder(Order order) {
        rawMaterialRepository.updateStockBasedOnTakenOrder(order);
        orderRepository.save(order);
    }

    public HashSet<Table> getTablesList(User user){
        HashSet<Table> tablesList = new HashSet<>();
        if (user.getRole().equals(Roles.SERVEUR))
            tablesList.addAll(tableRepository.findByUserId(user.getUserId()));
        return tablesList;
    }
}
