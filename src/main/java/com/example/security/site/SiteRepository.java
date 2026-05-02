// SiteRepository.java
package com.example.security.site;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    Optional<Site> findByName(String name);
    List<Site> findAllByOrderByNameAsc();
    // Dans SiteRepository.java
    // Dans SiteRepository.java - AJOUTER CETTE MÉTHODE
    @Query("SELECT DISTINCT s FROM Site s LEFT JOIN FETCH s.projets WHERE s.id = :id")
    Optional<Site> findByIdWithProjets(@Param("id") Long id);
}