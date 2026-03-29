package com.example.security.stock;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockModuleRepository extends JpaRepository<StockModule, Long> {
    // Tu peux ajouter des méthodes custom si besoin (par ex: findByStatus)
    Optional<StockModule> findByTechnicalFileItemId(Long technicalFileItemId);
    @Modifying
    @Transactional
    @Query("DELETE FROM StockModule s WHERE s.technicalFileItem.id = :itemId")
    void deleteByTechnicalFileItemId(@Param("itemId") Long itemId);
}