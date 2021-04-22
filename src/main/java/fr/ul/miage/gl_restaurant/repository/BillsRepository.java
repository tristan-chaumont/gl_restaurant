package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.model.Bills;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillsRepository extends JpaRepository<Bills, Long> {
}
