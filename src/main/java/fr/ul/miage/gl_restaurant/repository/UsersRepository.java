package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsersRepository extends JpaRepository<Users, Long> {

    List<Users> findByRole(String role);
}
