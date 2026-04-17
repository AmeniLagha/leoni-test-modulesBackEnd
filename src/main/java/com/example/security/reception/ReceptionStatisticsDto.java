// Créez un fichier ReceptionStatisticsDto.java
package com.example.security.reception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceptionStatisticsDto {

    // Statistiques globales
    private Long totalQuantityReceived;
    private Long totalQuantityOrdered;
    private Double completionRate;
    private Integer numberOfItems;
    private Integer numberOfSheets;

    // Par mois
    private Map<String, MonthlyReceptionStats> monthlyStats;

    // Par item
    private List<ItemReceptionStats> itemStats;

    // Données pour histogramme
    private ChartData chartData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyReceptionStats {
        private String month;
        private int year;
        private Long quantityReceived;
        private Long quantityOrdered;
        private Double completionRate;
        private Integer numberOfReceptions;
        private Integer numberOfItems;
        private Integer numberOfSheets;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemReceptionStats {
        private Long itemId;
        private String itemNumber;
        private String project;
        private String plant;
        private Integer quantityOrdered;
        private Integer quantityReceived;
        private Double completionRate;
        private Integer pendingQuantity;
        private String status;
        private LocalDate lastReceptionDate;
        private Integer numberOfReceptions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartData {
        private List<String> labels;      // Mois
        private List<Long> receivedData;  // Quantités reçues
        private List<Long> orderedData;   // Quantités commandées
        private List<Double> percentages; // Pourcentages
    }
}