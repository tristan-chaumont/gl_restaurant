package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.repository.OrderRepositoryImpl;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class CuisinierController extends UserController {

    private final TreeSet<Order> ordersQueue;
    private final OrderRepositoryImpl orderRepository;

    public CuisinierController(Authentification auth) {
        super(auth);
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

    /**
     * Prépare la commande prise par un serveur.
     * Pas besoin de vérifier les stocks, car ils sont décrémentés lorsque le serveur prend la commande.
     * @param order Commande à préparer.
     */
    public void prepareOrder(Order order) {
        order.setPreparationDate(Timestamp.from(Instant.now()));
        orderRepository.update(order);
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
