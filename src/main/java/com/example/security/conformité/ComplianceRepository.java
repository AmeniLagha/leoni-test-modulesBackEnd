package com.example.security.conformité;

import com.example.security.cahierdeCharge.ChargeSheetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplianceRepository extends JpaRepository<Compliance, Long> {

    List<Compliance> findByChargeSheetId(Long chargeSheetId);


    @Query("""
SELECT c
FROM Compliance c
JOIN c.item i
JOIN i.chargeSheet cs
WHERE cs.project = :project
AND cs.status = :status
""")
    List<Compliance> findByProjectAndChargeSheetStatus(
            String project,
            ChargeSheetStatus status
    );
        @Query("""
    SELECT c
    FROM Compliance c
    JOIN c.item i
    JOIN i.chargeSheet cs
    WHERE cs.project = :project
    """)
        List<Compliance> findByProject(String project);
    // Ajoutez ces méthodes
    List<Compliance> findByItemId(Long itemId);

    List<Compliance> findByReceptionHistoryId(Long receptionHistoryId);

    @Query("SELECT COUNT(c) FROM Compliance c WHERE c.item.id = :itemId")
    int countByItemId(@Param("itemId") Long itemId);

        @Query("""
    SELECT c
    FROM Compliance c
    JOIN c.item i
    JOIN i.chargeSheet cs
    WHERE cs.project = :project
    AND cs.status IN :statuses
    """)
        List<Compliance> findByProjectAndChargeSheetStatusIn(
                String project,
                List<ChargeSheetStatus> statuses
        );


    // 📊 NOUVELLES MÉTHODES POUR LES STATISTIQUES DE CONFORMITÉ

    // Compter les conformités par mois pour un projet spécifique
    @Query(value = "SELECT DATE_FORMAT(c.created_at, '%Y-%m') as month, COUNT(c.id) as count " +
            "FROM compliance c " +
            "JOIN charge_sheet_item i ON c.item_id = i.id " +
            "JOIN charge_sheet cs ON i.charge_sheet_id = cs.id " +
            "WHERE cs.project = :project " +
            "GROUP BY DATE_FORMAT(c.created_at, '%Y-%m') " +
            "ORDER BY DATE_FORMAT(c.created_at, '%Y-%m') DESC",
            nativeQuery = true)
    List<Object[]> countByMonthForProject(@Param("project") String project);

    // Compter les conformités par mois pour tous les projets (Admin)
    @Query(value = "SELECT DATE_FORMAT(c.created_at, '%Y-%m') as month, COUNT(c.id) as count " +
            "FROM compliance c " +
            "GROUP BY DATE_FORMAT(c.created_at, '%Y-%m') " +
            "ORDER BY DATE_FORMAT(c.created_at, '%Y-%m') DESC",
            nativeQuery = true)
    List<Object[]> countByMonthForAllProjects();
    }