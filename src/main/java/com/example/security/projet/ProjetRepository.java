// ProjetRepository.java
package com.example.security.projet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, Long> {
    Optional<Projet> findByName(String name);
    List<Projet> findAllByActiveTrue();
    List<Projet> findAllByOrderByNameAsc();
}