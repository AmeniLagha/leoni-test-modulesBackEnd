package com.example.security.cahierdeCharge;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/charge-sheets")
@RequiredArgsConstructor
public class ChargeSheetController {

    private final ChargeSheetService service;

    // ING: Créer avec plusieurs items
    @PostMapping
    @PreAuthorize("hasAuthority('charge_sheet:basic:create')")
    public ResponseEntity<ChargeSheetDto.CompleteDto> createChargeSheet(@RequestBody ChargeSheetDto.CreateDto dto) {
        ChargeSheet created = service.createChargeSheet(dto);
        return ResponseEntity.ok(service.getChargeSheetComplete(created.getId()));
    }

    // PT: Mettre à jour un item spécifique
    @PutMapping("/{sheetId}/items/{itemId}/tech")
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
    @PreAuthorize("hasAuthority('charge_sheet:basic:write')")
    public ResponseEntity<ChargeSheetDto.CompleteDto> addItem(
            @PathVariable Long sheetId,
            @RequestBody ChargeSheetDto.ItemDto itemDto) {
        ChargeSheet updated = service.addItem(sheetId, itemDto);
        return ResponseEntity.ok(service.getChargeSheetComplete(updated.getId()));
    }

    // ING: Supprimer un item
    @DeleteMapping("/{sheetId}/items/{itemId}")
    @PreAuthorize("hasAuthority('charge_sheet:basic:write')")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long sheetId,
            @PathVariable Long itemId) {
        service.removeItem(sheetId, itemId);
        return ResponseEntity.noContent().build();
    }

    // Tous les rôles: Lire un cahier des charges complet
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('charge_sheet:all:read')")
    public ResponseEntity<ChargeSheetDto.CompleteDto> getChargeSheet(@PathVariable Long id) {
        return ResponseEntity.ok(service.getChargeSheetComplete(id));
    }

    // Tous les rôles: Lister tous les cahiers des charges
    @GetMapping
    @PreAuthorize("hasAuthority('charge_sheet:all:read')")
    public ResponseEntity<List<ChargeSheetDto.CompleteDto>> getAllChargeSheets() {
        return ResponseEntity.ok(service.getAllChargeSheets());
    }
    // ADMIN: Supprimer un cahier des charges complet
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteChargeSheet(@PathVariable Long id) {
        service.deleteChargeSheet(id);
        return ResponseEntity.noContent().build();
    }
    // ING: Valider le cahier
    @PutMapping("/{id}/validate-ing")
    @PreAuthorize("hasAuthority('charge_sheet:basic:write')")
    public ResponseEntity<ChargeSheetDto.CompleteDto> validateByIng(@PathVariable Long id) {
        ChargeSheet validated = service.validateByIng(id);
        return ResponseEntity.ok(service.getChargeSheetComplete(validated.getId()));
    }

    // PT: Valider le cahier
    @PutMapping("/{id}/validate-pt")
    @PreAuthorize("hasAuthority('charge_sheet:tech:write')")
    public ResponseEntity<ChargeSheetDto.CompleteDto> validateByPt(@PathVariable Long id) {
        ChargeSheet validated = service.validateByPt(id);
        return ResponseEntity.ok(service.getChargeSheetComplete(validated.getId()));
    }
    // Statistiques pour le dashboard
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(service.getDashboardStats());
    }
    // Envoyer au fournisseur
    @PutMapping("/{id}/send-supplier")
    public ResponseEntity<ChargeSheetDto.CompleteDto> sendToSupplier(@PathVariable Long id) {
        ChargeSheet updated = service.sendToSupplier(id);
        return ResponseEntity.ok(service.getChargeSheetComplete(updated.getId()));
    }

    // Valider la réception
    @PutMapping("/{id}/confirm-reception")
    public ResponseEntity<ChargeSheetDto.CompleteDto> confirmReception(@PathVariable Long id) {
        ChargeSheet updated = service.confirmReception(id);
        return ResponseEntity.ok(service.getChargeSheetComplete(updated.getId()));
    }
// Ajouter cette méthode dans ChargeSheetController.java

    // ING: Mettre à jour les informations générales du cahier (seulement si DRAFT)
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('charge_sheet:basic:write')")
    public ResponseEntity<ChargeSheetDto.CompleteDto> updateChargeSheet(
            @PathVariable Long id,
            @RequestBody ChargeSheetDto.UpdateGeneralDto dto) {
        ChargeSheet updated = service.updateChargeSheet(id, dto);
        return ResponseEntity.ok(service.getChargeSheetComplete(updated.getId()));
    }
    // Compléter
    @PutMapping("/{id}/complete")
    public ResponseEntity<ChargeSheetDto.CompleteDto> complete(@PathVariable Long id) {
        ChargeSheet updated = service.completeChargeSheet(id);
        return ResponseEntity.ok(service.getChargeSheetComplete(updated.getId()));
    }
    // ============ ENDPOINTS DE RÉCEPTION ============

    /**
     * Préparer les données pour la réception
     */
    @GetMapping("/{id}/prepare-reception")
    @PreAuthorize("hasAuthority('charge_sheet:tech:write')")
    public ResponseEntity<ReceptionDto.ReceptionResponseDto> prepareReception(@PathVariable Long id) {
        return ResponseEntity.ok(service.prepareReceptionData(id));
    }

    /**
     * Confirmer une réception partielle
     */
    @PostMapping("/{id}/confirm-partial-reception")
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
    @PreAuthorize("hasAuthority('charge_sheet:all:read')")
    public ResponseEntity<List<ReceptionHistoryDto>> getReceptionHistory(@PathVariable Long id) {
        return ResponseEntity.ok(service.getReceptionHistoryDto(id));
    }
}