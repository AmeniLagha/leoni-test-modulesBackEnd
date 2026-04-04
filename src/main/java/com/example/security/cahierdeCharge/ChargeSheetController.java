package com.example.security.cahierdeCharge;

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
@Tag(name = "Cahier de charges", description = "Gestion des cahiers de charges")
@RequestMapping("/api/v1/charge-sheets")
@RequiredArgsConstructor
public class ChargeSheetController {

    private final ChargeSheetService service;

    // ING: Créer avec plusieurs items
    @PostMapping
    @Operation(
            summary = "Créer un cahier de charges",
           description = "Créer un nouveau cahier de charges avec plusieurs items"

    )
    @PreAuthorize("hasAuthority('charge_sheet:basic:create')")
    public ResponseEntity<ChargeSheetDto.CompleteDto> createChargeSheet(@RequestBody ChargeSheetDto.CreateDto dto) {
        ChargeSheet created = service.createChargeSheet(dto);
        return ResponseEntity.ok(service.getChargeSheetComplete(created.getId()));
    }

    // PT: Mettre à jour un item spécifique
    @PutMapping("/{sheetId}/items/{itemId}/tech")
    @Operation(summary = "Modifier un item technique", description = "Mettre à jour les informations techniques d’un item spécifique")
    @PreAuthorize("hasAuthority('charge_sheet:tech:write')")
    public ResponseEntity<ChargeSheetDto.ItemDto> updateItemTech(
            @PathVariable Long sheetId,
            @PathVariable Long itemId,
            @RequestBody ChargeSheetDto.UpdateTechDto dto) {
        // Ne pas appeler setItemId - ce n'est plus dans le DTO
        ChargeSheetItem updated = service.updateTechnicalFields(sheetId, itemId, dto);
        return ResponseEntity.ok(service.mapToItemDtoPublic(updated)); // Utiliser la méthode publique
    }

    // ING: Ajouter un nouvel item
    @PostMapping("/{sheetId}/items")
    @Operation(summary = "Ajouter un item", description = "Ajouter un nouvel item dans un cahier de charges")
    @PreAuthorize("hasAuthority('charge_sheet:basic:write')")
    public ResponseEntity<ChargeSheetDto.CompleteDto> addItem(
            @PathVariable Long sheetId,
            @RequestBody ChargeSheetDto.ItemDto itemDto) {
        ChargeSheet updated = service.addItem(sheetId, itemDto);
        return ResponseEntity.ok(service.getChargeSheetComplete(updated.getId()));
    }

    // ING: Supprimer un item
    @DeleteMapping("/{sheetId}/items/{itemId}")
    @Operation(summary = "Supprimer un item", description = "Supprimer un item d’un cahier de charges")
    @PreAuthorize("hasAuthority('charge_sheet:basic:write')")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long sheetId,
            @PathVariable Long itemId) {
        service.removeItem(sheetId, itemId);
        return ResponseEntity.noContent().build();
    }

    // Tous les rôles: Lire un cahier des charges complet
    @GetMapping("/{id}")
    @Operation(summary = "Consulter un cahier", description = "Récupérer les détails complets d’un cahier de charges")
    @PreAuthorize("hasAuthority('charge_sheet:all:read')")
    public ResponseEntity<ChargeSheetDto.CompleteDto> getChargeSheet(@PathVariable Long id) {
        return ResponseEntity.ok(service.getChargeSheetComplete(id));
    }

    // Tous les rôles: Lister tous les cahiers des charges
    @GetMapping
    @Operation(summary = "Lister les cahiers", description = "Afficher tous les cahiers de charges")
    @PreAuthorize("hasAuthority('charge_sheet:all:read')")
    public ResponseEntity<List<ChargeSheetDto.CompleteDto>> getAllChargeSheets() {
        return ResponseEntity.ok(service.getAllChargeSheets());
    }
    // ADMIN: Supprimer un cahier des charges complet
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un cahier", description = "Supprimer complètement un cahier de charges (ADMIN uniquement)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteChargeSheet(@PathVariable Long id) {
        service.deleteChargeSheet(id);
        return ResponseEntity.noContent().build();
    }
    // ING: Valider le cahier
    @PutMapping("/{id}/validate-ing")
    @Operation(summary = "Validation ingénieur", description = "Valider le cahier par l’ingénieur")
    @PreAuthorize("hasAuthority('charge_sheet:basic:write')")
    public ResponseEntity<ChargeSheetDto.CompleteDto> validateByIng(@PathVariable Long id) {
        ChargeSheet validated = service.validateByIng(id);
        return ResponseEntity.ok(service.getChargeSheetComplete(validated.getId()));
    }

    // PT: Valider le cahier
    @PutMapping("/{id}/validate-pt")
    @Operation(summary = "Validation technique", description = "Valider le cahier par le responsable technique")
    @PreAuthorize("hasAuthority('charge_sheet:tech:write')")
    public ResponseEntity<ChargeSheetDto.CompleteDto> validateByPt(@PathVariable Long id) {
        ChargeSheet validated = service.validateByPt(id);
        return ResponseEntity.ok(service.getChargeSheetComplete(validated.getId()));
    }
    // Statistiques pour le dashboard
    @GetMapping("/stats")
    @Operation(summary = "Statistiques", description = "Récupérer les statistiques pour le dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(service.getDashboardStats());
    }
    // Envoyer au fournisseur
    @PutMapping("/{id}/send-supplier")
    @Operation(summary = "Envoyer au fournisseur", description = "Envoyer le cahier au fournisseur")
    public ResponseEntity<ChargeSheetDto.CompleteDto> sendToSupplier(@PathVariable Long id) {
        ChargeSheet updated = service.sendToSupplier(id);
        return ResponseEntity.ok(service.getChargeSheetComplete(updated.getId()));
    }

    // Valider la réception
    @PutMapping("/{id}/confirm-reception")
    @Operation(summary = "Confirmer réception", description = "Confirmer la réception du cahier")
    public ResponseEntity<ChargeSheetDto.CompleteDto> confirmReception(@PathVariable Long id) {
        ChargeSheet updated = service.confirmReception(id);
        return ResponseEntity.ok(service.getChargeSheetComplete(updated.getId()));
    }
