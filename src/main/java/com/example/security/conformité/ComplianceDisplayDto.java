package com.example.security.conformité;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceDisplayDto {
    private Long id;

    // Cahier de charge
    private Long chargeSheetId;
    private String orderNumber;      // OrderNumber du cahier
    private Integer itemNumber;      // indexValue ou numéro du module
    private String orderitemNumber;
    // Général
    private LocalDate testDateTime;
    private String technicianName;
    private String rfidNumber;

    // Identity Code
    private String leoniPartNumber;
    private String producer;
    private String type;

    // Inspection
    private String sequenceTestPins;
    private String codingRequest;
    private String secondaryLocking;
    private Double offsetTestMm;
    private Double stableOffsetTestMm;
    private Double displacementPathPushBackMm;
    private String housingAttachments;
    private Double maxLeakTestMbar;
    private Double adjustmentLeakTestMbar;
    private String colourVerification;
    private String terminalAlignment;
    private String openShuntsAirbag;
    private String spacerClosingUnit;
    private String specialFunctions;
    private Double contactProblemsPercentage;

    // Qualification Result
    private Boolean qualifiedTestModule;
    private Boolean conditionallyQualifiedTestModule;
    private Boolean notQualifiedTestModule;

    // Remarks
    private String remarks;

    // Status & audit
    private String createdBy;
    private LocalDate createdAt;
    private String updatedBy;
    private LocalDate updatedAt;
}
