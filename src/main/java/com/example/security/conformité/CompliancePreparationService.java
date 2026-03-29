package com.example.security.conformité;

import com.example.security.cahierdeCharge.*;
import com.example.security.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompliancePreparationService {

    private final ChargeSheetItemRepository itemRepository;
    private final ReceptionHistoryRepository receptionHistoryRepository;
    private final ComplianceRepository complianceRepository;

    /**
     * Prépare les données pour la création de fiches de conformité
     * Basé sur les quantités REÇUES, pas sur les quantités commandées
     */
    public List<ComplianceDto.PrepareComplianceDto> prepareComplianceForItem(Long itemId) {
        ChargeSheetItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // Récupérer l'historique des réceptions pour cet item
        List<ReceptionHistory> receptions = receptionHistoryRepository.findByItemId(itemId);

        // Calculer le total reçu
        int totalReceived = receptions.stream()
                .mapToInt(ReceptionHistory::getQuantityReceived)
                .sum();

        // Compter combien de fiches de conformité existent déjà pour cet item
        List<Compliance> existingCompliances = complianceRepository.findByItemId(itemId);
        int existingCount = existingCompliances.size();

        // Le nombre à créer = total reçu - déjà créé
        int quantityToCreate = totalReceived - existingCount;

        List<ComplianceDto.PrepareComplianceDto> result = new ArrayList<>();

        if (quantityToCreate > 0) {
            result.add(ComplianceDto.PrepareComplianceDto.builder()
                    .itemId(item.getId())
                    .itemNumber(item.getItemNumber())
                    .quantityOrdered(item.getQuantityOfTestModules() != null ? item.getQuantityOfTestModules() : 0)
                    .quantityReceived(totalReceived)
                    .quantityToCreate(quantityToCreate)
                    .build());
        }

        return result;
    }

    /**
     * Crée les fiches de conformité pour les quantités reçues
     */
    @Transactional
    public List<Compliance> createComplianceForReceivedQuantity(Long itemId, int numberOfSheets) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheetItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // Récupérer les réceptions pour cet item - CORRECTION: utiliser findByItemId et trier manuellement
        List<ReceptionHistory> receptions = receptionHistoryRepository.findByItemId(itemId);

        // Trier manuellement par createdAt
        receptions.sort((a, b) -> {
            if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
            if (a.getCreatedAt() == null) return -1;
            if (b.getCreatedAt() == null) return 1;
            return a.getCreatedAt().compareTo(b.getCreatedAt());
        });

        // Récupérer les fiches existantes
        List<Compliance> existingCompliances = complianceRepository.findByItemId(itemId);
        int existingCount = existingCompliances.size();

        List<Compliance> createdCompliances = new ArrayList<>();

        // Extraire les informations de la référence client
        String supplierRef = item.getHousingReferenceSupplierCustomer() != null ?
                item.getHousingReferenceSupplierCustomer() : "";

        String[] refParts = supplierRef.split("_");
        String basePart = refParts.length > 0 ? refParts[0] : "";
        String indexValueStr = refParts.length > 1 ? refParts[1].replaceAll("[^0-9]", "") : "1";
        int indexValue = 1;
        try {
            indexValue = Integer.parseInt(indexValueStr);
        } catch (NumberFormatException e) {
            indexValue = 1;
        }
        String producer = refParts.length > 2 ? refParts[2] : "";
        String type = refParts.length > 3 ? refParts[3] : "";

        int createdCount = 0;

        // Parcourir les réceptions pour créer les fiches
        for (ReceptionHistory reception : receptions) {
            // Combien de fiches créer pour cette réception
            int startFrom = Math.max(0, existingCount - reception.getPreviousTotalReceived());
            int availableInThisReception = reception.getQuantityReceived() - Math.max(0, startFrom);
            int toCreate = Math.min(availableInThisReception, numberOfSheets - createdCount);

            for (int i = 0; i < toCreate && createdCount < numberOfSheets; i++) {
                int unitNumber = reception.getPreviousTotalReceived() + startFrom + i + 1;

                Compliance compliance = Compliance.builder()
                        .chargeSheetId(item.getChargeSheet().getId())
                        .item(item)
                        .orderNumber(item.getChargeSheet().getOrderNumber())
                        .orderitemNumber(item.getItemNumber() + "-" + String.format("%02d", unitNumber))
                        .leoniPartNumber(basePart)
                        .indexValue(indexValue)
                        .producer(producer)
                        .type(type)
                        .rfidNumber(item.getHousingReferenceLeoni())
                        .receptionHistoryId(reception.getId())
                        .deliveryNoteNumber(reception.getDeliveryNoteNumber())
                        .unitNumber(unitNumber)
                        .qualifiedTestModule(false)
                        .conditionallyQualifiedTestModule(false)
                        .notQualifiedTestModule(false)
                        .createdBy(currentUser.getEmail())
                        .createdAt(LocalDate.now())
                        .build();

                createdCompliances.add(complianceRepository.save(compliance));
                createdCount++;
            }
        }

        return createdCompliances;
    }
}