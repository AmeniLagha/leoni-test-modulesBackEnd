package com.example.security.cahierdeCharge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class ReceptionDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceptionItemDto {
        private Long itemId;
        private String itemNumber;
        private int quantityOrdered;
        private int quantityReceived;
        private int quantityRemaining;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceptionRequestDto {
        private Long chargeSheetId;
        private List<ReceptionItemDto> items;
        private String receptionDate;
        private String deliveryNoteNumber;
        private String comments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceptionResultDto {
        private Long receptionHistoryId;
        private List<Long> createdComplianceIds;
        private int quantityReceived;
        private String itemNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceptionResponseDto {
        private Long chargeSheetId;
        private List<ReceptionItemDto> items;
        private String message;
        private boolean complete;
        private List<ReceptionResultDto> receptionResults;
    }
}