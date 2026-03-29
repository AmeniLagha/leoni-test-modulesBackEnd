package com.example.security.cahierdeCharge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChargeSheetItemRepository extends JpaRepository<ChargeSheetItem, Long> {
    List<ChargeSheetItem> findByChargeSheetId(Long chargeSheetId);
}