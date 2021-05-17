package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.model.User;
import fr.ul.miage.gl_restaurant.repository.OrderRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.RawMaterialRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;

import java.util.HashSet;
import java.util.Set;

public class ServeurController extends UserController {

    private final OrderRepositoryImpl orderRepository = OrderRepositoryImpl.getInstance();
    private final TableRepositoryImpl tableRepository = TableRepositoryImpl.getInstance();
    private final RawMaterialRepositoryImpl rawMaterialRepository = RawMaterialRepositoryImpl.getInstance();

    public ServeurController(Authentification auth) {
        super(auth);
    }

    /**
     * Prend une commande et la sauvegarde en base de données.
     * @param order Commande à prendre.
     */
    public void takeOrder(Order order) {
        rawMaterialRepository.updateStockBasedOnTakenOrder(order);
        orderRepository.save(order);
    }

    /**
     * Récupère la liste des tables d'un serveur.
     * @param user Serveur.
     * @return La liste des tables du serveur.
     */
    public Set<Table> getTablesList(User user){
        Set<Table> tablesList = new HashSet<>();
        if (user.getRole().equals(Roles.SERVEUR)) {
            tablesList.addAll(tableRepository.findByUserId(user.getUserId()));
        }
        return tablesList;
    }

    /**
     * Indique que la commande a été servie par le serveur.
     * @param order Commande servie.
     */
    public void setOrderServed(Order order) {
        order.setServed(true);
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
