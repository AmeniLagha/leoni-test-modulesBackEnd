package com.example.security.fichierTechnique;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class TechnicalFileDto {

    // DTO ITEM (contient toutes les données techniques)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnicalFileItemDto {

        private Long chargeSheetItemId;

        private LocalDate maintenanceDate;
        private String technicianName;

        @JsonProperty("xCode")
        private String xCode;

        private String leoniReferenceNumber;
        private String producer;
        private String type;
        private Integer  indexValue;
        private String referencePinePushBack;

        private String position;

        private String pinRigidityM1;
        private String pinRigidityM2;
        private String pinRigidityM3;

        private String displacementPathM1;
        private String displacementPathM2;
        private String displacementPathM3;

        private String maxSealingValueM1;
        private String maxSealingValueM2;
        private String maxSealingValueM3;

        private String programmedSealingValueM1;
        private String programmedSealingValueM2;
        private String programmedSealingValueM3;

        private String detectionsM1;
        private String detectionsM2;
        private String detectionsM3;

        private String remarks;
    }

    // DTO création dossier
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDto {

        // seulement référence du dossier
        private String reference;

        // items
        private List<TechnicalFileItemDto> items;
    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDto {

        private String reference;

        private List<UpdateItemDto> items;
    }
    // DTO update item
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateItemDto {

        private Long technicalFileItemId;

        private LocalDate maintenanceDate;
        private String technicianName;

        @JsonProperty("xCode")
        private String xCode;
        private Integer  indexValue;
        private String leoniReferenceNumber;
        private String producer;
        private String type;
        private String referencePinePushBack;

        private String position;

        private String pinRigidityM1;
        private String pinRigidityM2;
        private String pinRigidityM3;

        private String displacementPathM1;
        private String displacementPathM2;
        private String displacementPathM3;

        private String maxSealingValueM1;
        private String maxSealingValueM2;
        private String maxSealingValueM3;

        private String programmedSealingValueM1;
        private String programmedSealingValueM2;
        private String programmedSealingValueM3;

        private String detectionsM1;
        private String detectionsM2;
        private String detectionsM3;

        private String remarks;
    }

    // DTO réponse item
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseItemDto {

        private Long id;
        private Long chargeSheetItemId;
        private String itemNumber;

        private LocalDate maintenanceDate;
        private String technicianName;

        @JsonProperty("xCode")
        private String xCode;
        private Integer  indexValue;
        private String leoniReferenceNumber;
        private String producer;
        private String type;
        private String referencePinePushBack;

        private String position;

        private String pinRigidityM1;
        private String pinRigidityM2;
        private String pinRigidityM3;

        private String displacementPathM1;
        private String displacementPathM2;
        private String displacementPathM3;

        private String maxSealingValueM1;
        private String maxSealingValueM2;
        private String maxSealingValueM3;

        private String programmedSealingValueM1;
        private String programmedSealingValueM2;
        private String programmedSealingValueM3;

        private String detectionsM1;
        private String detectionsM2;
        private String detectionsM3;

        private String remarks;
        private String validationStatus;
        private String validationStatusDisplay;
        private String validatedByPp;
        private String validatedAtPp;
        private String validatedByMc;
        private String validatedAtMc;
        private String validatedByMp;
        private String validatedAtMp;
    }

    // DTO réponse dossier
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseDto {

        private Long id;
        private String reference;

        private String createdBy;
        private LocalDate createdAt;

        private String updatedBy;
        private LocalDate updatedAt;

        private List<ResponseItemDto> items;
    }

    // DTO liste dossier
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListDto {

        private Long id;
        private String reference;
        private int itemCount;
    }

    // DTO ajouter item
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddItemDto {

        private Long chargeSheetItemId;

        private LocalDate maintenanceDate;
        private String technicianName;

        @JsonProperty("xCode")
        private String xCode;
        private Integer  indexValue;
        private String leoniReferenceNumber;
        private String producer;
        private String type;
        private String referencePinePushBack;

        private String position;

        private String pinRigidityM1;
        private String pinRigidityM2;
        private String pinRigidityM3;

        private String displacementPathM1;
        private String displacementPathM2;
        private String displacementPathM3;

        private String maxSealingValueM1;
        private String maxSealingValueM2;
        private String maxSealingValueM3;

        private String programmedSealingValueM1;
        private String programmedSealingValueM2;
        private String programmedSealingValueM3;

        private String detectionsM1;
        private String detectionsM2;
        private String detectionsM3;

        private String remarks;
        private String validationStatus;
        private String validationStatusDisplay;
        private String validatedByPp;
        private String validatedAtPp;
        private String validatedByMc;
        private String validatedAtMc;
        private String validatedByMp;
        private String validatedAtMp;
    }
}