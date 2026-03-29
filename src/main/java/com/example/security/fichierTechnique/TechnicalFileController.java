package com.example.security.fichierTechnique;

import com.example.security.fichierTechnique.TechnicalFileHistory.TechnicalFileHistory;
import com.example.security.user.Role;
import com.example.security.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/technical-files")
@RequiredArgsConstructor
public class TechnicalFileController {

    private final TechnicalFileService service;

    // ✅ Créer un dossier technique
    @PostMapping
    @PreAuthorize("hasAuthority('technical_file:create')")
    public ResponseEntity<TechnicalFile> createTechnicalFile(
            @RequestBody TechnicalFileDto.CreateDto dto) {

        return ResponseEntity.ok(service.createTechnicalFile(dto));
    }

    // ✅ Récupérer tous les dossiers
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<List<TechnicalFileDto.ListDto>> getAllTechnicalFiles() {

        return ResponseEntity.ok(service.getAllTechnicalFilesList());
    }

    // ✅ Récupérer un dossier par ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<TechnicalFile> getTechnicalFile(@PathVariable Long id) {

        return ResponseEntity.ok(service.getTechnicalFileById(id));
    }

    // ✅ Récupérer tous les dossiers avec leurs items
    @GetMapping("/detail")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<List<TechnicalFileDto.ResponseDto>> getAllTechnicalFilesWithItems() {

        return ResponseEntity.ok(service.getAllTechnicalFilesWithItems());
    }

    // ✅ Mettre à jour un dossier
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('technical_file:write')")
    public ResponseEntity<TechnicalFile> updateTechnicalFile(
            @PathVariable Long id,
            @RequestBody TechnicalFileDto.UpdateDto dto) {

        return ResponseEntity.ok(service.updateTechnicalFile(id, dto));
    }

    // ✅ Supprimer un dossier
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('technical_file:write')")
    public ResponseEntity<Void> deleteTechnicalFile(@PathVariable Long id) {

        service.deleteTechnicalFile(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Historique simple
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<List<TechnicalFileHistory>> getHistory(@PathVariable Long id) {

        return ResponseEntity.ok(service.getHistory(id));
    }

    // ✅ Historique Envers
    @GetMapping("/{id}/history-audited")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<?> getHistoryAudited(@PathVariable Long id) {

        return ResponseEntity.ok(service.getHistoryAudited(id));
    }

    // =========================
    // TECHNICAL FILE ITEMS
    // =========================

    // ✅ Récupérer un item
    @GetMapping("/items/{itemId}")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<TechnicalFileItem> getTechnicalFileItem(@PathVariable Long itemId) {

        return ResponseEntity.ok(service.getTechnicalFileItemById(itemId));
    }

    // ✅ Mettre à jour un item
    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasAuthority('technical_file:write')")
    public ResponseEntity<TechnicalFileItem> updateTechnicalFileItem(
            @PathVariable Long itemId,
            @RequestBody TechnicalFileDto.UpdateItemDto dto) {

        return ResponseEntity.ok(service.updateTechnicalFileItem(itemId, dto));
    }

    // ✅ Supprimer un item
    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasAuthority('technical_file:write')")
    public ResponseEntity<Void> deleteTechnicalFileItem(@PathVariable Long itemId) {

        service.deleteTechnicalFileItem(itemId);
        return ResponseEntity.noContent().build();
    }

    // ✅ Historique item
    @GetMapping("/items/{itemId}/history")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<List<TechnicalFileHistory>> getItemHistory(@PathVariable Long itemId) {

        return ResponseEntity.ok(service.getItemHistory(itemId));
    }

    // ✅ Historique Envers item
    @GetMapping("/items/{itemId}/history-audited")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<?> getItemHistoryAudited(@PathVariable Long itemId) {

        return ResponseEntity.ok(service.getItemHistoryAudited(itemId));
    }

    // ✅ Historique complet dossier
    @GetMapping("/{id}/full-history-audited")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<?> getFullHistory(@PathVariable Long id) {

        return ResponseEntity.ok(service.getFullHistoryAudited(id));
    }
    // ==================== VALIDATION ====================

    @PutMapping("/items/{itemId}/validate")
    @PreAuthorize("hasAnyRole('PP', 'MC', 'MP')")
    public ResponseEntity<TechnicalFileItem> validateItem(@PathVariable Long itemId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        Role userRole = currentUser.getRole();
        if (userRole == null) {
            throw new RuntimeException("Utilisateur sans rôle attribué");
        }

        String role = userRole.name(); // Pour une énumération, on utilise .name()

        TechnicalFileItem validatedItem = service.validateItem(itemId, role);
        return ResponseEntity.ok(validatedItem);
    }

    @GetMapping("/items/{itemId}/can-validate")
    @PreAuthorize("hasAnyRole('PP', 'MC', 'MP')")
    public ResponseEntity<Boolean> canValidateItem(@PathVariable Long itemId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        Role userRole = currentUser.getRole();
        if (userRole == null) {
            return ResponseEntity.ok(false);
        }

        String role = userRole.name();

        return ResponseEntity.ok(service.canValidateItem(itemId, role));
    }
    // Dans TechnicalFileController.java, ajoute cette méthode :

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAuthority('technical_file:create')")
    public ResponseEntity<TechnicalFileItem> addItemToTechnicalFile(
            @PathVariable Long id,
            @RequestBody TechnicalFileDto.AddItemDto dto) {
        return ResponseEntity.ok(service.addItemToTechnicalFile(id, dto));
    }
}