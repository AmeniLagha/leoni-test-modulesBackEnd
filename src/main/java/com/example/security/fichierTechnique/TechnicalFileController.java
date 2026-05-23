package com.example.security.fichierTechnique;

import com.example.security.common.ApiResponse;
import com.example.security.email.GlobalNotificationService;
import com.example.security.fichierTechnique.TechnicalFileHistory.TechnicalFileHistory;
import com.example.security.user.Role;
import com.example.security.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Dossier Technique", description = "Gestion des dossiers techniques, items, historique et validation")
@RequestMapping("/api/v1/technical-files")
@RequiredArgsConstructor
public class TechnicalFileController {

    private final TechnicalFileService service;
    private final TechnicalFileNotificationService notificationService ;

    // ✅ Créer un dossier technique
    @PostMapping
    @Operation(summary = "Créer un dossier technique", description = "Créer un nouveau dossier technique avec plusieurs items")
    @PreAuthorize("hasAuthority('technical_file:create')")
    public ResponseEntity<ApiResponse<TechnicalFile>> createTechnicalFile(
            @RequestBody TechnicalFileDto.CreateDto dto) {
        TechnicalFile created = service.createTechnicalFile(dto);

        ApiResponse<TechnicalFile> response = ApiResponse.success(
                "Dossier technique créé avec succès",
                created,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ✅ Récupérer tous les dossiers
    @GetMapping("/list")
    @Operation(summary = "Lister les dossiers", description = "Afficher tous les dossiers techniques (version simplifiée)")

    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<ApiResponse<List<TechnicalFileDto.ListDto>>> getAllTechnicalFiles() {
        List<TechnicalFileDto.ListDto> files = service.getAllTechnicalFilesList();

        ApiResponse<List<TechnicalFileDto.ListDto>> response = ApiResponse.success(
                "Liste des dossiers techniques récupérée avec succès",
                files
        );
        return ResponseEntity.ok(response);
    }

    // ✅ Récupérer un dossier par ID
    @GetMapping("/{id}")
    @Operation(summary = "Consulter un dossier", description = "Récupérer un dossier technique par son ID")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<ApiResponse<TechnicalFile>> getTechnicalFile(@PathVariable Long id) {
        TechnicalFile technicalFile = service.getTechnicalFileById(id);

        ApiResponse<TechnicalFile> response = ApiResponse.success(
                "Dossier technique récupéré avec succès",
                technicalFile
        );
        return ResponseEntity.ok(response);
    }

    // ✅ Récupérer tous les dossiers avec leurs items
    @GetMapping("/detail")
    @Operation(summary = "Détails complets", description = "Récupérer tous les dossiers avec leurs items")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<ApiResponse<List<TechnicalFileDto.ResponseDto>>> getAllTechnicalFilesWithItems() {
        List<TechnicalFileDto.ResponseDto> files = service.getAllTechnicalFilesWithItems();

        ApiResponse<List<TechnicalFileDto.ResponseDto>> response = ApiResponse.success(
                "Liste complète des dossiers techniques récupérée avec succès",
                files
        );
        return ResponseEntity.ok(response);
    }

    // ✅ Mettre à jour un dossier
    @PutMapping("/{id}")
    @Operation(summary = "Modifier un dossier", description = "Mettre à jour les informations d’un dossier technique")
    @PreAuthorize("hasAuthority('technical_file:write')")
    public ResponseEntity<ApiResponse<TechnicalFile>> updateTechnicalFile(
            @PathVariable Long id,
            @RequestBody TechnicalFileDto.UpdateDto dto) {
        TechnicalFile updated = service.updateTechnicalFile(id, dto);

        ApiResponse<TechnicalFile> response = ApiResponse.success(
                "Dossier technique mis à jour avec succès",
                updated
        );
        return ResponseEntity.ok(response);
    }

    // ✅ Supprimer un dossier
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un dossier", description = "Supprimer un dossier technique")
    @PreAuthorize("hasAuthority('technical_file:write')")
    public ResponseEntity<ApiResponse<Void>> deleteTechnicalFile(@PathVariable Long id) {
        service.deleteTechnicalFile(id);

        ApiResponse<Void> response = ApiResponse.success("Dossier technique supprimé avec succès");
        return ResponseEntity.ok(response);
    }

    // ✅ Historique Envers
    @GetMapping("/{id}/history-audited")
    @Operation(summary = "Historique audité", description = "Afficher l’historique complet via Hibernate Envers")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getHistoryAudited(@PathVariable Long id) {
        List<Map<String, Object>> history = service.getHistoryAudited(id);

        ApiResponse<List<Map<String, Object>>> response = ApiResponse.success(
                "Historique audité récupéré avec succès",
                history
        );
        return ResponseEntity.ok(response);
    }

    // =========================
    // TECHNICAL FILE ITEMS
    // =========================

    // ✅ Récupérer un item
    @GetMapping("/items/{itemId}")
    @Operation(summary = "Consulter un item", description = "Récupérer un item d’un dossier technique")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<ApiResponse<TechnicalFileItem>> getTechnicalFileItem(@PathVariable Long itemId) {
        TechnicalFileItem item = service.getTechnicalFileItemById(itemId);

        ApiResponse<TechnicalFileItem> response = ApiResponse.success(
                "Item technique récupéré avec succès",
                item
        );
        return ResponseEntity.ok(response);
    }

    // ✅ Mettre à jour un item
    @PutMapping("/items/{itemId}")
    @Operation(summary = "Modifier un item", description = "Mettre à jour un item technique")
    @PreAuthorize("hasAuthority('technical_file:write')")
    public ResponseEntity<ApiResponse<TechnicalFileItem>> updateTechnicalFileItem(
            @PathVariable Long itemId,
            @RequestBody TechnicalFileDto.UpdateItemDto dto) {
        TechnicalFileItem updated = service.updateTechnicalFileItem(itemId, dto);

        ApiResponse<TechnicalFileItem> response = ApiResponse.success(
                "Item technique mis à jour avec succès",
                updated
        );
        return ResponseEntity.ok(response);
    }

    // ✅ Supprimer un item
    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Supprimer un item", description = "Supprimer un item du dossier technique")
    @PreAuthorize("hasAuthority('technical_file:write')")
    public ResponseEntity<ApiResponse<Void>> deleteTechnicalFileItem(@PathVariable Long itemId) {
        service.deleteTechnicalFileItem(itemId);

        ApiResponse<Void> response = ApiResponse.success("Item technique supprimé avec succès");
        return ResponseEntity.ok(response);
    }

    // ✅ Historique item
    @GetMapping("/items/{itemId}/history")
    @Operation(summary = "Historique item", description = "Afficher l’historique des modifications d’un item")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<ApiResponse<List<TechnicalFileHistory>>> getItemHistory(@PathVariable Long itemId) {
        List<TechnicalFileHistory> history = service.getItemHistory(itemId);

        ApiResponse<List<TechnicalFileHistory>> response = ApiResponse.success(
                "Historique de l'item récupéré avec succès",
                history
        );
        return ResponseEntity.ok(response);
    }

    // ✅ Historique Envers item
    @GetMapping("/items/{itemId}/history-audited")
    @Operation(summary = "Historique item audité", description = "Historique complet via Envers pour un item")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getItemHistoryAudited(@PathVariable Long itemId) {
        List<Map<String, Object>> history = service.getItemHistoryAudited(itemId);

        ApiResponse<List<Map<String, Object>>> response = ApiResponse.success(
                "Historique audité de l'item récupéré avec succès",
                history
        );
        return ResponseEntity.ok(response);
    }

    // ✅ Historique complet dossier
    @GetMapping("/{id}/full-history-audited")
    @Operation(summary = "Historique complet", description = "Afficher l’historique complet du dossier et de ses items")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getFullHistory(@PathVariable Long id) {
        List<Map<String, Object>> history = service.getFullHistoryAudited(id);

        ApiResponse<List<Map<String, Object>>> response = ApiResponse.success(
                "Historique complet récupéré avec succès",
                history
        );
        return ResponseEntity.ok(response);
    }
    // ==================== VALIDATION ====================

    @PutMapping("/items/{itemId}/validate")
    @Operation(summary = "Valider un item", description = "Valider un item selon le rôle (PP, MC, MP)")
    @PreAuthorize("hasAnyRole('PP', 'MC', 'MP')")
    public ResponseEntity<ApiResponse<TechnicalFileItem>> validateItem(@PathVariable Long itemId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        Role userRole = currentUser.getRole();
        if (userRole == null) {
            throw new RuntimeException("Utilisateur sans rôle attribué");
        }

        String role = userRole.name();
        TechnicalFileItem validatedItem = service.validateItem(itemId, role);

        ApiResponse<TechnicalFileItem> response = ApiResponse.success(
                "Item validé avec succès par " + role,
                validatedItem
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/items/{itemId}/can-validate")
    @Operation(summary = "Vérifier validation", description = "Vérifier si l’utilisateur peut valider cet item")
    @PreAuthorize("hasAnyRole('PP', 'MC', 'MP')")
    public ResponseEntity<ApiResponse<Boolean>> canValidateItem(@PathVariable Long itemId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        Role userRole = currentUser.getRole();
        if (userRole == null) {
            ApiResponse<Boolean> response = ApiResponse.success("Vérification effectuée", false);
            return ResponseEntity.ok(response);
        }

        String role = userRole.name();
        boolean canValidate = service.canValidateItem(itemId, role);

        ApiResponse<Boolean> response = ApiResponse.success(
                "Vérification effectuée",
                canValidate
        );
        return ResponseEntity.ok(response);
    }
    // Dans TechnicalFileController.java, ajoute cette méthode :

    @PostMapping("/{id}/items")
    @Operation(summary = "Ajouter un item", description = "Ajouter un nouvel item dans un dossier technique")
    @PreAuthorize("hasAuthority('technical_file:create')")
    public ResponseEntity<ApiResponse<TechnicalFileItem>> addItemToTechnicalFile(
            @PathVariable Long id,
            @RequestBody TechnicalFileDto.AddItemDto dto) {
        TechnicalFileItem item = service.addItemToTechnicalFile(id, dto);

        ApiResponse<TechnicalFileItem> response = ApiResponse.success(
                "Item ajouté au dossier technique avec succès",
                item,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    // Dans TechnicalFileController.java - Ajoutez

    @GetMapping("/notifications/pending")
    @Operation(summary = "Notifications en attente", description = "Récupère les items techniques nécessitant une attention")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPendingNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        List<Map<String, Object>> notifications = notificationService.getPendingNotificationsForUser(userEmail);

        ApiResponse<List<Map<String, Object>>> response = ApiResponse.success(
                "Notifications récupérées avec succès",
                notifications
        );
        return ResponseEntity.ok(response);
    }
    // TechnicalFileController.java - Ajouter cet endpoint

    @GetMapping("/items/{itemId}/versions-compare")
    @Operation(summary = "Comparer versions", description = "Affiche la première et la dernière version d'un item")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFirstAndCurrentVersions(@PathVariable Long itemId) {
        Map<String, Object> versions = service.getFirstAndCurrentVersions(itemId);

        ApiResponse<Map<String, Object>> response = ApiResponse.success(
                "Comparaison des versions récupérée avec succès",
                versions
        );
        return ResponseEntity.ok(response);
    }
    // Dans TechnicalFileController.java - Ajouter cet endpoint

    @GetMapping("/items/{itemId}/all-versions")
    @Operation(summary = "Toutes les versions", description = "Afficher toutes les versions historiques d'un item")
    @PreAuthorize("hasAuthority('technical_file:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllVersions(@PathVariable Long itemId) {
        List<Map<String, Object>> versions = service.getAllVersionsAudited(itemId);

        ApiResponse<List<Map<String, Object>>> response = ApiResponse.success(
                "Toutes les versions récupérées avec succès (" + versions.size() + " versions)",
                versions
        );
        return ResponseEntity.ok(response);
    }
}