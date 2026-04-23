package com.example.security.reclamation;

import com.example.security.cahierdeCharge.ChargeSheet;
import com.example.security.cahierdeCharge.ChargeSheetRepository;
import com.example.security.cahierdeCharge.ImageStorageService;
import com.example.security.email.GlobalNotificationService;
import com.example.security.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository repository;
    private final GlobalNotificationService notificationService;
    private final ImageStorageService imageStorageService;
    private final ChargeSheetRepository chargeSheetRepository;

    // ClaimService.java - Modifier la méthode createClaim

    // ClaimService.java - Modifier la méthode createClaim

    @Transactional
    public Claim createClaim(ClaimDto.CreateDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        User currentUser = (User) auth.getPrincipal();

        // Récupérer le site de l'utilisateur
        String userSite = currentUser.getSiteName();

        Claim claim = Claim.builder()
                .chargeSheetId(dto.getChargeSheetId())
                .relatedTo(dto.getRelatedTo())
                .relatedId(dto.getRelatedId())
                // NOUVEAUX CHAMPS
                .plant(dto.getPlant() != null ? dto.getPlant() : userSite)
                .customer(dto.getCustomer())
                .contactPerson(dto.getContactPerson())
                .customerEmail(dto.getCustomerEmail())
                .customerPhone(dto.getCustomerPhone())
                .supplier(dto.getSupplier())
                .supplierContactPerson(dto.getSupplierContactPerson())
                .orderNumber(dto.getOrderNumber())
                .testModuleNumber(dto.getTestModuleNumber())
                .testModuleQuantity(dto.getTestModuleQuantity())
                .ppoSignature(dto.getPpoSignature())
                .problemWhatHappened(dto.getProblemWhatHappened())
                .problemWhy(dto.getProblemWhy())
                .problemWhenDetected(dto.getProblemWhenDetected())
                .problemWhoDetected(dto.getProblemWhoDetected())
                .problemWhereDetected(dto.getProblemWhereDetected())
                .problemHowDetected(dto.getProblemHowDetected())
                .claimDate(dto.getClaimDate() != null ? dto.getClaimDate() : LocalDate.now())
                // Champs existants
                .title(dto.getTitle())
                .description(dto.getDescription())
                .priority(dto.getPriority() != null ? dto.getPriority() : Claim.Priority.MEDIUM)
                .category(dto.getCategory())
                .imagePath(dto.getImagePath())
                .status(Claim.ClaimStatus.ASSIGNED)
                .reportedBy(currentUserEmail)
                .reportedDate(LocalDate.now())
                .assignedTo(dto.getAssignedTo())
                .assignedDate(dto.getAssignedTo() != null ? LocalDate.now() : null)
                .createdBy(currentUserEmail)
                .createdAt(LocalDate.now())
                .build();

        Claim savedClaim = repository.save(claim);

        // ✅ AJOUTER LA NOTIFICATION À LA PERSONNE ASSIGNÉE
        if (savedClaim.getAssignedTo() != null && !savedClaim.getAssignedTo().isEmpty()) {
            // Envoyer un email à la personne assignée
            String subject = "🔔 Nouvelle réclamation assignée - #" + savedClaim.getId();
            String message = String.format(
                    "Bonjour,\n\n" +
                            "Une nouvelle réclamation vous a été assignée.\n\n" +
                            "📋 **Titre:** %s\n" +
                            "🔴 **Priorité:** %s\n" +
                            "📝 **Description:** %s\n" +
                            "👤 **Signalé par:** %s\n" +
                            "📅 **Date:** %s\n\n" +
                            "🔗 Veuillez vous connecter à l'application pour traiter cette réclamation.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe Qualité Leoni",
                    savedClaim.getTitle(),
                    savedClaim.getPriority(),
                    savedClaim.getDescription(),
                    currentUserEmail,
                    LocalDate.now()
            );

            notificationService.sendNotificationToOneUser(subject, message, savedClaim.getAssignedTo());

        }

        return savedClaim;
    }

    @Transactional
    public Claim updateClaim(Long id, ClaimDto.UpdateDto dto) {
        try {
            Claim claim = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();
            // ✅ Récupérer les infos projet et site
            String projet = null;
            String site = null;
            ChargeSheet chargeSheet = chargeSheetRepository.findById(claim.getChargeSheetId()).orElse(null);
            if (chargeSheet != null) {
                projet = chargeSheet.getProject();
                site = chargeSheet.getPlant();
            }
            // Mise à jour des champs de base
            if (dto.getTitle() != null) claim.setTitle(dto.getTitle());
            if (dto.getDescription() != null) claim.setDescription(dto.getDescription());
            if (dto.getPriority() != null) claim.setPriority(dto.getPriority());
            if (dto.getCategory() != null) claim.setCategory(dto.getCategory());
            if (dto.getImagePath() != null) claim.setImagePath(dto.getImagePath());
            // Gestion du changement de statut
            if (dto.getStatus() != null) {
                claim.setStatus(dto.getStatus());

                // Si le statut est ASSIGNED et assignedTo est défini
                if (dto.getStatus() == Claim.ClaimStatus.ASSIGNED && dto.getAssignedTo() != null) {
                    claim.setAssignedTo(dto.getAssignedTo());
                    claim.setAssignedDate(LocalDate.now());
                }

                // Si le statut est RESOLVED
                if (dto.getStatus() == Claim.ClaimStatus.RESOLVED) {
                    claim.setResolvedBy(currentUser.getEmail());
                    claim.setResolvedDate(LocalDate.now());
                }

                // Si le statut est CLOSED
                if (dto.getStatus() == Claim.ClaimStatus.CLOSED) {
                    claim.setClosedBy(currentUser.getEmail());
                    claim.setClosedDate(LocalDate.now());
                }
            }

            // Mise à jour de l'assignation
            if (dto.getAssignedTo() != null && !dto.getAssignedTo().equals(claim.getAssignedTo())) {
                claim.setAssignedTo(dto.getAssignedTo());
                claim.setAssignedDate(LocalDate.now());
                claim.setStatus(Claim.ClaimStatus.ASSIGNED);
            }

            // Actions et résolution
            if (dto.getActionTaken() != null) claim.setActionTaken(dto.getActionTaken());
            if (dto.getResolution() != null) claim.setResolution(dto.getResolution());

            // Dates estimées et réelles
            if (dto.getEstimatedResolutionDate() != null) {
                claim.setEstimatedResolutionDate(dto.getEstimatedResolutionDate());
            }
            if (dto.getActualResolutionDate() != null) {
                claim.setActualResolutionDate(dto.getActualResolutionDate());
            }

            claim.setUpdatedBy(currentUser.getEmail());
            claim.setUpdatedAt(LocalDate.now());

            Claim updated = repository.save(claim);

            // ✅ Notification modifiée
            notificationService.notifyClaimUpdatedToProjectAndSite(
                    updated.getId(),
                    updated.getTitle(),
                    updated.getChargeSheetId(),
                    currentUser.getEmail(),
                    "MODIFIÉE",
                    projet,
                    site
            );

            return updated;
        } catch (Exception e) {
            System.err.println("❌ Erreur dans updateClaim: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public Claim assignClaim(Long id, ClaimDto.AssignmentDto dto) {
        try {
            Claim claim = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();

            // ✅ Récupérer les infos projet et site
            String projet = null;
            String site = null;
            ChargeSheet chargeSheet = chargeSheetRepository.findById(claim.getChargeSheetId()).orElse(null);
            if (chargeSheet != null) {
                projet = chargeSheet.getProject();
                site = chargeSheet.getPlant();
            }

            String oldAssignedTo = claim.getAssignedTo();

            claim.setAssignedTo(dto.getAssignedTo());
            claim.setAssignedDate(LocalDate.now());
            claim.setStatus(Claim.ClaimStatus.ASSIGNED);

            if (dto.getEstimatedResolutionDate() != null) {
                claim.setEstimatedResolutionDate(dto.getEstimatedResolutionDate());
            }

            claim.setUpdatedBy(currentUser.getEmail());
            claim.setUpdatedAt(LocalDate.now());

            Claim updated = repository.save(claim);

            // ✅ Notification à la nouvelle personne assignée (individuelle)
            if (dto.getAssignedTo() != null && !dto.getAssignedTo().isEmpty()) {
                String subject = "🔔 Réclamation assignée - #" + updated.getId();
                String message = String.format(
                        "Bonjour,\n\n" +
                                "Une réclamation vous a été assignée.\n\n" +
                                "📋 **Titre:** %s\n" +
                                "🔴 **Priorité:** %s\n" +
                                "📝 **Description:** %s\n" +
                                "👤 **Assigné par:** %s\n" +
                                "📅 **Date d'assignation:** %s\n\n" +
                                "🔗 Veuillez vous connecter à l'application pour traiter cette réclamation.\n\n" +
                                "Cordialement,\n" +
                                "L'équipe Qualité Leoni",
                        updated.getTitle(),
                        updated.getPriority(),
                        updated.getDescription(),
                        currentUser.getEmail(),
                        LocalDate.now()
                );

                notificationService.sendNotificationToOneUser(subject, message, dto.getAssignedTo());
            }

            // ✅ Notification à tous les utilisateurs du projet et site (optionnel)
            // Pour informer l'équipe du changement d'assignation
            if (oldAssignedTo != null && !oldAssignedTo.equals(dto.getAssignedTo())) {
                notificationService.notifyClaimUpdatedToProjectAndSite(
                        updated.getId(),
                        updated.getTitle(),
                        updated.getChargeSheetId(),
                        currentUser.getEmail(),
                        "ASSIGNATION MODIFIÉE (de " + oldAssignedTo + " à " + dto.getAssignedTo() + ")",
                        projet,
                        site
                );
            } else {
                notificationService.notifyClaimUpdatedToProjectAndSite(
                        updated.getId(),
                        updated.getTitle(),
                        updated.getChargeSheetId(),
                        currentUser.getEmail(),
                        "ASSIGNÉE",
                        projet,
                        site
                );
            }

            return updated;
        } catch (Exception e) {
            System.err.println("❌ Erreur dans assignClaim: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public Claim resolveClaim(Long id, ClaimDto.ResolutionDto dto) {
        try {
            Claim claim = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();
            // ✅ Récupérer les infos projet et site
            String projet = null;
            String site = null;
            ChargeSheet chargeSheet = chargeSheetRepository.findById(claim.getChargeSheetId()).orElse(null);
            if (chargeSheet != null) {
                projet = chargeSheet.getProject();
                site = chargeSheet.getPlant();
            }
            claim.setActionTaken(dto.getActionTaken());
            claim.setResolution(dto.getResolution());
            claim.setResolvedBy(currentUser.getEmail());
            claim.setResolvedDate(LocalDate.now());

            if (dto.getActualResolutionDate() != null) {
                claim.setActualResolutionDate(dto.getActualResolutionDate());
            } else {
                claim.setActualResolutionDate(LocalDate.now());
            }

            // ⚡ Fix : statut RESOLVED
            claim.setStatus(Claim.ClaimStatus.RESOLVED);

            claim.setUpdatedBy(currentUser.getEmail());
            claim.setUpdatedAt(LocalDate.now());

            Claim updated = repository.save(claim);

            // ✅ Notification modifiée
            notificationService.notifyClaimUpdatedToProjectAndSite(
                    updated.getId(),
                    updated.getTitle(),
                    updated.getChargeSheetId(),
                    currentUser.getEmail(),
                    "RÉSOLUE",
                    projet,
                    site
            );


            return updated;
        } catch (Exception e) {
            System.err.println("❌ Erreur dans resolveClaim: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    @Transactional
    public void deleteClaim(Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();

            Claim claim = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            Long chargeSheetId = claim.getChargeSheetId();
            String claimTitle = claim.getTitle();
            String projet = null;
            String site = null;
            ChargeSheet chargeSheet = chargeSheetRepository.findById(chargeSheetId).orElse(null);
            if (chargeSheet != null) {
                projet = chargeSheet.getProject();
                site = chargeSheet.getPlant();
            }
            repository.deleteById(id);

            // ✅ Notification modifiée
            notificationService.notifyClaimDeletedToProjectAndSite(
                    "Réclamation: " + claimTitle,
                    id,
                    chargeSheetId,
                    currentUser.getEmail(),
                    projet,
                    site
            );
        } catch (Exception e) {
            System.err.println("❌ Erreur dans deleteClaim: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Claim> getClaimsByChargeSheetId(Long chargeSheetId) {
        return repository.findByChargeSheetId(chargeSheetId);
    }

    public List<Claim> getClaimsByReportedBy(String email) {
        return repository.findByReportedBy(email);
    }

    public List<Claim> getClaimsByAssignedTo(String email) {
        return repository.findByAssignedTo(email);
    }

    public List<Claim> getClaimsByStatus(Claim.ClaimStatus status) {
        return repository.findByStatus(status);
    }

    public List<Claim> getClaimsByPriority(Claim.Priority priority) {
        return repository.findByPriority(priority);
    }

    public List<Claim> getClaimsByCategory(String category) {
        return repository.findByCategory(category);
    }

    public List<Claim> getClaimsByRelatedItem(String relatedTo, Long relatedId) {
        return repository.findByRelatedToAndRelatedId(relatedTo, relatedId);
    }

    public Claim getClaimById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
    }

    public List<Claim> getAllClaims() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();
            String userRole = currentUser.getRole().name();
            String userProjectsString = currentUser.getProjetsNames();
            List<String> userProjects = userProjectsString != null ?
                    Arrays.asList(userProjectsString.split(", ")) :
                    List.of();
            String userSite = currentUser.getSiteName();

            List<Claim> allClaims = repository.findAll();

            if (userRole.equals("ADMIN")) {
                return allClaims;
            }

            // ✅ Filtrer comme les autres services
            return allClaims.stream()
                    .filter(claim -> {
                        // Récupérer le ChargeSheet associé à cette réclamation
                        ChargeSheet chargeSheet = chargeSheetRepository.findById(claim.getChargeSheetId()).orElse(null);
                        if (chargeSheet == null) return false;

                        // Vérifier le site (plant)
                        String plant = chargeSheet.getPlant();
                        if (plant == null || !plant.equals(userSite)) return false;

                        // ✅ Vérifier que le projet est dans la liste des projets de l'utilisateur
                        String project = chargeSheet.getProject();
                        return project != null && userProjects.contains(project);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("❌ Erreur dans getAllClaims: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    @Transactional
    public Claim updateClaimStatus(Long id, Claim.ClaimStatus status) {
        try{
            Claim claim = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();
            // ✅ Récupérer les infos projet et site
            String projet = null;
            String site = null;
            ChargeSheet chargeSheet = chargeSheetRepository.findById(claim.getChargeSheetId()).orElse(null);
            if (chargeSheet != null) {
                projet = chargeSheet.getProject();
                site = chargeSheet.getPlant();
            }
            claim.setStatus(status);

            // Mise à jour des dates selon le statut
            if (status == Claim.ClaimStatus.RESOLVED) {
                claim.setResolvedBy(currentUser.getEmail());
                claim.setResolvedDate(LocalDate.now());
            } else if (status == Claim.ClaimStatus.CLOSED) {
                claim.setClosedBy(currentUser.getEmail());
                claim.setClosedDate(LocalDate.now());
            }

            claim.setUpdatedBy(currentUser.getEmail());
            claim.setUpdatedAt(LocalDate.now());

            Claim updated = repository.save(claim);

            // ✅ Notification modifiée
            notificationService.notifyClaimUpdatedToProjectAndSite(
                    updated.getId(),
                    updated.getTitle(),
                    updated.getChargeSheetId(),
                    currentUser.getEmail(),
                    "STATUT MIS À JOUR",
                    projet,
                    site
            );

            return updated;
        } catch (Exception e) {
            System.err.println("❌ Erreur dans updateClaimStatus: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<Claim> searchClaims(String keyword) {
        return repository.findAll();
    }
    /**
     * Calcule la variation entre deux mois spécifiques pour les réclamations
     */
    /**
     * Calcule la variation entre deux mois spécifiques pour les réclamations
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
            formula = String.format("Création depuis zéro: %d réclamations", countMonth2);
        } else if (countMonth1 == 0 && countMonth2 == 0) {
            formula = "Aucune réclamation sur les deux mois";
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
        result.put("interpretation", getClaimInterpretation(trend, variation, countMonth2, countMonth1));

        return result;
    }

    /**
     * Version simplifiée pour le dashboard avec les deux derniers mois
     */
    /**
     * Version simplifiée pour le dashboard avec les deux derniers mois
     */
    public Map<String, Object> getLastTwoMonthsVariation(String project) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

        // ✅ Récupérer tous les projets de l'utilisateur
        String userProjectsString = currentUser.getProjetsNames();
        List<String> userProjects = userProjectsString != null ?
                Arrays.asList(userProjectsString.split(", ")) :
                List.of();

        List<Object[]> results;

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
        result.put("interpretation", getClaimInterpretation(trend, variation, countLatest, countPrevious));

        return result;
    }

    private String getClaimInterpretation(String trend, double variation, long currentCount, long previousCount) {
        if (trend.equals("hausse")) {
            if (variation > 50) {
                return String.format("📈 Forte augmentation de %.1f%% (%d vs %d réclamations)", variation, currentCount, previousCount);
            } else {
                return String.format("📈 Augmentation de %.1f%% (%d vs %d réclamations)", variation, currentCount, previousCount);
            }
        } else if (trend.equals("baisse")) {
            if (variation < -50) {
                return String.format("📉 Forte baisse de %.1f%% (%d vs %d réclamations)", Math.abs(variation), currentCount, previousCount);
            } else {
                return String.format("📉 Baisse de %.1f%% (%d vs %d réclamations)", Math.abs(variation), currentCount, previousCount);
            }
        } else {
            return String.format("➡️ Stable (%d réclamations pour les deux mois)", currentCount);
        }
    }
}