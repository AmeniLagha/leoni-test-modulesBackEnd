package com.example.security.reclamation;

import com.example.security.cahierdeCharge.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public ResponseEntity<Claim> createClaim(@RequestBody ClaimDto.CreateDto dto) {
        return ResponseEntity.ok(service.createClaim(dto));
    }

    // Mettre à jour une réclamation
    @PutMapping("/{id}")
    @Operation(summary = "Modifier une réclamation", description = "Mettre à jour une réclamation existante")
    @PreAuthorize("hasAuthority('claim:write')")
    public ResponseEntity<Claim> updateClaim(
            @PathVariable Long id,
            @RequestBody ClaimDto.UpdateDto dto) {
        return ResponseEntity.ok(service.updateClaim(id, dto));
    }

    // Assigner une réclamation
    @PutMapping("/{id}/assign")
    @Operation(summary = "Assigner une réclamation", description = "Assigner une réclamation à un utilisateur")
    @PreAuthorize("hasAuthority('claim:write')")
    public ResponseEntity<Claim> assignClaim(
            @PathVariable Long id,
            @RequestBody ClaimDto.AssignmentDto dto) {
        return ResponseEntity.ok(service.assignClaim(id, dto));
    }

    // Résoudre une réclamation
    @PutMapping("/{id}/resolve")
    @Operation(summary = "Résoudre une réclamation", description = "Marquer une réclamation comme résolue")
    @PreAuthorize("hasAuthority('claim:write')")
    public ResponseEntity<Claim> resolveClaim(
            @PathVariable Long id,
            @RequestBody ClaimDto.ResolutionDto dto) {
        return ResponseEntity.ok(service.resolveClaim(id, dto));
    }

    // Changer le statut d'une réclamation
    @PatchMapping("/{id}/status/{status}")
    @Operation(summary = "Changer statut", description = "Mettre à jour le statut d’une réclamation")
    @PreAuthorize("hasAuthority('claim:write')")
    public ResponseEntity<Claim> updateClaimStatus(
            @PathVariable Long id,
            @PathVariable Claim.ClaimStatus status) {
        return ResponseEntity.ok(service.updateClaimStatus(id, status));
    }

    // Lire une réclamation spécifique
    @GetMapping("/{id}")
    @Operation(summary = "Consulter une réclamation", description = "Afficher une réclamation par ID")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<Claim> getClaim(@PathVariable Long id) {
        return ResponseEntity.ok(service.getClaimById(id));
    }

    // Récupérer les réclamations par cahier des charges
    @GetMapping("/charge-sheet/{chargeSheetId}")
    @Operation(summary = "Réclamations par cahier", description = "Lister les réclamations liées à un cahier")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<List<Claim>> getClaimsByChargeSheet(
            @PathVariable Long chargeSheetId) {
        return ResponseEntity.ok(service.getClaimsByChargeSheetId(chargeSheetId));
    }

    // Récupérer les réclamations par élément lié
    @GetMapping("/related/{relatedTo}/{relatedId}")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<List<Claim>> getClaimsByRelatedItem(
            @PathVariable String relatedTo,
            @PathVariable Long relatedId) {
        return ResponseEntity.ok(service.getClaimsByRelatedItem(relatedTo, relatedId));
    }

    // Récupérer mes réclamations signalées
    @GetMapping("/my-reported")
    @Operation(summary = "Mes réclamations", description = "Récupérer les réclamations signalées par l’utilisateur")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<List<Claim>> getMyReportedClaims() {
        return ResponseEntity.ok(service.getClaimsByReportedBy(getCurrentUserEmail()));
    }

    // Récupérer les réclamations qui me sont assignées
    @GetMapping("/my-assigned")
    @Operation(summary = "Mes tâches", description = "Récupérer les réclamations assignées à l’utilisateur")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<List<Claim>> getMyAssignedClaims() {
        return ResponseEntity.ok(service.getClaimsByAssignedTo(getCurrentUserEmail()));
    }

    // Récupérer les réclamations par statut
    @GetMapping("/status/{status}")
    @Operation(summary = "Filtrer par statut", description = "Lister les réclamations par statut")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<List<Claim>> getClaimsByStatus(
            @PathVariable Claim.ClaimStatus status) {
        return ResponseEntity.ok(service.getClaimsByStatus(status));
    }

    // Récupérer les réclamations par priorité
    @GetMapping("/priority/{priority}")
    @Operation(summary = "Filtrer par priorité", description = "Lister les réclamations par priorité")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<List<Claim>> getClaimsByPriority(
            @PathVariable Claim.Priority priority) {
        return ResponseEntity.ok(service.getClaimsByPriority(priority));
    }

    // Récupérer les réclamations par catégorie
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<List<Claim>> getClaimsByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(service.getClaimsByCategory(category));
    }

    // Rechercher des réclamations
    @GetMapping("/search")
    @Operation(summary = "Recherche", description = "Rechercher des réclamations par mot-clé")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<List<Claim>> searchClaims(@RequestParam String keyword) {
        return ResponseEntity.ok(service.searchClaims(keyword));
    }

    // Récupérer toutes les réclamations (admin)
    @GetMapping
    @Operation(summary = "Lister toutes les réclamations", description = "Afficher toutes les réclamations")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<List<Claim>> getAllClaims() {
        return ResponseEntity.ok(service.getAllClaims());
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
    public ResponseEntity<Map<String, Object>> getClaimSummary(@PathVariable Long chargeSheetId) {
        List<Claim> claims = repository.findByChargeSheetId(chargeSheetId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("total", claims.size());
        summary.put("new", claims.stream().filter(c -> c.getStatus() == Claim.ClaimStatus.NEW).count());
        summary.put("inProgress", claims.stream().filter(c -> c.getStatus() == Claim.ClaimStatus.IN_PROGRESS).count());
        summary.put("resolved", claims.stream().filter(c -> c.getStatus() == Claim.ClaimStatus.RESOLVED).count());
        summary.put("closed", claims.stream().filter(c -> c.getStatus() == Claim.ClaimStatus.CLOSED).count());

        return ResponseEntity.ok(summary);
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une réclamation", description = "Supprimer une réclamation")
    @PreAuthorize("hasAuthority('claim:write')")
    public ResponseEntity<Void> deleteClaim(@PathVariable Long id) {
        service.deleteClaim(id);
        return ResponseEntity.noContent().build();
    }
    private final ImageStorageService imageStorageService;
    private static final String CLAIM_IMAGE_FOLDER = "claims";

    @PostMapping("/{id}/upload-image")
    @Operation(summary = "Uploader image", description = "Ajouter une image à une réclamation")
    @PreAuthorize("hasAuthority('claim:write')")
    public ResponseEntity<Map<String, String>> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        try {
            // Récupérer la réclamation
            Claim claim = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            // Sauvegarder l'image dans le dossier "claims"
            String imagePath = imageStorageService.saveImage(file, CLAIM_IMAGE_FOLDER);

            // Mettre à jour le chemin dans la base de données
            claim.setImagePath(imagePath);
            repository.save(claim);

            Map<String, String> response = new HashMap<>();
            response.put("filename", imagePath.substring(imagePath.lastIndexOf("/") + 1));
            response.put("path", imagePath);
            response.put("message", "Image uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
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

            // Déterminer le type MIME
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
    public ResponseEntity<Map<String, String>> deleteImage(@PathVariable Long id) {
        try {
            Claim claim = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            String imagePath = claim.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                imageStorageService.deleteImage(imagePath);
                claim.setImagePath(null);
                repository.save(claim);
            }

            return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

}
