package com.example.security.reclamation;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository repository;
    private final GlobalNotificationService notificationService;
    private final ImageStorageService imageStorageService;

    @Transactional
    public Claim createClaim(ClaimDto.CreateDto dto) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();

            Claim claim = Claim.builder()
                    .chargeSheetId(dto.getChargeSheetId())
                    .relatedTo(dto.getRelatedTo())
                    .relatedId(dto.getRelatedId())
                    .imagePath(dto.getImagePath())
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .priority(dto.getPriority() != null ? dto.getPriority() : Claim.Priority.MEDIUM)
                    .category(dto.getCategory() != null ? dto.getCategory() : "OTHER")
                    .status(Claim.ClaimStatus.ASSIGNED)
                    .reportedBy(currentUser.getEmail())
                    .reportedDate(LocalDate.now())
                    .assignedTo(dto.getAssignedTo())
                    .assignedDate(dto.getAssignedTo() != null ? LocalDate.now() : null)
                    .createdBy(currentUser.getEmail())
                    .createdAt(LocalDate.now())
                    .build();

            Claim saved = repository.save(claim);

            notificationService.notifyClaimAssigned(saved, currentUser.getEmail());


            return saved;
        } catch (Exception e) {
            System.err.println("❌ Erreur dans createClaim: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public Claim updateClaim(Long id, ClaimDto.UpdateDto dto) {
        try {
            Claim claim = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();

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

            // Notification à TOUS les utilisateurs
            notificationService.notifyClaimUpdated(
                    updated.getId(),
                    updated.getTitle(),
                    updated.getChargeSheetId(),
                    currentUser.getEmail(),
                    "MODIFIÉE"
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

            claim.setAssignedTo(dto.getAssignedTo());
            claim.setAssignedDate(LocalDate.now());
            claim.setStatus(Claim.ClaimStatus.ASSIGNED);

            if (dto.getEstimatedResolutionDate() != null) {
                claim.setEstimatedResolutionDate(dto.getEstimatedResolutionDate());
            }

            claim.setUpdatedBy(currentUser.getEmail());
            claim.setUpdatedAt(LocalDate.now());

            Claim updated = repository.save(claim);

            notificationService.notifyClaimAssigned(updated, currentUser.getEmail());


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

            notificationService.notifyClaimUpdated(
                    updated.getId(),
                    updated.getTitle(),
                    updated.getChargeSheetId(),
                    currentUser.getEmail(),
                    "RÉSOLUE"
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
            repository.deleteById(id);

            // Notification à TOUS les utilisateurs
            notificationService.notifyDocumentDeleted(
                    "Réclamation: " + claimTitle,
                    id,
                    chargeSheetId,
                    currentUser.getEmail()
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
            return repository.findAll();
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

            // Notification à TOUS les utilisateurs
            notificationService.notifyClaimUpdated(
                    updated.getId(),
                    updated.getTitle(),
                    updated.getChargeSheetId(),
                    currentUser.getEmail(),
                    "STATUT MIS À JOUR"
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
    public Map<String, Object> getVariationBetweenMonths(String project, String month1, String month2) {
        List<Object[]> results;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

        if (isAdmin && (project == null || project.isEmpty() || project.equals("ALL"))) {
            results = repository.countByMonthForAllProjects();
        } else {
            // Pour les réclamations, on filtre par projet via chargeSheet
            String userProject = (project != null && !project.isEmpty() && !project.equals("ALL")) ? project : currentUser.getProjet();
            results = repository.countByMonthForAllProjects(); // TODO: filtrer par projet
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
    public Map<String, Object> getLastTwoMonthsVariation(String project) {
        List<Object[]> results = repository.countByMonthForAllProjects();

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