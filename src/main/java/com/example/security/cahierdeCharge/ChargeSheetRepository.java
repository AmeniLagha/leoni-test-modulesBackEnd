package com.example.security.cahierdeCharge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargeSheetRepository extends JpaRepository<ChargeSheet, Long> {

    // ============ MÉTHODES EXISTANTES ============

    @Query("SELECT cs FROM ChargeSheet cs WHERE cs.project = :project")
    List<ChargeSheet> findByProject(String project);

    List<ChargeSheet> findByProjectAndStatus(String project, ChargeSheetStatus status);

    List<ChargeSheet> findByProjectAndStatusIn(String project, List<ChargeSheetStatus> statuses);

    long countByProject(String project);

    long countByProjectAndStatus(String project, ChargeSheetStatus status);

    @Query(value = "SELECT DATE_FORMAT(cs.created_at, '%Y-%m') as month, COUNT(cs.id) as count " +
            "FROM charge_sheet cs " +
            "WHERE cs.project = :project " +
            "GROUP BY DATE_FORMAT(cs.created_at, '%Y-%m') " +
            "ORDER BY DATE_FORMAT(cs.created_at, '%Y-%m') DESC",
            nativeQuery = true)
    List<Object[]> countByMonthForProject(@Param("project") String project);

    @Query(value = "SELECT DATE_FORMAT(cs.created_at, '%Y-%m') as month, COUNT(cs.id) as count " +
            "FROM charge_sheet cs " +
            "GROUP BY DATE_FORMAT(cs.created_at, '%Y-%m') " +
            "ORDER BY DATE_FORMAT(cs.created_at, '%Y-%m') DESC",
            nativeQuery = true)
    List<Object[]> countByMonthForAllProjects();

    // ============ NOUVELLES MÉTHODES AVEC FILTRAGE PAR PLANT (SITE) ============

    // ✅ Filtrer par projet ET plant
    @Query("SELECT cs FROM ChargeSheet cs WHERE cs.project = :project AND cs.plant = :plant")
    List<ChargeSheet> findByProjectAndPlant(@Param("project") String project, @Param("plant") String plant);

    // ✅ Filtrer par projet, plant ET statut
    @Query("SELECT cs FROM ChargeSheet cs WHERE cs.project = :project AND cs.plant = :plant AND cs.status = :status")
    List<ChargeSheet> findByProjectAndPlantAndStatus(@Param("project") String project,
                                                     @Param("plant") String plant,
                                                     @Param("status") ChargeSheetStatus status);

    // ✅ Filtrer par projet, plant ET liste de statuts
    @Query("SELECT cs FROM ChargeSheet cs WHERE cs.project = :project AND cs.plant = :plant AND cs.status IN :statuses")
    List<ChargeSheet> findByProjectAndPlantAndStatusIn(@Param("project") String project,
                                                       @Param("plant") String plant,
                                                       @Param("statuses") List<ChargeSheetStatus> statuses);

    // ✅ Compter par projet ET plant
    @Query("SELECT COUNT(cs) FROM ChargeSheet cs WHERE cs.project = :project AND cs.plant = :plant")
    long countByProjectAndPlant(@Param("project") String project, @Param("plant") String plant);

    // ✅ Compter par projet, plant ET statut
    @Query("SELECT COUNT(cs) FROM ChargeSheet cs WHERE cs.project = :project AND cs.plant = :plant AND cs.status = :status")
    long countByProjectAndPlantAndStatus(@Param("project") String project,
                                         @Param("plant") String plant,
                                         @Param("status") ChargeSheetStatus status);

    // ✅ Statistiques mensuelles par projet ET plant
    @Query(value = "SELECT DATE_FORMAT(cs.created_at, '%Y-%m') as month, COUNT(cs.id) as count " +
            "FROM charge_sheet cs " +
            "WHERE cs.project = :project AND cs.plant = :plant " +
            "GROUP BY DATE_FORMAT(cs.created_at, '%Y-%m') " +
            "ORDER BY DATE_FORMAT(cs.created_at, '%Y-%m') DESC",
            nativeQuery = true)
    List<Object[]> countByMonthForProjectAndPlant(@Param("project") String project, @Param("plant") String plant);

    // ✅ Pour ADMIN : voir tous les cahiers (déjà existant avec findAll())
    // findAll() fonctionne déjà

    // ✅ Pour ADMIN : compter par statut global
    @Query("SELECT COUNT(cs) FROM ChargeSheet cs WHERE cs.status = :status")
    long countByStatus(@Param("status") ChargeSheetStatus status);

    List<ChargeSheet> findByProjectInAndPlant(List<String> projects, String plant);

    List<ChargeSheet> findByProjectInAndPlantAndStatusIn(List<String> projects, String plant, List<ChargeSheetStatus> statuses);

    List<ChargeSheet> findByProjectInAndPlantAndStatus(List<String> projects, String plant, ChargeSheetStatus status);
}