package com.example.security.reception;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReceptionHistoryRepository extends JpaRepository<ReceptionHistory, Long> {

    List<ReceptionHistory> findByChargeSheetId(Long chargeSheetId);

    List<ReceptionHistory> findByChargeSheetIdOrderByCreatedAtDesc(Long chargeSheetId);

    List<ReceptionHistory> findByItemId(Long itemId);

    @Query("SELECT r FROM ReceptionHistory r WHERE r.item.id = :itemId ORDER BY r.createdAt ASC")
    List<ReceptionHistory> findByItemIdOrderByCreatedAtAsc(@Param("itemId") Long itemId);

    @Query("SELECT SUM(r.quantityReceived) FROM ReceptionHistory r WHERE r.item.id = :itemId")
    Integer sumQuantityReceivedByItemId(@Param("itemId") Long itemId);

    List<ReceptionHistory> findByItemIdOrderByReceptionDateDesc(Long itemId);
    // ✅ Statistiques par mois - CORRIGÉ
    @Query("""
        SELECT 
            FUNCTION('YEAR', r.receptionDate) as year,
            FUNCTION('MONTH', r.receptionDate) as month,
            SUM(r.quantityReceived) as totalReceived,
            COUNT(DISTINCT r.item.id) as numberOfItems,
            COUNT(DISTINCT r.chargeSheet.id) as numberOfSheets
        FROM ReceptionHistory r
        WHERE r.receptionDate BETWEEN :startDate AND :endDate
        GROUP BY FUNCTION('YEAR', r.receptionDate), FUNCTION('MONTH', r.receptionDate)
        ORDER BY FUNCTION('YEAR', r.receptionDate) DESC, FUNCTION('MONTH', r.receptionDate) DESC
    """)
    List<Object[]> getMonthlyReceptionStats(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    // ✅ Statistiques par mois avec filtre projet et site - CORRIGÉ
    @Query("""
        SELECT 
            FUNCTION('YEAR', r.receptionDate) as year,
            FUNCTION('MONTH', r.receptionDate) as month,
            SUM(r.quantityReceived) as totalReceived,
            COUNT(DISTINCT r.item.id) as numberOfItems,
            COUNT(DISTINCT r.chargeSheet.id) as numberOfSheets
        FROM ReceptionHistory r
        WHERE r.receptionDate BETWEEN :startDate AND :endDate
        AND (:project IS NULL OR r.chargeSheet.project = :project)
        AND (:site IS NULL OR r.chargeSheet.plant = :site)
        GROUP BY FUNCTION('YEAR', r.receptionDate), FUNCTION('MONTH', r.receptionDate)
        ORDER BY FUNCTION('YEAR', r.receptionDate) DESC, FUNCTION('MONTH', r.receptionDate) DESC
    """)
    List<Object[]> getMonthlyReceptionStatsFiltered(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate,
                                                    @Param("project") String project,
                                                    @Param("site") String site);

    // ✅ Quantité totale reçue par mois pour chaque item - CORRIGÉ
    @Query("""
        SELECT 
            FUNCTION('YEAR', r.receptionDate) as year,
            FUNCTION('MONTH', r.receptionDate) as month,
            r.item.id as itemId,
            r.item.itemNumber as itemNumber,
            SUM(r.quantityReceived) as quantityReceived
        FROM ReceptionHistory r
        WHERE r.receptionDate BETWEEN :startDate AND :endDate
        GROUP BY FUNCTION('YEAR', r.receptionDate), FUNCTION('MONTH', r.receptionDate), r.item.id, r.item.itemNumber
        ORDER BY FUNCTION('YEAR', r.receptionDate) DESC, FUNCTION('MONTH', r.receptionDate) DESC, quantityReceived DESC
    """)
    List<Object[]> getItemMonthlyReceptionStats(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    // ✅ Quantité totale reçue (globale)
    @Query("SELECT COALESCE(SUM(r.quantityReceived), 0) FROM ReceptionHistory r")
    Long getTotalQuantityReceived();

    // ✅ Quantité totale reçue par projet
    @Query("""
        SELECT r.chargeSheet.project, COALESCE(SUM(r.quantityReceived), 0)
        FROM ReceptionHistory r
        GROUP BY r.chargeSheet.project
    """)
    List<Object[]> getTotalQuantityByProject();

    // ✅ Quantité totale reçue par site
    @Query("""
        SELECT r.chargeSheet.plant, COALESCE(SUM(r.quantityReceived), 0)
        FROM ReceptionHistory r
        GROUP BY r.chargeSheet.plant
    """)
    List<Object[]> getTotalQuantityBySite();
}