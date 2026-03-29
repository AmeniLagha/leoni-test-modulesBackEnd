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

        private String relatedTo; // "CHARGE_SHEET", "COMPLIANCE", "TECHNICAL_FILE"
        private Long relatedId;   // ID de la section concernée

        private String title;
        private String description;
        private Claim.Priority priority;
        private String category; // "TECHNICAL", "QUALITY", "LOGISTIC", "OTHER"
        private String imagePath;
        private String assignedTo; // Optionnel: assigner directement
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
        private Claim.ClaimStatus status; // Doit être RESOLVED ou CLOSED
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