package com.example.security.fichierTechnique;

import com.example.security.cahierdeCharge.ChargeSheetStatus;
import com.example.security.fichierTechnique.TechnicalFileHistory.TechnicalFileHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TechnicalFileRepository extends JpaRepository<TechnicalFile, Long> {

    @Query("""
SELECT DISTINCT tf
FROM TechnicalFile tf
JOIN tf.technicalFileItems tfi
JOIN tfi.chargeSheetItem csi
JOIN csi.chargeSheet cs
WHERE cs.project = :project
AND cs.status IN :statuses
""")
    List<TechnicalFile> findByProjectAndChargeSheetStatusIn(
            String project,
            List<ChargeSheetStatus> statuses
    );
    @Query("""
SELECT DISTINCT tf
FROM TechnicalFile tf
JOIN tf.technicalFileItems tfi
JOIN tfi.chargeSheetItem csi
JOIN csi.chargeSheet cs
WHERE cs.project = :project
""")
    List<TechnicalFile> findByProject(String project);
    // ✅ NOUVELLE MÉTHODE : Compter les dossiers techniques par projet et site
    @Query("SELECT COUNT(DISTINCT tf) FROM TechnicalFile tf " +
            "JOIN tf.technicalFileItems tfi " +
            "JOIN tfi.chargeSheetItem csi " +
            "JOIN csi.chargeSheet cs " +
            "WHERE cs.project = :project AND cs.plant = :siteName")
    long countByProjectAndSite(@Param("project") String project, @Param("siteName") String siteName);


}
