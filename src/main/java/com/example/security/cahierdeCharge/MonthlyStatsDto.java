package com.example.security.cahierdeCharge;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class MonthlyStatsDto {
    private Map<String, Long> monthlyCounts; // Mois -> Nombre de cahiers
    private Map<String, Double> monthlyVariations; // Mois -> Pourcentage de variation
    private String currentMonth;
    private String previousMonth;
    private double variationPercentage;
    private String trend; // "hausse", "baisse", "stable"
    private String formula;
}