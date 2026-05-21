package com.example.security.cahierdeCharge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 * Interface de repository pour la gestion des entités {@link ChargeSheet}.
 * <p>
 * Cette interface étend {@link JpaRepository} et fournit les opérations d'accès
 * aux données pour les cahiers des charges. Elle permet la recherche, le filtrage
 * par projet, site (plant), statut, ainsi que les calculs statistiques pour les
 * tableaux de bord.
 * </p>
 *
 * <p><strong>Fonctionnalités principales :</strong></p>
 * <ul>
 *     <li>Recherche de cahiers par projet, site et statut</li>
 *     <li>Comptages (total, par statut, par projet, par site)</li>
 *     <li>Statistiques mensuelles pour les tableaux de bord</li>
 *     <li>Filtrage multiple pour l'isolation multi-sites</li>
 * </ul>
 *
 * <p><strong>Isolation des données :</strong></p>
 * <ul>
 *     <li>Pour les utilisateurs non-ADMIN : filtrage par {@code project} ET {@code plant}</li>
 *     <li>Pour l'ADMIN : accès global à tous les cahiers</li>
 * </ul>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see ChargeSheet
 * @see ChargeSheetStatus
 * @since Sprint 4
 */
@Repository
public interface ChargeSheetRepository extends JpaRepository<ChargeSheet, Long> {

    // ============================================================
    // MÉTHODES EXISTANTES - FILTRAGE PAR PROJET UNIQUEMENT
    // ============================================================

    /**
     * Récupère tous les cahiers des charges d'un projet spécifique.
     *
     * @param project Le nom du projet (ex: "Mercedes", "BMW")
     * @return Liste des cahiers appartenant au projet
     */
    @Query("SELECT cs FROM ChargeSheet cs WHERE cs.project = :project")
    List<ChargeSheet> findByProject(String project);
    /**
     * Récupère tous les cahiers d'un projet avec un statut spécifique.
     *
     * @param project Le nom du projet
     * @param status Le statut du cahier
     * @return Liste des cahiers correspondant aux critères
     */
    List<ChargeSheet> findByProjectAndStatus(String project, ChargeSheetStatus status);
    /**
     * Récupère tous les cahiers d'un projet dont le statut est dans une liste donnée.
     *
     * @param project Le nom du projet
     * @param statuses Liste des statuts à inclure
     * @return Liste des cahiers correspondant aux critères
     */
    List<ChargeSheet> findByProjectAndStatusIn(String project, List<ChargeSheetStatus> statuses);
    /**
     * Compte le nombre total de cahiers pour un projet.
     *
     * @param project Le nom du projet
     * @return Nombre de cahiers
     */
    long countByProject(String project);
    /**
     * Compte le nombre de cahiers pour un projet avec un statut spécifique.
     *
     * @param project Le nom du projet
     * @param status Le statut à compter
     * @return Nombre de cahiers correspondant
     */
    long countByProjectAndStatus(String project, ChargeSheetStatus status);
    /**
     * Statistiques mensuelles de création pour un projet spécifique.
     * <p>
     * Retourne pour chaque mois le nombre de cahiers créés, triés du plus récent
     * au plus ancien.
     * </p>
     *
     * @param project Le nom du projet
     * @return Liste d'objets [mois (YYYY-MM), count] classés par mois DESC
     */
    @Query(value = "SELECT DATE_FORMAT(cs.created_at, '%Y-%m') as month, COUNT(cs.id) as count " +
            "FROM charge_sheet cs " +
            "WHERE cs.project = :project " +
            "GROUP BY DATE_FORMAT(cs.created_at, '%Y-%m') " +
            "ORDER BY DATE_FORMAT(cs.created_at, '%Y-%m') DESC",
            nativeQuery = true)
    List<Object[]> countByMonthForProject(@Param("project") String project);
    /**
     * Statistiques mensuelles de création pour tous les projets.
     * <p>
     * Retourne pour chaque mois le nombre total de cahiers créés.
     * </p>
     *
     * @return Liste d'objets [mois (YYYY-MM), count] classés par mois DESC
     */
    @Query(value = "SELECT DATE_FORMAT(cs.created_at, '%Y-%m') as month, COUNT(cs.id) as count " +
            "FROM charge_sheet cs " +
            "GROUP BY DATE_FORMAT(cs.created_at, '%Y-%m') " +
            "ORDER BY DATE_FORMAT(cs.created_at, '%Y-%m') DESC",
            nativeQuery = true)
    List<Object[]> countByMonthForAllProjects();

