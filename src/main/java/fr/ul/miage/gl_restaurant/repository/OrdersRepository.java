package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
}
