package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.repository.OrderRepositoryImpl;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class CuisinierController {

    private final TreeSet<Order> ordersQueue;
    private final OrderRepositoryImpl orderRepository;

    public CuisinierController() {
        this.ordersQueue = new TreeSet<>();
        this.orderRepository = OrderRepositoryImpl.getInstance();
    }

    /**
     * Récupère toutes les commandes prises par les serveurs et qui n'ont pas encore été préparées.
     * On fait une requête DB à chaque fois pour actualiser les commandes.
     * @return
     *      Les commandes passées par les clients.
     */
    public SortedSet<Order> getOrdersQueue() {
        List<Order> orders = orderRepository.findCurrentOrders();
        this.ordersQueue.addAll(orders);
        return ordersQueue;
    }
}
