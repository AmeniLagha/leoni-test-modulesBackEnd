package com.example.security.cahierdeCharge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargeSheetRepository extends JpaRepository<ChargeSheet, Long> {
    @Query("SELECT cs FROM ChargeSheet cs WHERE cs.project = :project")
    List<ChargeSheet> findByProject(String project);
    List<ChargeSheet> findByProjectAndStatus(String project, ChargeSheetStatus status);
    List<ChargeSheet> findByProjectAndStatusIn(String project, List<ChargeSheetStatus> statuses);
    long countByProject(String project);
    long countByProjectAndStatus(String project, ChargeSheetStatus status);
    // Dans ChargeSheetRepository.java
    // ✅ Version corrigée pour MySQL
    @Query(value = "SELECT DATE_FORMAT(cs.created_at, '%Y-%m') as month, COUNT(cs.id) as count " +
            "FROM charge_sheet cs " +
            "WHERE cs.project = :project " +
            "GROUP BY DATE_FORMAT(cs.created_at, '%Y-%m') " +
            "ORDER BY DATE_FORMAT(cs.created_at, '%Y-%m') DESC",
            nativeQuery = true)
    List<Object[]> countByMonthForProject(@Param("project") String project);
    // Dans ChargeSheetRepository.java - Ajoutez cette méthode

    // Pour compter tous les projets (admin)
    @Query(value = "SELECT DATE_FORMAT(cs.created_at, '%Y-%m') as month, COUNT(cs.id) as count " +
            "FROM charge_sheet cs " +
            "GROUP BY DATE_FORMAT(cs.created_at, '%Y-%m') " +
            "ORDER BY DATE_FORMAT(cs.created_at, '%Y-%m') DESC",
            nativeQuery = true)
    List<Object[]> countByMonthForAllProjects();
}