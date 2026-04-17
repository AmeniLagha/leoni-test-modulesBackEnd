// StockModuleDto.java
package com.example.security.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockModuleDto {

    // Champs existants
    private Long id;
    private Long technicalFileId;
    private Long technicalFileItemId;
    private Long chargeSheetItemId;
    private Long siteId;
    private String itemNumber;
    private String position;
    private Double finalDisplacement;
    private Double finalProgrammedSealing;
    private String finalDetection;
    private String movedBy;
    private LocalDate movedAt;
    private StockModule.StockStatus status;
    private String siteName;
    // Nouveaux champs
    private String casier;
    private String stuffNumr;
    private String leoniNumr;
    private String indexValue;
    private Integer quantite;
    private String fournisseur;
    private String etat;
    private String caisse;
    private String specifications;
    private LocalDate dernierMaj;
    private String infoSurModules;
    private String demandeurExplication;
    private LocalDate dateDemande;  // ✅ Changé de dateCreation à dateDemande
    private Integer newQuantite;
}