package com.example.security.conformité;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class MonthlyComplianceStatsDto {
    private Map<String, Long> monthlyCounts;
    private Map<String, Double> monthlyVariations;
    private String currentMonth;
    private String previousMonth;
    private double variationPercentage;
    private String trend;
    private String formula;
}