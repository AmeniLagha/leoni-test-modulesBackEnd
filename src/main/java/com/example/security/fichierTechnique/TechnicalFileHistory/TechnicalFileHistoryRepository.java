package com.example.security.fichierTechnique.TechnicalFileHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicalFileHistoryRepository extends JpaRepository<TechnicalFileHistory, Long> {

    List<TechnicalFileHistory> findByTechnicalFileId(Long technicalFileId);
    List<TechnicalFileHistory> findByTechnicalFileItemId(Long technicalFileItemId);
}
