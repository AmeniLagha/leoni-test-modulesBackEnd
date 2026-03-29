package com.example.security.conformité;

import com.example.security.cahierdeCharge.ChargeSheetItem;
import com.example.security.cahierdeCharge.ChargeSheetItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/compliance")
@RequiredArgsConstructor
public class ComplianceController {

    private final ComplianceService service;
    private final ChargeSheetItemRepository chargeSheetItemRepository;
    private final CompliancePreparationService compliancePreparationService; // Ajouté

    // PP: Créer une conformité
    @PostMapping
    @PreAuthorize("hasAuthority('compliance:create')")
    public ResponseEntity<Compliance> createCompliance(@RequestBody ComplianceDto.CreateDto dto) {
        return ResponseEntity.ok(service.createCompliance(dto));
    }

    // PP: Mettre à jour une conformité
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('compliance:write')")
    public ResponseEntity<Compliance> updateCompliance(
            @PathVariable Long id,
            @RequestBody ComplianceDto.UpdateDto dto) {
        return ResponseEntity.ok(service.updateCompliance(id, dto));
    }

    // PP, MC, MP: Lire une conformité
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<Compliance> getCompliance(@PathVariable Long id) {
        return ResponseEntity.ok(service.getComplianceById(id));
    }

    // Tous: Lister les conformités par cahier des charges
    @GetMapping("/charge-sheet/{chargeSheetId}")
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<List<Compliance>> getComplianceByChargeSheet(
            @PathVariable Long chargeSheetId) {
        return ResponseEntity.ok(service.getComplianceByChargeSheetId(chargeSheetId));
    }

    // Tous: Lister les conformités par item
    @GetMapping("/item/{itemId}")
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<List<Compliance>> getComplianceByItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(complianceRepository.findByItemId(itemId));
    }

    // Admin: Lister toutes les conformités
    @GetMapping
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<List<Compliance>> getAllCompliance() {
        return ResponseEntity.ok(service.getAllCompliance());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('compliance:write')")
    public ResponseEntity<Void> deleteCompliance(@PathVariable Long id) {
        service.deleteCompliance(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create-for-item/{itemId}")
    @PreAuthorize("hasAuthority('compliance:create')")
    public ResponseEntity<List<Compliance>> createForItem(@PathVariable Long itemId) {
        ChargeSheetItem item = chargeSheetItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        List<Compliance> created = service.createComplianceForItem(item);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/display")
    @PreAuthorize("hasAuthority('compliance:read')")
    public ResponseEntity<List<ComplianceDisplayDto>> getAllComplianceForDisplay() {
        return ResponseEntity.ok(service.getAllComplianceForDisplay());
    }

    /**
     * Préparer les données pour la création de fiches de conformité
     * Renvoie le nombre de fiches à créer basé sur les quantités reçues
     */
    @GetMapping("/prepare/{itemId}")
    @PreAuthorize("hasAuthority('compliance:create')")
    public ResponseEntity<List<ComplianceDto.PrepareComplianceDto>> prepareComplianceForItem(
            @PathVariable Long itemId) {
        return ResponseEntity.ok(compliancePreparationService.prepareComplianceForItem(itemId));
    }

    /**
     * Créer les fiches de conformité pour les quantités reçues
     */
    @PostMapping("/create-for-received/{itemId}")
    @PreAuthorize("hasAuthority('compliance:create')")
    public ResponseEntity<List<Compliance>> createComplianceForReceivedQuantity(
            @PathVariable Long itemId,
            @RequestParam int numberOfSheets) {
        return ResponseEntity.ok(compliancePreparationService.createComplianceForReceivedQuantity(itemId, numberOfSheets));
    }

    // Ajoutez cette dépendance
    private final ComplianceRepository complianceRepository;
}