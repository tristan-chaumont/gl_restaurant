package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.model.DishesOrders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishesOrdersRepository extends JpaRepository<DishesOrders, Long> {
}
