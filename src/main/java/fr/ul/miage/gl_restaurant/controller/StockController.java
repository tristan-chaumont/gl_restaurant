package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.model.Dish;
import fr.ul.miage.gl_restaurant.model.RawMaterial;
import fr.ul.miage.gl_restaurant.repository.DishRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.RawMaterialRepositoryImpl;

import java.util.List;
import java.util.Optional;

public class StockController {

    private final DishRepositoryImpl dishRepository = new DishRepositoryImpl(Environment.TEST);
    private final RawMaterialRepositoryImpl rawMaterialRepository = new RawMaterialRepositoryImpl(Environment.TEST);

    /**
     * Met à jour la carte du jour en fonction du stock de matières premières.
     * Cette méthode doit être exécutée avant qu'un serveur prenne une commande pour vérifier
     * que les plats commandés puissent bien être servis.
     */
    public void updateDailyMenuBasedOnRemainingStock() {
        List<Dish> dishes = dishRepository.findAll();
        dishes.forEach(dish -> {
            if (dish.isDailyMenu()) {
                dish.getRawMaterials().forEach((rm, quantity) -> {
                    // on récupère la matière première nécessaire au repas
                    Optional<RawMaterial> rawMaterialUsed = rawMaterialRepository.findById(rm.getRawMaterialId());
                    if (rawMaterialUsed.isPresent() && quantity > rm.getStockQuantity()) {
                        // si le stock n'est plus suffisant, on retire le repas de la carte du jour
                        dishRepository.updateDailyMenu(dish.getDishId(), false);
                    }
                });
            }
        });
    }
}
