package com.example.security.conformité;

import com.example.security.cahierdeCharge.ChargeSheetItem;
import com.example.security.cahierdeCharge.ChargeSheetItemRepository;
import com.example.security.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/compliance")
@RequiredArgsConstructor
@Tag(name = "Conformité", description = "Gestion des fiches de conformité des items")
public class ComplianceController {

    private final ComplianceService service;
    private final ChargeSheetItemRepository chargeSheetItemRepository;
    private final CompliancePreparationService compliancePreparationService;
    private final ComplianceRepository complianceRepository;

    // PP: Créer une conformité
    @PostMapping
    @Operation(
            summary = "Créer une conformité",
            description = "Créer une fiche de conformité pour un item"
    )
    @PreAuthorize("hasAuthority('compliance:create')")
    public ResponseEntity<Compliance> createCompliance(@RequestBody ComplianceDto.CreateDto dto) {
        return ResponseEntity.ok(service.createCompliance(dto));
    }

    // PP: Mettre à jour une conformité
    @PutMapping("/{id}")
    @Operation(
            summary = "Modifier une conformité",
            description = "Mettre à jour les informations d’une fiche de conformité"
    )
    @PreAuthorize("hasAuthority('compliance:write')")
    public ResponseEntity<Compliance> updateCompliance(
            @PathVariable Long id,
            @RequestBody ComplianceDto.UpdateDto dto) {
        return ResponseEntity.ok(service.updateCompliance(id, dto));
    }

    // PP, MC, MP: Lire une conformité
    @GetMapping("/{id}")
    @Operation(
            summary = "Consulter une conformité",
            description = "Récupérer une fiche de conformité par son ID"
    )
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<Compliance> getCompliance(@PathVariable Long id) {
        return ResponseEntity.ok(service.getComplianceById(id));
    }

    // Tous: Lister les conformités par cahier des charges
    @GetMapping("/charge-sheet/{chargeSheetId}")
    @Operation(
            summary = "Conformités par cahier",
            description = "Lister toutes les conformités associées à un cahier de charges"
    )
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<List<Compliance>> getComplianceByChargeSheet(
            @PathVariable Long chargeSheetId) {
        return ResponseEntity.ok(service.getComplianceByChargeSheetId(chargeSheetId));
    }

    // Tous: Lister les conformités par item
    @GetMapping("/item/{itemId}")
    @Operation(
            summary = "Conformités par item",
            description = "Lister toutes les conformités associées à un item"
    )
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<List<Compliance>> getComplianceByItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(complianceRepository.findByItemId(itemId));
    }

    // Admin: Lister toutes les conformités
    @GetMapping
    @Operation(
            summary = "Lister toutes les conformités",
            description = "Afficher toutes les fiches de conformité"
    )
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<List<Compliance>> getAllCompliance() {
        return ResponseEntity.ok(service.getAllCompliance());
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Supprimer une conformité",
            description = "Supprimer une fiche de conformité"
    )
    @PreAuthorize("hasAuthority('compliance:write')")
    public ResponseEntity<Void> deleteCompliance(@PathVariable Long id) {
        service.deleteCompliance(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create-for-item/{itemId}")
    @Operation(
            summary = "Créer conformités pour item",
            description = "Créer automatiquement plusieurs fiches de conformité pour un item"
    )
    @PreAuthorize("hasAuthority('compliance:create')")
    public ResponseEntity<List<Compliance>> createForItem(@PathVariable Long itemId) {
        ChargeSheetItem item = chargeSheetItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        List<Compliance> created = service.createComplianceForItem(item);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/display")
    @Operation(
            summary = "Affichage enrichi",
            description = "Afficher les conformités avec des informations enrichies pour le frontend"
    )
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<List<ComplianceDisplayDto>> getAllComplianceForDisplay() {
        return ResponseEntity.ok(service.getAllComplianceForDisplay());
    }

    /**
     * Préparer les données pour la création de fiches de conformité
     * Renvoie le nombre de fiches à créer basé sur les quantités reçues
     */
    @GetMapping("/prepare/{itemId}")
    @Operation(
            summary = "Préparer conformité",
            description = "Préparer les fiches de conformité selon les quantités reçues"
    )
    @PreAuthorize("hasAuthority('compliance:create')")
    public ResponseEntity<List<ComplianceDto.PrepareComplianceDto>> prepareComplianceForItem(
            @PathVariable Long itemId) {
        return ResponseEntity.ok(compliancePreparationService.prepareComplianceForItem(itemId));
    }

    /**
     * Créer les fiches de conformité pour les quantités reçues
     */
    @PostMapping("/create-for-received/{itemId}")
    @Operation(
            summary = "Créer conformités reçues",
            description = "Créer des fiches de conformité en fonction des quantités reçues"
    )
    @PreAuthorize("hasAuthority('compliance:create')")
    public ResponseEntity<List<Compliance>> createComplianceForReceivedQuantity(
            @PathVariable Long itemId,
            @RequestParam int numberOfSheets) {
        return ResponseEntity.ok(compliancePreparationService.createComplianceForReceivedQuantity(itemId, numberOfSheets));
    }
// Dans ComplianceController.java - Ajoutez ces méthodes

    @GetMapping("/stats/monthly-variation")
    @Operation(summary = "Variation mensuelle des conformités",
            description = "Calcule la variation entre deux mois spécifiques")
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<Map<String, Object>> getMonthlyVariation(
            @RequestParam String month1,
            @RequestParam String month2,
            @RequestParam(required = false) String project) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userProject = (project != null && !project.isEmpty()) ? project : null;

        Map<String, Object> variation = service.getVariationBetweenMonths(userProject, month1, month2);
        return ResponseEntity.ok(variation);
    }

    @GetMapping("/stats/last-two-months")
    @Operation(summary = "Variation deux derniers mois des conformités",
            description = "Calcule automatiquement la variation entre les deux derniers mois disponibles")
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<Map<String, Object>> getLastTwoMonthsVariation(
            @RequestParam(required = false) String project) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userProject = (project != null && !project.isEmpty()) ? project : null;

        Map<String, Object> variation = service.getLastTwoMonthsVariation(userProject);
        return ResponseEntity.ok(variation);
    }

    @GetMapping("/stats/monthly-stats")
    @Operation(summary = "Statistiques mensuelles des conformités",
            description = "Nombre de fiches de conformité créées par mois")
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<MonthlyComplianceStatsDto> getMonthlyComplianceStats(
            @RequestParam(required = false) String project,
            @RequestParam(defaultValue = "6") int months) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userProject = (project != null && !project.isEmpty()) ? project : null;

        MonthlyComplianceStatsDto stats = service.getMonthlyComplianceStats(userProject, months);
        return ResponseEntity.ok(stats);
    }

}