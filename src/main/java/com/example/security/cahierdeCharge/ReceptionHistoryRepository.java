package com.example.security.cahierdeCharge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}