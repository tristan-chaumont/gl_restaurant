package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.model.Meals;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealsRepository extends JpaRepository<Meals, Long> {
}
