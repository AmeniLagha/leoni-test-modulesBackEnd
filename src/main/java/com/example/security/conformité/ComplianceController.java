package com.example.security.conformité;

import com.example.security.cahierdeCharge.ChargeSheetItem;
import com.example.security.cahierdeCharge.ChargeSheetItemRepository;
import com.example.security.common.ApiResponse;
import com.example.security.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    private final JavaMailSender mailSender;


    // PP: Créer une conformité
    @PostMapping
    @Operation(
            summary = "Créer une conformité",
            description = "Créer une fiche de conformité pour un item"
    )
    @PreAuthorize("hasAuthority('compliance:create')")
    public ResponseEntity<ApiResponse<Compliance>> createCompliance(@RequestBody ComplianceDto.CreateDto dto) {
        Compliance created = service.createCompliance(dto);

        ApiResponse<Compliance> response = ApiResponse.success(
                "Fiche de conformité créée avec succès",
                created,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PP: Mettre à jour une conformité
    @PutMapping("/{id}")
    @Operation(
            summary = "Modifier une conformité",
            description = "Mettre à jour les informations d’une fiche de conformité"
    )
    @PreAuthorize("hasAuthority('compliance:write')")
    public ResponseEntity<ApiResponse<Compliance>> updateCompliance(
            @PathVariable Long id,
            @RequestBody ComplianceDto.UpdateDto dto) {
        Compliance updated = service.updateCompliance(id, dto);

        ApiResponse<Compliance> response = ApiResponse.success(
                "Fiche de conformité modifiée avec succès",
                updated
        );
        return ResponseEntity.ok(response);
    }

    // PP, MC, MP: Lire une conformité
    @GetMapping("/{id}")
    @Operation(
            summary = "Consulter une conformité",
            description = "Récupérer une fiche de conformité par son ID"
    )
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<ApiResponse<Compliance>> getCompliance(@PathVariable Long id) {
        Compliance compliance = service.getComplianceById(id);

        ApiResponse<Compliance> response = ApiResponse.success(
                "Fiche de conformité récupérée avec succès",
                compliance
        );
        return ResponseEntity.ok(response);
    }

    // Tous: Lister les conformités par cahier des charges
    @GetMapping("/charge-sheet/{chargeSheetId}")
    @Operation(
            summary = "Conformités par cahier",
            description = "Lister toutes les conformités associées à un cahier de charges"
    )
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<ApiResponse<List<Compliance>>> getComplianceByChargeSheet(
            @PathVariable Long chargeSheetId) {
        List<Compliance> compliances = service.getComplianceByChargeSheetId(chargeSheetId);

        ApiResponse<List<Compliance>> response = ApiResponse.success(
                "Liste des conformités récupérée avec succès",
                compliances
        );
        return ResponseEntity.ok(response);
    }

    // Tous: Lister les conformités par item
    @GetMapping("/item/{itemId}")
    @Operation(
            summary = "Conformités par item",
            description = "Lister toutes les conformités associées à un item"
    )
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<ApiResponse<List<Compliance>>> getComplianceByItem(@PathVariable Long itemId) {
        List<Compliance> compliances = complianceRepository.findByItemId(itemId);

        ApiResponse<List<Compliance>> response = ApiResponse.success(
                "Liste des conformités récupérée avec succès",
                compliances
        );
        return ResponseEntity.ok(response);
    }


    // Admin: Lister toutes les conformités
    @GetMapping
    @Operation(
            summary = "Lister toutes les conformités",
            description = "Afficher toutes les fiches de conformité"
    )
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<ApiResponse<List<Compliance>>> getAllCompliance() {
        List<Compliance> compliances = service.getAllCompliance();

        ApiResponse<List<Compliance>> response = ApiResponse.success(
                "Liste de toutes les conformités récupérée avec succès",
                compliances
        );
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Supprimer une conformité",
            description = "Supprimer une fiche de conformité"
    )
    @PreAuthorize("hasAuthority('compliance:write')")
    public ResponseEntity<ApiResponse<Void>> deleteCompliance(@PathVariable Long id) {
        service.deleteCompliance(id);

        ApiResponse<Void> response = ApiResponse.success("Fiche de conformité supprimée avec succès");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-for-item/{itemId}")
    @Operation(
            summary = "Créer conformités pour item",
            description = "Créer automatiquement plusieurs fiches de conformité pour un item"
    )
    @PreAuthorize("hasAuthority('compliance:create')")
    public ResponseEntity<ApiResponse<List<Compliance>>> createForItem(@PathVariable Long itemId) {
        ChargeSheetItem item = chargeSheetItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        List<Compliance> created = service.createComplianceForItem(item);

        ApiResponse<List<Compliance>> response = ApiResponse.success(
                "Fiches de conformité créées avec succès",
                created,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/display")
    @Operation(
            summary = "Affichage enrichi",
            description = "Afficher les conformités avec des informations enrichies pour le frontend"
    )
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<ApiResponse<List<ComplianceDisplayDto>>> getAllComplianceForDisplay() {
        List<ComplianceDisplayDto> compliances = service.getAllComplianceForDisplay();

        ApiResponse<List<ComplianceDisplayDto>> response = ApiResponse.success(
                "Affichage enrichi des conformités récupéré avec succès",
                compliances
        );
        return ResponseEntity.ok(response);
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
    public ResponseEntity<ApiResponse<List<ComplianceDto.PrepareComplianceDto>>> prepareComplianceForItem(
            @PathVariable Long itemId) {
        List<ComplianceDto.PrepareComplianceDto> result = compliancePreparationService.prepareComplianceForItem(itemId);

        ApiResponse<List<ComplianceDto.PrepareComplianceDto>> response = ApiResponse.success(
                "Préparation des fiches de conformité réussie",
                result
        );
        return ResponseEntity.ok(response);
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
    public ResponseEntity<ApiResponse<List<Compliance>>> createComplianceForReceivedQuantity(
            @PathVariable Long itemId,
            @RequestParam int numberOfSheets) {
        List<Compliance> created = compliancePreparationService.createComplianceForReceivedQuantity(itemId, numberOfSheets);

        ApiResponse<List<Compliance>> response = ApiResponse.success(
                "Fiches de conformité créées avec succès",
                created,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
// Dans ComplianceController.java - Ajoutez ces méthodes

    @GetMapping("/stats/monthly-variation")
    @Operation(summary = "Variation mensuelle des conformités",
            description = "Calcule la variation entre deux mois spécifiques")
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyVariation(
            @RequestParam String month1,
            @RequestParam String month2,
            @RequestParam(required = false) String project) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userProject = (project != null && !project.isEmpty()) ? project : null;

        Map<String, Object> variation = service.getVariationBetweenMonths(userProject, month1, month2);

        ApiResponse<Map<String, Object>> response = ApiResponse.success(
                "Variation mensuelle calculée avec succès",
                variation
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/last-two-months")
    @Operation(summary = "Variation deux derniers mois des conformités",
            description = "Calcule automatiquement la variation entre les deux derniers mois disponibles")
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLastTwoMonthsVariation(
            @RequestParam(required = false) String project) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userProject = (project != null && !project.isEmpty()) ? project : null;

        Map<String, Object> variation = service.getLastTwoMonthsVariation(userProject);

        ApiResponse<Map<String, Object>> response = ApiResponse.success(
                "Variation des deux derniers mois calculée avec succès",
                variation
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/monthly-stats")
    @Operation(summary = "Statistiques mensuelles des conformités",
            description = "Nombre de fiches de conformité créées par mois")
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<ApiResponse<MonthlyComplianceStatsDto>> getMonthlyComplianceStats(
            @RequestParam(required = false) String project,
            @RequestParam(defaultValue = "6") int months) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userProject = (project != null && !project.isEmpty()) ? project : null;

        MonthlyComplianceStatsDto stats = service.getMonthlyComplianceStats(userProject, months);

        ApiResponse<MonthlyComplianceStatsDto> response = ApiResponse.success(
                "Statistiques mensuelles récupérées avec succès",
                stats
        );
        return ResponseEntity.ok(response);
    }

}