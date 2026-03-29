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

    }