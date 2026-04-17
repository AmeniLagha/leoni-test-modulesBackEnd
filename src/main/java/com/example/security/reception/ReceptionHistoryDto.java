package com.example.security.reception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceptionHistoryDto {
    private Long id;
    private ItemInfoDto item;
    private int quantityReceived;
    private int previousTotalReceived;
    private int newTotalReceived;
    private int quantityOrdered;
    private String deliveryNoteNumber;
    private LocalDate receptionDate;
    private String receivedBy;
    private String comments;
    private LocalDate createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemInfoDto {
        private Long id;
        private String itemNumber;
    }
}