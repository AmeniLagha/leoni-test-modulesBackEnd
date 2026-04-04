package com.example.security.reclamation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByChargeSheetId(Long chargeSheetId);

    List<Claim> findByReportedBy(String reportedBy);

    List<Claim> findByAssignedTo(String assignedTo);

    List<Claim> findByStatus(Claim.ClaimStatus status);

    List<Claim> findByPriority(Claim.Priority priority);

    List<Claim> findByCategory(String category);

    List<Claim> findByRelatedToAndRelatedId(String relatedTo, Long relatedId);
    // Compter les réclamations par mois pour tous les projets (Admin)
    @Query(value = "SELECT DATE_FORMAT(c.created_at, '%Y-%m') as month, COUNT(c.id) as count " +
            "FROM claim c " +
            "GROUP BY DATE_FORMAT(c.created_at, '%Y-%m') " +
            "ORDER BY DATE_FORMAT(c.created_at, '%Y-%m') DESC",
            nativeQuery = true)
    List<Object[]> countByMonthForAllProjects();
}