// Ajouter cette méthode dans ChargeSheetController.java

    // ING: Mettre à jour les informations générales du cahier (seulement si DRAFT)
    @PutMapping("/{id}")
    @Operation(summary = "Modifier un cahier", description = "Mettre à jour les informations générales d’un cahier (mode DRAFT uniquement)")
    @PreAuthorize("hasAuthority('charge_sheet:basic:write')")
    public ResponseEntity<ChargeSheetDto.CompleteDto> updateChargeSheet(
            @PathVariable Long id,
            @RequestBody ChargeSheetDto.UpdateGeneralDto dto) {
        ChargeSheet updated = service.updateChargeSheet(id, dto);
        return ResponseEntity.ok(service.getChargeSheetComplete(updated.getId()));
    }
    // Compléter
    @PutMapping("/{id}/complete")
    @Operation(summary = "Compléter le cahier", description = "Marquer le cahier comme complété")
    public ResponseEntity<ChargeSheetDto.CompleteDto> complete(@PathVariable Long id) {
        ChargeSheet updated = service.completeChargeSheet(id);
        return ResponseEntity.ok(service.getChargeSheetComplete(updated.getId()));
    }
    // ============ ENDPOINTS DE RÉCEPTION ============

    /**
     * Préparer les données pour la réception
     */
    @GetMapping("/{id}/prepare-reception")
    @Operation(summary = "Préparer réception", description = "Préparer les données nécessaires pour la réception")
    @PreAuthorize("hasAuthority('charge_sheet:tech:write')")
    public ResponseEntity<ReceptionDto.ReceptionResponseDto> prepareReception(@PathVariable Long id) {
        return ResponseEntity.ok(service.prepareReceptionData(id));
    }

    /**
     * Confirmer une réception partielle
     */
    @PostMapping("/{id}/confirm-partial-reception")
    @Operation(summary = "Réception partielle", description = "Confirmer une réception partielle des items")
    @PreAuthorize("hasAuthority('charge_sheet:tech:write')")
    public ResponseEntity<ReceptionDto.ReceptionResponseDto> confirmPartialReception(
            @PathVariable Long id,
            @RequestBody ReceptionDto.ReceptionRequestDto request) {
        request.setChargeSheetId(id);
        return ResponseEntity.ok(service.confirmPartialReception(request));
    }

    /**
     * Obtenir l'historique des réceptions
     */
    @GetMapping("/{id}/reception-history")
    @Operation(summary = "Historique réception", description = "Afficher l’historique des réceptions d’un cahier")
    @PreAuthorize("hasAuthority('charge_sheet:all:read')")
    public ResponseEntity<List<ReceptionHistoryDto>> getReceptionHistory(@PathVariable Long id) {
        return ResponseEntity.ok(service.getReceptionHistoryDto(id));
    }
// Dans ChargeSheetController.java - Ajoutez ces méthodes

    @GetMapping("/stats/monthly-variation")
    @Operation(summary = "Variation mensuelle", description = "Calcule la variation entre deux mois spécifiques")
    @PreAuthorize("hasAuthority('charge_sheet:all:read')")
    public ResponseEntity<Map<String, Object>> getMonthlyVariation(
            @RequestParam String month1,
            @RequestParam String month2,
            @RequestParam(required = false) String project) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userProject = (project != null && !project.isEmpty()) ? project : currentUser.getProjet();

        Map<String, Object> variation = service.getVariationBetweenMonths(userProject, month1, month2);
        return ResponseEntity.ok(variation);
    }

    @GetMapping("/stats/monthly-creation")
    @Operation(summary = "Statistiques de création mensuelles",
            description = "Nombre de cahiers créés par mois avec variations")
    @PreAuthorize("hasAuthority('charge_sheet:all:read')")
    public ResponseEntity<MonthlyStatsDto> getMonthlyCreationStats(
            @RequestParam(required = false) String project,
            @RequestParam(defaultValue = "6") int months) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userProject = (project != null && !project.isEmpty()) ? project : currentUser.getProjet();

        MonthlyStatsDto stats = service.getMonthlyCreationStats(userProject, months);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/last-two-months")
    @Operation(summary = "Variation deux derniers mois",
            description = "Calcule automatiquement la variation entre les deux derniers mois disponibles")
    @PreAuthorize("hasAuthority('charge_sheet:all:read')")
    public ResponseEntity<Map<String, Object>> getLastTwoMonthsVariation(
            @RequestParam(required = false) String project) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userProject = (project != null && !project.isEmpty()) ? project : currentUser.getProjet();

        Map<String, Object> variation = service.getLastTwoMonthsVariation(userProject);
        return ResponseEntity.ok(variation);
    }
}