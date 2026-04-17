package com.example.security.reclamation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

public class ClaimDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDto {
        private Long chargeSheetId;
        private String relatedTo;
        private Long relatedId;

        // NOUVEAUX CHAMPS
        private String plant;
        private String customer;
        private String contactPerson;
        private String customerEmail;
        private String customerPhone;
        private String supplier;
        private String supplierContactPerson;
        private String orderNumber;
        private String testModuleNumber;
        private Integer testModuleQuantity;
        private String ppoSignature;
        private String problemWhatHappened;
        private String problemWhy;
        private String problemWhenDetected;
        private String problemWhoDetected;
        private String problemWhereDetected;
        private String problemHowDetected;
        private LocalDate claimDate;

        // Champs existants
        private String title;
        private String description;
        private Claim.Priority priority;
        private String category;
        private String imagePath;
        private String assignedTo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDto {
        private String title;
        private String description;
        private Claim.Priority priority;
        private String category;
        private String imagePath;
        private Claim.ClaimStatus status;
        private String assignedTo;
        private String actionTaken;
        private String resolution;
        private LocalDate estimatedResolutionDate;
        private LocalDate actualResolutionDate;

        // NOUVEAUX CHAMPS pour mise à jour
        private String problemWhatHappened;
        private String problemWhy;
        private String problemWhenDetected;
        private String problemWhoDetected;
        private String problemWhereDetected;
        private String problemHowDetected;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentDto {
        private String assignedTo;
        private LocalDate estimatedResolutionDate;
        private String comment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResolutionDto {
        private String actionTaken;
        private String resolution;
        private LocalDate actualResolutionDate;
        private Claim.ClaimStatus status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseDto {
        private Long id;
        private Long chargeSheetId;
        private String relatedTo;
        private Long relatedId;

        // NOUVEAUX CHAMPS
        private String plant;
        private String customer;
        private String contactPerson;
        private String customerEmail;
        private String customerPhone;
        private String supplier;
        private String supplierContactPerson;
        private String orderNumber;
        private String testModuleNumber;
        private Integer testModuleQuantity;
        private String ppoSignature;
        private String problemWhatHappened;
        private String problemWhy;
        private String problemWhenDetected;
        private String problemWhoDetected;
        private String problemWhereDetected;
        private String problemHowDetected;
        private LocalDate claimDate;

        // Champs existants
        private String imagePath;
        private String title;
        private String description;
        private Claim.Priority priority;
        private String category;
        private Claim.ClaimStatus status;
        private String reportedBy;
        private LocalDate reportedDate;
        private String assignedTo;
        private LocalDate assignedDate;
        private String actionTaken;
        private String resolution;
        private String resolvedBy;
        private LocalDate resolvedDate;
        private LocalDate estimatedResolutionDate;
        private LocalDate actualResolutionDate;
        private String closedBy;
        private LocalDate closedDate;
        private String createdBy;
        private LocalDate createdAt;
        private String updatedBy;
        private LocalDate updatedAt;
    }
}