    // ============================================================
    // MÉTHODES AVEC FILTRAGE PAR PROJET ET SITE (PLANT)
    // ============================================================

    /**
     * Récupère tous les cahiers d'un projet ET d'un site spécifiques.
     * <p>
     * Utilisé pour l'isolation multi-sites : un utilisateur non-ADMIN
     * ne voit que les cahiers de son site.
     * </p>
     *
     * @param project Le nom du projet
     * @param plant Le nom du site de production (ex: "Manzel Hayet", "LTN1")
     * @return Liste des cahiers correspondant aux critères
     */
    @Query("SELECT cs FROM ChargeSheet cs WHERE cs.project = :project AND cs.plant = :plant")
    List<ChargeSheet> findByProjectAndPlant(@Param("project") String project, @Param("plant") String plant);

    /**
     * Récupère tous les cahiers d'un projet, d'un site ET avec un statut spécifique.
     *
     * @param project Le nom du projet
     * @param plant Le nom du site
     * @param status Le statut du cahier
     * @return Liste des cahiers correspondant aux critères
     */
    @Query("SELECT cs FROM ChargeSheet cs WHERE cs.project = :project AND cs.plant = :plant AND cs.status = :status")
    List<ChargeSheet> findByProjectAndPlantAndStatus(@Param("project") String project,
                                                     @Param("plant") String plant,
                                                     @Param("status") ChargeSheetStatus status);

    /**
     * Compte le nombre total de cahiers pour un projet ET un site.
     *
     * @param project Le nom du projet
     * @param plant Le nom du site
     * @return Nombre de cahiers
     */
    @Query("SELECT COUNT(cs) FROM ChargeSheet cs WHERE cs.project = :project AND cs.plant = :plant")
    long countByProjectAndPlant(@Param("project") String project, @Param("plant") String plant);

    /**
     * Compte le nombre de cahiers pour un projet, un site ET un statut spécifique.
     *
     * @param project Le nom du projet
     * @param plant Le nom du site
     * @param status Le statut à compter
     * @return Nombre de cahiers correspondant
     */
    @Query("SELECT COUNT(cs) FROM ChargeSheet cs WHERE cs.project = :project AND cs.plant = :plant AND cs.status = :status")
    long countByProjectAndPlantAndStatus(@Param("project") String project,
                                         @Param("plant") String plant,
                                         @Param("status") ChargeSheetStatus status);


    // ============================================================
    // MÉTHODES POUR ADMIN (ACCÈS GLOBAL)
    // ============================================================

    /**
     * Compte le nombre total de cahiers ayant un statut spécifique (global).
     * <p>
     * Utilisé par l'ADMIN pour les statistiques globales du dashboard.
     * </p>
     *
     * @param status Le statut à compter
     * @return Nombre total de cahiers avec ce statut
     */
    @Query("SELECT COUNT(cs) FROM ChargeSheet cs WHERE cs.status = :status")
    long countByStatus(@Param("status") ChargeSheetStatus status);
    // ============================================================
    // MÉTHODES DE FILTRAGE MULTIPLE (PROJETS + SITE)
    // ============================================================

    /**
     * Récupère tous les cahiers dont le projet est dans une liste donnée
     * et dont le site correspond.
     * <p>
     * Utilisé pour les utilisateurs ayant plusieurs projets associés.
     * </p>
     *
     * @param projects Liste des noms de projets
     * @param plant Le nom du site
     * @return Liste des cahiers correspondant aux critères
     */
    List<ChargeSheet> findByProjectInAndPlant(List<String> projects, String plant);
    /**
     * Récupère tous les cahiers dont le projet est dans une liste donnée,
     * le site correspond, et le statut est dans une liste donnée.
     *
     * @param projects Liste des noms de projets
     * @param plant Le nom du site
     * @param statuses Liste des statuts à inclure
     * @return Liste des cahiers correspondant aux critères
     */
    List<ChargeSheet> findByProjectInAndPlantAndStatusIn(List<String> projects, String plant, List<ChargeSheetStatus> statuses);
    /**
     * Récupère tous les cahiers dont le projet est dans une liste donnée,
     * le site correspond, et le statut est spécifique.
     *
     * @param projects Liste des noms de projets
     * @param plant Le nom du site
     * @param status Le statut du cahier
     * @return Liste des cahiers correspondant aux critères
     */
    List<ChargeSheet> findByProjectInAndPlantAndStatus(List<String> projects, String plant, ChargeSheetStatus status);
}