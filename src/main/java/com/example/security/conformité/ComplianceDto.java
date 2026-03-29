package com.example.security.conformité;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

public class ComplianceDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDto {
        private Long chargeSheetId;
        private Long itemId;

        // General
        private String orderNumber;
        private String orderitemNumber;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate testDateTime;
        private String technicianName;
        private String rfidNumber;

        // Identity Code
        private String leoniPartNumber;
        private Integer indexValue;
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
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDto {
        private String orderitemNumber;
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

        private Boolean qualifiedTestModule;
        private Boolean conditionallyQualifiedTestModule;
        private Boolean notQualifiedTestModule;

        private String remarks;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseDto {
        private Long id;
        private Long chargeSheetId;

        // General
        private String orderNumber;
        private String orderitemNumber;
        private LocalDate testDateTime;
        private String technicianName;
        private String rfidNumber;

        // Identity Code
        private String leoniPartNumber;
        private Integer indexValue;
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

        // Status
        private String createdBy;
        private LocalDate createdAt;
        private String updatedBy;
        private LocalDate updatedAt;
    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrepareComplianceDto {
        private Long itemId;
        private String itemNumber;
        private int quantityOrdered;
        private int quantityReceived;
        private int quantityToCreate;
    }
}