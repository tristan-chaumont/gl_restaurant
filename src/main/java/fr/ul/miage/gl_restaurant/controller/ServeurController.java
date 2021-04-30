package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.repository.OrderRepositoryImpl;

public class ServeurController {

    private final CuisinierController cuisinierController;
    private final OrderRepositoryImpl orderRepository = new OrderRepositoryImpl(Environment.TEST);

    public ServeurController(CuisinierController cuisinierController) {
        this.cuisinierController = cuisinierController;
    }

    public void takeOrder(Order order) {
        orderRepository.save(order);
    }
}