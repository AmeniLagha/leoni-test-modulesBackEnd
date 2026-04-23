package com.example.security.conformité;

import com.example.security.cahierdeCharge.*;
import com.example.security.email.GlobalNotificationService;
import com.example.security.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplianceService {

    private final ComplianceRepository repository;
    private final GlobalNotificationService notificationService;
    private final ChargeSheetItemRepository chargeSheetItemRepository;
    @Autowired
    private ComplianceReminderService reminderService;

    @Transactional
    public Compliance createCompliance(ComplianceDto.CreateDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        // ⚡ Récupérer l’item
        ChargeSheetItem item = chargeSheetItemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));
        ChargeSheet chargeSheet = item.getChargeSheet();
        String projet = chargeSheet.getProject();
        String site = chargeSheet.getPlant();
        // ⚡ Récupérer le chargeSheetId depuis l'item
        Long chargeSheetId = item.getChargeSheet().getId();
        Compliance compliance = Compliance.builder()
                .item(item) // ✅ assigné ici
                .chargeSheetId(chargeSheetId)
                .orderNumber(dto.getOrderNumber())
                .orderitemNumber(dto.getOrderitemNumber())
                .testDateTime(dto.getTestDateTime())
                .technicianName(dto.getTechnicianName())
                .rfidNumber(dto.getRfidNumber())
                .leoniPartNumber(dto.getLeoniPartNumber())
                .indexValue(dto.getIndexValue())
                .producer(dto.getProducer())
                .type(dto.getType())
                .sequenceTestPins(dto.getSequenceTestPins())
                .codingRequest(dto.getCodingRequest())
                .secondaryLocking(dto.getSecondaryLocking())
                .offsetTestMm(dto.getOffsetTestMm())
                .stableOffsetTestMm(dto.getStableOffsetTestMm())
                .displacementPathPushBackMm(dto.getDisplacementPathPushBackMm())
                .housingAttachments(dto.getHousingAttachments())
                .maxLeakTestMbar(dto.getMaxLeakTestMbar())
                .adjustmentLeakTestMbar(dto.getAdjustmentLeakTestMbar())
                .colourVerification(dto.getColourVerification())
                .terminalAlignment(dto.getTerminalAlignment())
                .openShuntsAirbag(dto.getOpenShuntsAirbag())
                .spacerClosingUnit(dto.getSpacerClosingUnit())
                .specialFunctions(dto.getSpecialFunctions())
                .contactProblemsPercentage(dto.getContactProblemsPercentage())
                .qualifiedTestModule(dto.getQualifiedTestModule())
                .conditionallyQualifiedTestModule(dto.getConditionallyQualifiedTestModule())
                .notQualifiedTestModule(dto.getNotQualifiedTestModule())
                .remarks(dto.getRemarks())
                .createdBy(currentUser.getEmail())
                .createdAt(LocalDate.now())
                .build();

        Compliance saved = repository.save(compliance);

        notificationService.notifyComplianceCreatedToProjectAndSite(
                saved.getId(),
                saved.getChargeSheetId(),
                currentUser.getEmail(),
                projet,
                site
        );
        reminderService.resetRemindersForItem(dto.getItemId());

        return saved;
    }


    @Transactional
    public Compliance updateCompliance(Long id, ComplianceDto.UpdateDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        Compliance compliance = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compliance not found"));
        ChargeSheetItem item = compliance.getItem();
        ChargeSheet chargeSheet = item.getChargeSheet();
        String projet = chargeSheet.getProject();
        String site = chargeSheet.getPlant();
        // Update inspection fields
        compliance.setOrderitemNumber(dto.getOrderitemNumber());
        compliance.setSequenceTestPins(dto.getSequenceTestPins());
        compliance.setCodingRequest(dto.getCodingRequest());
        compliance.setSecondaryLocking(dto.getSecondaryLocking());
        compliance.setOffsetTestMm(dto.getOffsetTestMm());
        compliance.setStableOffsetTestMm(dto.getStableOffsetTestMm());
        compliance.setDisplacementPathPushBackMm(dto.getDisplacementPathPushBackMm());
        compliance.setHousingAttachments(dto.getHousingAttachments());
        compliance.setMaxLeakTestMbar(dto.getMaxLeakTestMbar());
        compliance.setAdjustmentLeakTestMbar(dto.getAdjustmentLeakTestMbar());
        compliance.setColourVerification(dto.getColourVerification());
        compliance.setTerminalAlignment(dto.getTerminalAlignment());
        compliance.setOpenShuntsAirbag(dto.getOpenShuntsAirbag());
        compliance.setSpacerClosingUnit(dto.getSpacerClosingUnit());
        compliance.setSpecialFunctions(dto.getSpecialFunctions());
        compliance.setContactProblemsPercentage(dto.getContactProblemsPercentage());

        // Update qualification result
        compliance.setQualifiedTestModule(dto.getQualifiedTestModule());
        compliance.setConditionallyQualifiedTestModule(dto.getConditionallyQualifiedTestModule());
        compliance.setNotQualifiedTestModule(dto.getNotQualifiedTestModule());

        // Update remarks and status
        compliance.setRemarks(dto.getRemarks());


        compliance.setUpdatedBy(currentUser.getEmail());
        compliance.setUpdatedAt(LocalDate.now());

        Compliance updated = repository.save(compliance);

        // Notification à TOUS les utilisateurs
        notificationService.notifyComplianceUpdatedToProjectAndSite(
                updated.getId(),
                updated.getChargeSheetId(),
                currentUser.getEmail(),
                projet,
                site
        );

        return updated;
    }

    @Transactional
    public void deleteCompliance(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        Compliance compliance = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compliance not found"));
        ChargeSheetItem item = compliance.getItem();
        ChargeSheet chargeSheet = item.getChargeSheet();
        String projet = chargeSheet.getProject();
        String site = chargeSheet.getPlant();
        Long chargeSheetId = compliance.getChargeSheetId();
        repository.deleteById(id);

        // Notification à TOUS les utilisateurs
        notificationService.notifyComplianceDeletedToProjectAndSite(
                "Fiche de Conformité",
                id,
                chargeSheetId,
                currentUser.getEmail(),
                projet,
                site
        );
    }

    public List<Compliance> getComplianceByChargeSheetId(Long chargeSheetId) {
        return repository.findByChargeSheetId(chargeSheetId);
    }

    public Compliance getComplianceById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compliance not found"));
    }

    public List<Compliance> getAllCompliance() {
        return repository.findAll();
    }
    public List<ComplianceDisplayDto> getAllComplianceForDisplay() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userRole = currentUser.getRole().name();
        String userProjectsString = currentUser.getProjetsNames();  // ✅ Récupérer tous les projets
        String userSite = currentUser.getSiteName();

        // ✅ Convertir en liste
        List<String> userProjects = userProjectsString != null ?
                Arrays.asList(userProjectsString.split(", ")) :
                List.of();

        List<Compliance> list;

        if (userRole.equals("ADMIN")) {
            list = repository.findAll();
        }
        else {
            // ✅ Récupérer toutes les conformités pour les projets de l'utilisateur
            List<Compliance> allCompliances = new ArrayList<>();
            for (String project : userProjects) {
                allCompliances.addAll(repository.findByProject(project));
            }

            // Filtrer en mémoire par site (plant du chargeSheet)
            list = allCompliances.stream()
                    .filter(compliance -> {
                        ChargeSheetItem item = compliance.getItem();
                        if (item == null) return false;
                        ChargeSheet chargeSheet = item.getChargeSheet();
                        if (chargeSheet == null) return false;
                        String plant = chargeSheet.getPlant();
                        return plant != null && plant.equals(userSite);
                    })
                    .collect(Collectors.toList());

            // ✅ Appliquer les filtres de statut selon le rôle
            switch (userRole) {
                case "ING":
                    // ING voit tous les cahiers de ses projets ET site (déjà filtré)
                    break;
                case "PT":
                    list = list.stream()
                            .filter(c -> {
                                ChargeSheet cs = c.getItem().getChargeSheet();
                                return cs != null && List.of(
                                        ChargeSheetStatus.VALIDATED_ING,
                                        ChargeSheetStatus.TECH_FILLED,
                                        ChargeSheetStatus.VALIDATED_PT,
                                        ChargeSheetStatus.SENT_TO_SUPPLIER,
                                        ChargeSheetStatus.COMPLETED
                                ).contains(cs.getStatus());
                            })
                            .collect(Collectors.toList());
                    break;
                case "PP":
                    list = list.stream()
                            .filter(c -> {
                                ChargeSheet cs = c.getItem().getChargeSheet();
                                return cs != null && List.of(
                                        ChargeSheetStatus.VALIDATED_PT,
                                        ChargeSheetStatus.SENT_TO_SUPPLIER,
                                        ChargeSheetStatus.COMPLETED
                                ).contains(cs.getStatus());
                            })
                            .collect(Collectors.toList());
                    break;
                case "MC":
                case "MP":
                    list = list.stream()
                            .filter(c -> {
                                ChargeSheet cs = c.getItem().getChargeSheet();
                                return cs != null && cs.getStatus() == ChargeSheetStatus.COMPLETED;
                            })
                            .collect(Collectors.toList());
                    break;
                default:
                    list = List.of();
                    break;
            }
        }

        System.out.println("📋 Compliance pour site '" + userSite + "': " + list.size() + " élément(s)");

        return list.stream().map(c -> ComplianceDisplayDto.builder()
                .id(c.getId())
                .chargeSheetId(c.getChargeSheetId())
                .orderNumber(c.getOrderNumber())
                .itemNumber(c.getIndexValue())
                .orderitemNumber(c.getOrderitemNumber())
                .testDateTime(c.getTestDateTime())
                .technicianName(c.getTechnicianName())
                .rfidNumber(c.getRfidNumber())
                .leoniPartNumber(c.getLeoniPartNumber())
                .producer(c.getProducer())
                .indexValue(c.getIndexValue())
                .type(c.getType())
                .sequenceTestPins(c.getSequenceTestPins())
                .codingRequest(c.getCodingRequest())
                .secondaryLocking(c.getSecondaryLocking())
                .offsetTestMm(c.getOffsetTestMm())
                .stableOffsetTestMm(c.getStableOffsetTestMm())
                .displacementPathPushBackMm(c.getDisplacementPathPushBackMm())
                .housingAttachments(c.getHousingAttachments())
                .maxLeakTestMbar(c.getMaxLeakTestMbar())
                .adjustmentLeakTestMbar(c.getAdjustmentLeakTestMbar())
                .colourVerification(c.getColourVerification())
                .terminalAlignment(c.getTerminalAlignment())
                .openShuntsAirbag(c.getOpenShuntsAirbag())
                .spacerClosingUnit(c.getSpacerClosingUnit())
                .specialFunctions(c.getSpecialFunctions())
                .contactProblemsPercentage(c.getContactProblemsPercentage())
                .qualifiedTestModule(c.getQualifiedTestModule())
                .conditionallyQualifiedTestModule(c.getConditionallyQualifiedTestModule())
                .notQualifiedTestModule(c.getNotQualifiedTestModule())
                .remarks(c.getRemarks())
                .createdBy(c.getCreatedBy())
                .createdAt(c.getCreatedAt())
                .updatedBy(c.getUpdatedBy())
                .updatedAt(c.getUpdatedAt())
                .build()
        ).toList();
    }
    @Transactional
    public List<Compliance> createComplianceForItem(ChargeSheetItem item) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        List<Compliance> createdList = new ArrayList<>();

        for (int i = 1; i <= item.getQuantityOfTestModules(); i++) {
            Compliance compliance = Compliance.builder()
                    .chargeSheetId(item.getChargeSheet().getId())
                    .item(item)
                    .orderNumber(item.getChargeSheet().getOrderNumber()) // récupère order number du cahier
                    .indexValue(i) // chaque module a son index
                    .createdBy(currentUser.getEmail())
                    .createdAt(LocalDate.now())
                    .build();

            createdList.add(repository.save(compliance));
        }

        return createdList;
    }
    /**
     * Calcule la variation entre deux mois spécifiques pour les conformités
     */
    public Map<String, Object> getVariationBetweenMonths(String project, String month1, String month2) {
        List<Object[]> results;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

        // ✅ Récupérer tous les projets de l'utilisateur
        String userProjectsString = currentUser.getProjetsNames();
        List<String> userProjects = userProjectsString != null ?
                Arrays.asList(userProjectsString.split(", ")) :
                List.of();

        if (isAdmin && (project == null || project.isEmpty() || project.equals("ALL"))) {
            results = repository.countByMonthForAllProjects();
        } else {
            // ✅ Déterminer le projet cible
            String targetProject = project;
            if (targetProject == null || targetProject.isEmpty() || targetProject.equals("ALL")) {
                targetProject = userProjects.isEmpty() ? null : userProjects.get(0);
            }

            if (targetProject == null) {
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("message", "Aucun projet disponible");
                emptyResult.put("month1Count", 0L);
                emptyResult.put("month2Count", 0L);
                return emptyResult;
            }

            results = repository.countByMonthForProject(targetProject);
        }

        Map<String, Long> monthlyCounts = new HashMap<>();
        for (Object[] result : results) {
            String month = (String) result[0];
            Long count = ((Number) result[1]).longValue();
            monthlyCounts.put(month, count);
        }

        Long countMonth1 = monthlyCounts.getOrDefault(month1, 0L);
        Long countMonth2 = monthlyCounts.getOrDefault(month2, 0L);

        double variation = 0.0;
        String trend = "stable";
        String formula = "";

        if (countMonth1 > 0) {
            variation = ((countMonth2 - countMonth1) * 100.0) / countMonth1;
            variation = Math.round(variation * 10.0) / 10.0;

            if (variation > 0) {
                trend = "hausse";
            } else if (variation < 0) {
                trend = "baisse";
            }

            formula = String.format("((%d - %d) / %d) × 100 = %.1f%%",
                    countMonth2, countMonth1, countMonth1, variation);
        } else if (countMonth1 == 0 && countMonth2 > 0) {
            variation = 100.0;
            trend = "hausse";
            formula = String.format("Création depuis zéro: %d fiches", countMonth2);
        } else if (countMonth1 == 0 && countMonth2 == 0) {
            formula = "Aucune conformité sur les deux mois";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("project", (isAdmin && (project == null || project.isEmpty() || project.equals("ALL"))) ? "TOUS_PROJETS" : project);
        result.put("month1", month1);
        result.put("month1Count", countMonth1);
        result.put("month2", month2);
        result.put("month2Count", countMonth2);
        result.put("variation", variation);
        result.put("trend", trend);
        result.put("formula", formula);
        result.put("interpretation", getInterpretation(trend, variation, countMonth2, countMonth1));

        return result;
    }

    /**
     * Récupère les statistiques de création mensuelles des conformités
     */
    public MonthlyComplianceStatsDto getMonthlyComplianceStats(String project, int numberOfMonths) {
        List<Object[]> results;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

        if (isAdmin && (project == null || project.isEmpty() || project.equals("ALL"))) {
            results = repository.countByMonthForAllProjects();
        } else {
            String userProject = (project != null && !project.isEmpty() && !project.equals("ALL")) ? project : currentUser.getProjetsNames();
            results = repository.countByMonthForProject(userProject);
        }

        Map<String, Long> monthlyCounts = new LinkedHashMap<>();
        Map<String, Double> monthlyVariations = new LinkedHashMap<>();

        for (Object[] result : results) {
            String month = (String) result[0];
            Long count = ((Number) result[1]).longValue();
            monthlyCounts.put(month, count);
        }

        // Calculer les variations
        List<String> months = new ArrayList<>(monthlyCounts.keySet());
        for (int i = 0; i < months.size() - 1; i++) {
            String currentMonth = months.get(i);
            String previousMonth = months.get(i + 1);

            Long currentCount = monthlyCounts.get(currentMonth);
            Long previousCount = monthlyCounts.get(previousMonth);

            if (previousCount != null && previousCount > 0) {
                double variation = ((currentCount - previousCount) * 100.0) / previousCount;
                monthlyVariations.put(currentMonth, Math.round(variation * 10.0) / 10.0);
            } else if (previousCount != null && previousCount == 0) {
                monthlyVariations.put(currentMonth, currentCount > 0 ? 100.0 : 0.0);
            } else {
                monthlyVariations.put(currentMonth, 0.0);
            }
        }

        // Calculer la variation pour les deux derniers mois
        String currentMonth = null;
        String previousMonth = null;
        double variationPercentage = 0.0;
        String trend = "stable";

        if (months.size() >= 2) {
            currentMonth = months.get(0);
            previousMonth = months.get(1);
            variationPercentage = monthlyVariations.getOrDefault(currentMonth, 0.0);

            if (variationPercentage > 0) {
                trend = "hausse";
            } else if (variationPercentage < 0) {
                trend = "baisse";
            }
        }

        // Limiter au nombre de mois demandé
        Map<String, Long> limitedMonthlyCounts = new LinkedHashMap<>();
        Map<String, Double> limitedMonthlyVariations = new LinkedHashMap<>();

        int count = 0;
        for (Map.Entry<String, Long> entry : monthlyCounts.entrySet()) {
            if (count >= numberOfMonths) break;
            limitedMonthlyCounts.put(entry.getKey(), entry.getValue());
            if (monthlyVariations.containsKey(entry.getKey())) {
                limitedMonthlyVariations.put(entry.getKey(), monthlyVariations.get(entry.getKey()));
            }
            count++;
        }

        String formula = "";
        if (currentMonth != null && previousMonth != null) {
            Long currentCount = monthlyCounts.get(currentMonth);
            Long prevCount = monthlyCounts.get(previousMonth);
            if (prevCount != null && prevCount > 0) {
                formula = String.format("((%d - %d) / %d) × 100 = %.1f%%",
                        currentCount, prevCount, prevCount, variationPercentage);
            }
        }

        return MonthlyComplianceStatsDto.builder()
                .monthlyCounts(limitedMonthlyCounts)
                .monthlyVariations(limitedMonthlyVariations)
                .currentMonth(currentMonth)
                .previousMonth(previousMonth)
                .variationPercentage(variationPercentage)
                .trend(trend)
                .formula(formula)
                .build();
    }

    /**
     * Version simplifiée pour le dashboard avec les deux derniers mois
     */
    public Map<String, Object> getLastTwoMonthsVariation(String project) {
        List<Object[]> results;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

        // ✅ Récupérer tous les projets de l'utilisateur
        String userProjectsString = currentUser.getProjetsNames();
        List<String> userProjects = userProjectsString != null ?
                Arrays.asList(userProjectsString.split(", ")) :
                List.of();

        if (isAdmin && (project == null || project.isEmpty() || project.equals("ALL"))) {
            results = repository.countByMonthForAllProjects();
        } else {
            String targetProject = project;
            if (targetProject == null || targetProject.isEmpty() || targetProject.equals("ALL")) {
                targetProject = userProjects.isEmpty() ? null : userProjects.get(0);
            }

            if (targetProject == null) {
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("message", "Aucun projet disponible");
                emptyResult.put("availableMonths", 0);
                return emptyResult;
            }

            results = repository.countByMonthForProject(targetProject);
        }

        if (results == null || results.size() < 2) {
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("message", "Pas assez de données pour calculer la variation");
            emptyResult.put("availableMonths", results != null ? results.size() : 0);
            return emptyResult;
        }

        Object[] latestMonth = results.get(0);
        Object[] previousMonth = results.get(1);

        String monthLatest = (String) latestMonth[0];
        Long countLatest = ((Number) latestMonth[1]).longValue();
        String monthPrevious = (String) previousMonth[0];
        Long countPrevious = ((Number) previousMonth[1]).longValue();

        double variation = 0.0;
        String trend = "stable";
        String formula = "";

        if (countPrevious > 0) {
            variation = ((countLatest - countPrevious) * 100.0) / countPrevious;
            variation = Math.round(variation * 10.0) / 10.0;

            if (variation > 0) {
                trend = "hausse";
            } else if (variation < 0) {
                trend = "baisse";
            }

            formula = String.format("((%d - %d) / %d) × 100 = %.1f%%",
                    countLatest, countPrevious, countPrevious, variation);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("currentMonth", monthLatest);
        result.put("currentMonthCount", countLatest);
        result.put("previousMonth", monthPrevious);
        result.put("previousMonthCount", countPrevious);
        result.put("variation", variation);
        result.put("trend", trend);
        result.put("formula", formula);
        result.put("interpretation", getInterpretation(trend, variation, countLatest, countPrevious));

        return result;
    }

    private String getInterpretation(String trend, double variation, long currentCount, long previousCount) {
        if (trend.equals("hausse")) {
            if (variation > 50) {
                return String.format("📈 Forte augmentation de %.1f%% (%d vs %d)", variation, currentCount, previousCount);
            } else {
                return String.format("📈 Augmentation de %.1f%% (%d vs %d)", variation, currentCount, previousCount);
            }
        } else if (trend.equals("baisse")) {
            if (variation < -50) {
                return String.format("📉 Forte baisse de %.1f%% (%d vs %d)", Math.abs(variation), currentCount, previousCount);
            } else {
                return String.format("📉 Baisse de %.1f%% (%d vs %d)", Math.abs(variation), currentCount, previousCount);
            }
        } else {
            return String.format("➡️ Stable (%d fiches de conformité pour les deux mois)", currentCount);
        }


    }
}