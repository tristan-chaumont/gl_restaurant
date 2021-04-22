package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.model.Dishes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishesRepository extends JpaRepository<Dishes, Long> {
}
