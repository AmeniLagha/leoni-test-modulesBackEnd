package com.example.security.reclamation;

import com.example.security.cahierdeCharge.ImageStorageService;
import com.example.security.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@CrossOrigin(
        origins = "http://localhost:4200",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
@RestController
@RequestMapping("/api/v1/claims")
@Tag(name = "Réclamations", description = "Gestion des réclamations, suivi, assignation et résolution")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService service;
    private final ClaimRepository repository;

    // PP, MC, MP, PT: Créer une réclamation
    @PostMapping
    @PreAuthorize("hasAuthority('claim:write')")
    @Operation(summary = "Créer une réclamation", description = "Créer une nouvelle réclamation liée à un problème")
    public ResponseEntity<ApiResponse<Claim>> createClaim(@RequestBody ClaimDto.CreateDto dto) {
        Claim created = service.createClaim(dto);

        ApiResponse<Claim> response = ApiResponse.success(
                "Réclamation créée avec succès",
                created,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Mettre à jour une réclamation
    @PutMapping("/{id}")
    @Operation(summary = "Modifier une réclamation", description = "Mettre à jour une réclamation existante")
    @PreAuthorize("hasAuthority('claim:write')")
    public ResponseEntity<ApiResponse<Claim>> updateClaim(
            @PathVariable Long id,
            @RequestBody ClaimDto.UpdateDto dto) {
        Claim updated = service.updateClaim(id, dto);

        ApiResponse<Claim> response = ApiResponse.success(
                "Réclamation mise à jour avec succès",
                updated
        );
        return ResponseEntity.ok(response);
    }

    // Assigner une réclamation
    @PutMapping("/{id}/assign")
    @Operation(summary = "Assigner une réclamation", description = "Assigner une réclamation à un utilisateur")
    @PreAuthorize("hasAuthority('claim:write')")
    public ResponseEntity<ApiResponse<Claim>> assignClaim(
            @PathVariable Long id,
            @RequestBody ClaimDto.AssignmentDto dto) {
        Claim assigned = service.assignClaim(id, dto);

        ApiResponse<Claim> response = ApiResponse.success(
                "Réclamation assignée avec succès",
                assigned
        );
        return ResponseEntity.ok(response);
    }

    // Résoudre une réclamation
    @PutMapping("/{id}/resolve")
    @Operation(summary = "Résoudre une réclamation", description = "Marquer une réclamation comme résolue")
    @PreAuthorize("hasAuthority('claim:write')")
    public ResponseEntity<ApiResponse<Claim>> resolveClaim(
            @PathVariable Long id,
            @RequestBody ClaimDto.ResolutionDto dto) {
        Claim resolved = service.resolveClaim(id, dto);

        ApiResponse<Claim> response = ApiResponse.success(
                "Réclamation résolue avec succès",
                resolved
        );
        return ResponseEntity.ok(response);
    }

    // Changer le statut d'une réclamation
    @PatchMapping("/{id}/status/{status}")
    @Operation(summary = "Changer statut", description = "Mettre à jour le statut d’une réclamation")
    @PreAuthorize("hasAuthority('claim:write')")
    public ResponseEntity<ApiResponse<Claim>> updateClaimStatus(
            @PathVariable Long id,
            @PathVariable Claim.ClaimStatus status) {
        Claim updated = service.updateClaimStatus(id, status);

        ApiResponse<Claim> response = ApiResponse.success(
                "Statut de la réclamation mis à jour avec succès",
                updated
        );
        return ResponseEntity.ok(response);
    }

    // Lire une réclamation spécifique
    @GetMapping("/{id}")
    @Operation(summary = "Consulter une réclamation", description = "Afficher une réclamation par ID")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<ApiResponse<Claim>> getClaim(@PathVariable Long id) {
        Claim claim = service.getClaimById(id);

        ApiResponse<Claim> response = ApiResponse.success(
                "Réclamation récupérée avec succès",
                claim
        );
        return ResponseEntity.ok(response);
    }
    // Récupérer les réclamations par cahier des charges
    @GetMapping("/charge-sheet/{chargeSheetId}")
    @Operation(summary = "Réclamations par cahier", description = "Lister les réclamations liées à un cahier")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<ApiResponse<List<Claim>>> getClaimsByChargeSheet(
            @PathVariable Long chargeSheetId) {
        List<Claim> claims = service.getClaimsByChargeSheetId(chargeSheetId);

        ApiResponse<List<Claim>> response = ApiResponse.success(
                "Liste des réclamations récupérée avec succès",
                claims
        );
        return ResponseEntity.ok(response);
    }

    // Récupérer les réclamations par élément lié
    @GetMapping("/related/{relatedTo}/{relatedId}")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<ApiResponse<List<Claim>>> getClaimsByRelatedItem(
            @PathVariable String relatedTo,
            @PathVariable Long relatedId) {
        List<Claim> claims = service.getClaimsByRelatedItem(relatedTo, relatedId);

        ApiResponse<List<Claim>> response = ApiResponse.success(
                "Liste des réclamations récupérée avec succès",
                claims
        );
        return ResponseEntity.ok(response);
    }

    // Récupérer mes réclamations signalées
    @GetMapping("/my-reported")
    @Operation(summary = "Mes réclamations", description = "Récupérer les réclamations signalées par l’utilisateur")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<ApiResponse<List<Claim>>> getMyReportedClaims() {
        List<Claim> claims = service.getClaimsByReportedBy(getCurrentUserEmail());

        ApiResponse<List<Claim>> response = ApiResponse.success(
                "Liste de vos réclamations récupérée avec succès",
                claims
        );
        return ResponseEntity.ok(response);
    }

    // Récupérer les réclamations qui me sont assignées
    @GetMapping("/my-assigned")
    @Operation(summary = "Mes tâches", description = "Récupérer les réclamations assignées à l’utilisateur")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<ApiResponse<List<Claim>>> getMyAssignedClaims() {
        List<Claim> claims = service.getClaimsByAssignedTo(getCurrentUserEmail());

        ApiResponse<List<Claim>> response = ApiResponse.success(
                "Liste de vos réclamations assignées récupérée avec succès",
                claims
        );
        return ResponseEntity.ok(response);
    }

    // Récupérer les réclamations par statut
    @GetMapping("/status/{status}")
    @Operation(summary = "Filtrer par statut", description = "Lister les réclamations par statut")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<ApiResponse<List<Claim>>> getClaimsByStatus(
            @PathVariable Claim.ClaimStatus status) {
        List<Claim> claims = service.getClaimsByStatus(status);

        ApiResponse<List<Claim>> response = ApiResponse.success(
                "Liste des réclamations filtrée par statut récupérée avec succès",
                claims
        );
        return ResponseEntity.ok(response);
    }

    // Récupérer les réclamations par priorité
    @GetMapping("/priority/{priority}")
    @Operation(summary = "Filtrer par priorité", description = "Lister les réclamations par priorité")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<ApiResponse<List<Claim>>> getClaimsByPriority(
            @PathVariable Claim.Priority priority) {
        List<Claim> claims = service.getClaimsByPriority(priority);

        ApiResponse<List<Claim>> response = ApiResponse.success(
                "Liste des réclamations filtrée par priorité récupérée avec succès",
                claims
        );
        return ResponseEntity.ok(response);
    }

    // Récupérer les réclamations par catégorie
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<ApiResponse<List<Claim>>> getClaimsByCategory(
            @PathVariable String category) {
        List<Claim> claims = service.getClaimsByCategory(category);

        ApiResponse<List<Claim>> response = ApiResponse.success(
                "Liste des réclamations filtrée par catégorie récupérée avec succès",
                claims
        );
        return ResponseEntity.ok(response);
    }
    // Rechercher des réclamations
    @GetMapping("/search")
    @Operation(summary = "Recherche", description = "Rechercher des réclamations par mot-clé")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<ApiResponse<List<Claim>>> searchClaims(@RequestParam String keyword) {
        List<Claim> claims = service.searchClaims(keyword);

        ApiResponse<List<Claim>> response = ApiResponse.success(
                "Résultats de recherche récupérés avec succès",
                claims
        );
        return ResponseEntity.ok(response);
    }

    // Récupérer toutes les réclamations (admin)
    @GetMapping
    @Operation(summary = "Lister toutes les réclamations", description = "Afficher toutes les réclamations")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<ApiResponse<List<Claim>>> getAllClaims() {
        List<Claim> claims = service.getAllClaims();

        ApiResponse<List<Claim>> response = ApiResponse.success(
                "Liste de toutes les réclamations récupérée avec succès",
                claims
        );
        return ResponseEntity.ok(response);
    }
    // Méthode utilitaire pour obtenir l'email de l'utilisateur courant
    private String getCurrentUserEmail() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    @GetMapping("/summary/{chargeSheetId}")
    @Operation(summary = "Statistiques", description = "Obtenir un résumé des réclamations par statut")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getClaimSummary(@PathVariable Long chargeSheetId) {
        List<Claim> claims = repository.findByChargeSheetId(chargeSheetId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("total", claims.size());
        summary.put("new", claims.stream().filter(c -> c.getStatus() == Claim.ClaimStatus.NEW).count());
        summary.put("inProgress", claims.stream().filter(c -> c.getStatus() == Claim.ClaimStatus.IN_PROGRESS).count());
        summary.put("resolved", claims.stream().filter(c -> c.getStatus() == Claim.ClaimStatus.RESOLVED).count());
        summary.put("closed", claims.stream().filter(c -> c.getStatus() == Claim.ClaimStatus.CLOSED).count());

        ApiResponse<Map<String, Object>> response = ApiResponse.success(
                "Statistiques des réclamations récupérées avec succès",
                summary
        );
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une réclamation", description = "Supprimer une réclamation")
    @PreAuthorize("hasAuthority('claim:write')")
    public ResponseEntity<ApiResponse<Void>> deleteClaim(@PathVariable Long id) {
        service.deleteClaim(id);

        ApiResponse<Void> response = ApiResponse.success("Réclamation supprimée avec succès");
        return ResponseEntity.ok(response);
    }
    private final ImageStorageService imageStorageService;
    private static final String CLAIM_IMAGE_FOLDER = "claims";

    @PostMapping("/{id}/upload-image")
    @Operation(summary = "Uploader image", description = "Ajouter une image à une réclamation")
    @PreAuthorize("hasAuthority('claim:write')")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        try {
            Claim claim = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            String imagePath = imageStorageService.saveImage(file, CLAIM_IMAGE_FOLDER);
            claim.setImagePath(imagePath);
            repository.save(claim);

            Map<String, String> data = new HashMap<>();
            data.put("filename", imagePath.substring(imagePath.lastIndexOf("/") + 1));
            data.put("path", imagePath);

            ApiResponse<Map<String, String>> response = ApiResponse.success(
                    "Image uploadée avec succès",
                    data
            );
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            ApiResponse<Map<String, String>> response = ApiResponse.error(
                    "Failed to upload image: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}/image")
    @Operation(summary = "Afficher image", description = "Afficher l’image d’une réclamation")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        try {
            Claim claim = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            String imagePath = claim.getImagePath();
            if (imagePath == null || imagePath.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            byte[] imageData = imageStorageService.getImage(imagePath);

            String contentType = "image/jpeg";
            if (imagePath.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (imagePath.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            } else if (imagePath.toLowerCase().endsWith(".webp")) {
                contentType = "image/webp";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(imageData.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageData);

        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/image")
    @Operation(summary = "Supprimer image", description = "Supprimer l’image associée à une réclamation")
    @PreAuthorize("hasAuthority('claim:write')")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteImage(@PathVariable Long id) {
        try {
            Claim claim = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            String imagePath = claim.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                imageStorageService.deleteImage(imagePath);
                claim.setImagePath(null);
                repository.save(claim);
            }

            ApiResponse<Map<String, String>> response = ApiResponse.success("Image supprimée avec succès");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            ApiResponse<Map<String, String>> response = ApiResponse.error(
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
// Dans ClaimController.java - Ajoutez ces méthodes

    @GetMapping("/stats/last-two-months")
    @Operation(summary = "Variation deux derniers mois des réclamations",
            description = "Calcule automatiquement la variation entre les deux derniers mois disponibles")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLastTwoMonthsVariation(
            @RequestParam(required = false) String project) {

        Map<String, Object> variation = service.getLastTwoMonthsVariation(project);

        ApiResponse<Map<String, Object>> response = ApiResponse.success(
                "Variation calculée avec succès",
                variation
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/monthly-variation")
    @Operation(summary = "Variation mensuelle des réclamations",
            description = "Calcule la variation entre deux mois spécifiques")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyVariation(
            @RequestParam String month1,
            @RequestParam String month2,
            @RequestParam(required = false) String project) {

        Map<String, Object> variation = service.getVariationBetweenMonths(project, month1, month2);

        ApiResponse<Map<String, Object>> response = ApiResponse.success(
                "Variation mensuelle calculée avec succès",
                variation
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/debug-all-images")
    public ResponseEntity<ApiResponse<Map<String, Object>>> debugAllImages() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> claimsWithImages = new ArrayList<>();

        List<Claim> allClaims = repository.findAll();
        for (Claim claim : allClaims) {
            Map<String, Object> claimInfo = new HashMap<>();
            claimInfo.put("id", claim.getId());
            claimInfo.put("title", claim.getTitle());
            claimInfo.put("imagePath", claim.getImagePath());

            if (claim.getImagePath() != null && !claim.getImagePath().isEmpty()) {
                Path filePath = Paths.get("uploads", claim.getImagePath());
                claimInfo.put("fullPath", filePath.toAbsolutePath().toString());
                claimInfo.put("fileExists", Files.exists(filePath));
            }

            claimsWithImages.add(claimInfo);
        }

        result.put("claimsWithImages", claimsWithImages);

        Path claimsDir = Paths.get("uploads/claims");
        List<String> files = new ArrayList<>();
        if (Files.exists(claimsDir) && Files.isDirectory(claimsDir)) {
            try {
                Files.list(claimsDir).forEach(p -> files.add(p.getFileName().toString()));
            } catch (IOException e) {
                files.add("Erreur de lecture: " + e.getMessage());
            }
        } else {
            files.add("Dossier non trouvé: " + claimsDir.toAbsolutePath().toString());
        }
        result.put("filesInUploadsClaims", files);
        result.put("uploadsAbsolutePath", Paths.get("uploads").toAbsolutePath().toString());
        result.put("uploadsExists", Files.exists(Paths.get("uploads")));

        ApiResponse<Map<String, Object>> response = ApiResponse.success(
                "Debug des images récupéré avec succès",
                result
        );
        return ResponseEntity.ok(response);
    }

}
