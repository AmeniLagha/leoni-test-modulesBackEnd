package com.example.security.cahierdeCharge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}