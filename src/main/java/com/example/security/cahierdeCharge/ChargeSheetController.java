package com.example.security.cahierdeCharge;

import com.example.security.common.ApiResponse;
import com.example.security.reception.ReceptionDto;
import com.example.security.reception.ReceptionHistoryDto;
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

/**
 * Contrôleur REST pour la gestion des cahiers des charges.
 * <p>
 * Ce contrôleur expose les endpoints permettant de gérer l'ensemble du cycle
 * de vie des cahiers des charges : création, consultation, modification,
 * validation, gestion des items techniques, réceptions et statistiques.
 * </p>
 *
 * <p><strong>Fonctionnalités principales :</strong></p>
 * <ul>
 *     <li><strong>CRUD complet</strong> : Création, lecture, modification, suppression</li>
 *     <li><strong>Gestion des items</strong> : Ajout, modification technique, suppression</li>
 *     <li><strong>Workflow de validation</strong> : Validation ING, validation PT, retour à ING</li>
 *     <li><strong>Gestion des réceptions</strong> : Préparation, confirmation partielle/complète, historique</li>
 *     <li><strong>Statistiques</strong> : Dashboard, variations mensuelles, création par mois</li>
 *     <li><strong>Gestion des statuts</strong> : Envoi fournisseur, confirmation réception, complétion</li>
 * </ul>
 *
 * <p><strong>Sécurité et permissions :</strong></p>
 * <ul>
 *     <li><strong>@PreAuthorize("hasAuthority('charge_sheet:basic:create')")</strong> : Création</li>
 *     <li><strong>@PreAuthorize("hasAuthority('charge_sheet:basic:write')")</strong> : Modification, ajout/suppression items</li>
 *     <li><strong>@PreAuthorize("hasAuthority('charge_sheet:tech:write')")</strong> : Saisie technique, réceptions</li>
 *     <li><strong>@PreAuthorize("hasAuthority('charge_sheet:all:read')")</strong> : Consultation</li>
 *     <li><strong>@PreAuthorize("hasRole('ADMIN')")</strong> : Suppression définitive</li>
 * </ul>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see ChargeSheetService
 * @see ChargeSheetDto
 * @since Sprint 4
 */
@RestController
@Tag(name = "Cahier de charges", description = "Gestion des cahiers de charges")
@RequestMapping("/api/v1/charge-sheets")
@RequiredArgsConstructor
public class ChargeSheetController {

    private final ChargeSheetService service;

    // ============================================================
    // ENDPOINTS - CRÉATION ET MODIFICATION DU CAHIER
    // ============================================================

    /**
     * Crée un nouveau cahier des charges.
     * <p>
     * Endpoint réservé aux ingénieurs (permission {@code charge_sheet:basic:create}).
     * Permet de créer un cahier avec ses informations générales et plusieurs items.
     * Le cahier est créé avec le statut {@code DRAFT}.
     * </p>
     *
     * @param dto Les informations de création (informations générales + items)
     * @return ResponseEntity contenant le DTO complet du cahier créé avec statut HTTP 201
     */
    @PostMapping
    @Operation(
            summary = "Créer un cahier de charges",
           description = "Créer un nouveau cahier de charges avec plusieurs items"

    )
    @PreAuthorize("hasAuthority('charge_sheet:basic:create')")
    public ResponseEntity<ApiResponse<ChargeSheetDto.CompleteDto>> createChargeSheet(@RequestBody ChargeSheetDto.CreateDto dto) {
        ChargeSheet created = service.createChargeSheet(dto);
        ChargeSheetDto.CompleteDto result = service.getChargeSheetComplete(created.getId());

        ApiResponse<ChargeSheetDto.CompleteDto> response = ApiResponse.success(
                "Cahier des charges créé avec succès",
                result,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Met à jour les informations générales d'un cahier des charges.
     * <p>
     * Endpoint réservé aux ingénieurs (permission {@code charge_sheet:basic:write}).
     * La modification n'est possible que lorsque le cahier est en statut {@code DRAFT}.
     * </p>
     *
     * @param dto Les nouvelles informations générales
     * @return ResponseEntity contenant le DTO complet du cahier mis à jour
     */
    // ING: Mettre à jour les informations générales du cahier (seulement si DRAFT)
    @PutMapping("/{id}")
    @Operation(summary = "Modifier un cahier", description = "Mettre à jour les informations générales d’un cahier (mode DRAFT uniquement)")
    @PreAuthorize("hasAuthority('charge_sheet:basic:write')")
    public ResponseEntity<ApiResponse<ChargeSheetDto.CompleteDto>> updateChargeSheet(
            @PathVariable Long id,
            @RequestBody ChargeSheetDto.UpdateGeneralDto dto) {
        ChargeSheet updated = service.updateChargeSheet(id, dto);
        ChargeSheetDto.CompleteDto result = service.getChargeSheetComplete(updated.getId());

        ApiResponse<ChargeSheetDto.CompleteDto> response = ApiResponse.success(
                "Cahier des charges mis à jour avec succès",
                result
        );
        return ResponseEntity.ok(response);
    }
    /**
     * Supprime définitivement un cahier des charges.
     * <p>
     * Endpoint réservé aux administrateurs. Supprime le cahier et tous ses items associés.
     * </p>
     *
     * @return ResponseEntity confirmant la suppression
     */
    // ADMIN: Supprimer un cahier des charges complet
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un cahier", description = "Supprimer complètement un cahier de charges (ADMIN uniquement)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteChargeSheet(@PathVariable Long id) {
        service.deleteChargeSheet(id);

        ApiResponse<Void> response = ApiResponse.success("Cahier des charges supprimé avec succès");
        return ResponseEntity.ok(response);
    }
    // ============================================================
    // ENDPOINTS - GESTION DES ITEMS TECHNIQUES
    // ============================================================

    /**
     * Ajoute un nouvel item technique à un cahier des charges.
     * <p>
     * Endpoint réservé aux ingénieurs (permission {@code charge_sheet:basic:write}).
     * </p>
     *
     * @param sheetId L'identifiant du cahier auquel ajouter l'item
     * @return ResponseEntity contenant le DTO complet du cahier mis à jour
     */
    @PostMapping("/{sheetId}/items")
    @Operation(summary = "Ajouter un item", description = "Ajouter un nouvel item dans un cahier de charges")
    @PreAuthorize("hasAuthority('charge_sheet:basic:write')")
    public ResponseEntity<ApiResponse<ChargeSheetDto.CompleteDto>> addItem(
            @PathVariable Long sheetId,
            @RequestBody ChargeSheetDto.ItemDto itemDto) {
        ChargeSheet updated = service.addItem(sheetId, itemDto);
        ChargeSheetDto.CompleteDto result = service.getChargeSheetComplete(updated.getId());

        ApiResponse<ChargeSheetDto.CompleteDto> response = ApiResponse.success(
                "Item ajouté avec succès",
                result
        );
        return ResponseEntity.ok(response);
    }
    /**
     * Met à jour les champs techniques d'un item spécifique.
     * <p>
     * Endpoint réservé aux techniciens (permission {@code charge_sheet:tech:write}).
     * Permet de saisir les 150+ attributs techniques d'un connecteur.
     * </p>
     *
     * @param sheetId L'identifiant du cahier contenant l'item
     * @param itemId L'identifiant de l'item à modifier
     * @param dto Les valeurs techniques à mettre à jour
     * @return ResponseEntity contenant le DTO de l'item mis à jour
     */
    @PutMapping("/{sheetId}/items/{itemId}/tech")
    @Operation(summary = "Modifier un item technique", description = "Mettre à jour les informations techniques d’un item spécifique")
    @PreAuthorize("hasAuthority('charge_sheet:tech:write')")
    public ResponseEntity<ApiResponse<ChargeSheetDto.ItemDto>> updateItemTech(
            @PathVariable Long sheetId,
            @PathVariable Long itemId,
            @RequestBody ChargeSheetDto.UpdateTechDto dto) {
        ChargeSheetItem updated = service.updateTechnicalFields(sheetId, itemId, dto);
        ChargeSheetDto.ItemDto result = service.mapToItemDtoPublic(updated);

        ApiResponse<ChargeSheetDto.ItemDto> response = ApiResponse.success(
                "Item technique mis à jour avec succès",
                result
        );
        return ResponseEntity.ok(response);
    }
    /**
     * Supprime un item d'un cahier des charges.
     * <p>
     * Endpoint réservé aux ingénieurs (permission {@code charge_sheet:basic:write}).
     * </p>
     *
     * @param sheetId L'identifiant du cahier contenant l'item
     * @param itemId L'identifiant de l'item à supprimer
     * @return ResponseEntity confirmant la suppression
     */
    @DeleteMapping("/{sheetId}/items/{itemId}")
    @Operation(summary = "Supprimer un item", description = "Supprimer un item d’un cahier de charges")
    @PreAuthorize("hasAuthority('charge_sheet:basic:write')")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable Long sheetId,
            @PathVariable Long itemId) {
        service.removeItem(sheetId, itemId);

        ApiResponse<Void> response = ApiResponse.success("Item supprimé avec succès");
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // ENDPOINTS - CONSULTATION
    // ============================================================

    /**
     * Récupère les détails complets d'un cahier des charges.
     * <p>
     * Endpoint accessible à tous les utilisateurs authentifiés
     * (permission {@code charge_sheet:all:read}).
     * </p>
     *
     * @param id L'identifiant du cahier à consulter
     * @return ResponseEntity contenant le DTO complet du cahier
     */
    @GetMapping("/{id}")
    @Operation(summary = "Consulter un cahier", description = "Récupérer les détails complets d’un cahier de charges")
    @PreAuthorize("hasAuthority('charge_sheet:all:read')")
    public ResponseEntity<ApiResponse<ChargeSheetDto.CompleteDto>> getChargeSheet(@PathVariable Long id) {
        ChargeSheetDto.CompleteDto result = service.getChargeSheetComplete(id);

        ApiResponse<ChargeSheetDto.CompleteDto> response = ApiResponse.success(
                "Cahier des charges récupéré avec succès",
                result
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère la liste de tous les cahiers des charges.
     * <p>
     * Endpoint accessible à tous les utilisateurs authentifiés.
     * Les résultats sont filtrés selon le projet et le site de l'utilisateur.
     * </p>
     *
     * @return ResponseEntity contenant la liste des DTO complets des cahiers
     */
    @GetMapping
    @Operation(summary = "Lister les cahiers", description = "Afficher tous les cahiers de charges")
    @PreAuthorize("hasAuthority('charge_sheet:all:read')")
    public ResponseEntity<ApiResponse<List<ChargeSheetDto.CompleteDto>>> getAllChargeSheets() {
        List<ChargeSheetDto.CompleteDto> result = service.getAllChargeSheets();

        ApiResponse<List<ChargeSheetDto.CompleteDto>> response = ApiResponse.success(
                "Liste des cahiers des charges récupérée avec succès",
                result
        );
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // ENDPOINTS - WORKFLOW DE VALIDATION
    // ============================================================

    /**
     * Valide un cahier par l'ingénieur.
     * <p>
     * Endpoint réservé aux ingénieurs. Fait passer le statut de
     * {@code DRAFT} à {@code VALIDATED_ING}. Une notification email
     * est envoyée aux équipes PT.
     * </p>
     *
     * @param id L'identifiant du cahier à valider
     * @return ResponseEntity contenant le DTO complet du cahier validé
     */
    @PutMapping("/{id}/validate-ing")
    @Operation(summary = "Validation ingénieur", description = "Valider le cahier par l’ingénieur")
    @PreAuthorize("hasAuthority('charge_sheet:basic:write')")
    public ResponseEntity<ApiResponse<ChargeSheetDto.CompleteDto>> validateByIng(@PathVariable Long id) {
        ChargeSheet validated = service.validateByIng(id);
        ChargeSheetDto.CompleteDto result = service.getChargeSheetComplete(validated.getId());

        ApiResponse<ChargeSheetDto.CompleteDto> response = ApiResponse.success(
                "Cahier des charges validé par ING avec succès",
                result
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Valide un cahier par le technicien.
     * <p>
     * Endpoint réservé aux techniciens. Fait passer le statut de
     * {@code TECH_FILLED} à {@code VALIDATED_PT}.
     * </p>
     *
     * @param id L'identifiant du cahier à valider
     * @return ResponseEntity contenant le DTO complet du cahier validé
     */
    @PutMapping("/{id}/validate-pt")
    @Operation(summary = "Validation technique", description = "Valider le cahier par le responsable technique")
    public ResponseEntity<ApiResponse<ChargeSheetDto.CompleteDto>> validateByPt(@PathVariable Long id) {
        ChargeSheet validated = service.validateByPt(id);
        ChargeSheetDto.CompleteDto result = service.getChargeSheetComplete(validated.getId());

        ApiResponse<ChargeSheetDto.CompleteDto> response = ApiResponse.success(
                "Cahier des charges validé par PT avec succès",
                result
        );
        return ResponseEntity.ok(response);
    }
    /**
     * Retourne un cahier à l'ingénieur pour corrections.
     * <p>
     * Endpoint réservé aux techniciens. Permet de retourner un cahier
     * à l'ingénieur lorsque des modifications sont nécessaires.
     * </p>
     *
     * @return ResponseEntity contenant le DTO complet du cahier retourné
     */
    @PutMapping("/{id}/revert-to-ing")
    @Operation(summary = "Retourner à ING", description = "Permet à PT de retourner un cahier à ING pour corrections")
    @PreAuthorize("hasAuthority('charge_sheet:tech:write')")
    public ResponseEntity<ApiResponse<ChargeSheetDto.CompleteDto>> revertToIng(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {

        String reason = body != null ? body.get("reason") : null;
        ChargeSheet reverted = service.revertToIng(id, reason);
        ChargeSheetDto.CompleteDto result = service.getChargeSheetComplete(reverted.getId());

        ApiResponse<ChargeSheetDto.CompleteDto> response = ApiResponse.success(
                "Cahier des charges retourné à ING avec succès",
                result
        );
        return ResponseEntity.ok(response);
    }
    // ============================================================
    // ENDPOINTS - GESTION DES STATUTS
    // ============================================================

    /**
     * Envoie un cahier au fournisseur.
     * <p>
     * Fait passer le statut de {@code VALIDATED_PT} à {@code SENT_TO_SUPPLIER}.
     * </p>
     *
     * @return ResponseEntity contenant le DTO complet du cahier mis à jour
     */
    @PutMapping("/{id}/send-supplier")
    @Operation(summary = "Envoyer au fournisseur", description = "Envoyer le cahier au fournisseur")
    public ResponseEntity<ApiResponse<ChargeSheetDto.CompleteDto>> sendToSupplier(@PathVariable Long id) {
        ChargeSheet updated = service.sendToSupplier(id);
        ChargeSheetDto.CompleteDto result = service.getChargeSheetComplete(updated.getId());

        ApiResponse<ChargeSheetDto.CompleteDto> response = ApiResponse.success(
                "Cahier des charges envoyé au fournisseur avec succès",
                result
        );
        return ResponseEntity.ok(response);
    }
    /**
     * Confirme la réception d'un cahier.
     * <p>
     * Fait passer le statut de {@code SENT_TO_SUPPLIER} à {@code RECEIVED_FROM_SUPPLIER}.
     * </p>
     *
     * @return ResponseEntity contenant le DTO complet du cahier mis à jour
     */
    @PutMapping("/{id}/confirm-reception")
    @Operation(summary = "Confirmer réception", description = "Confirmer la réception du cahier")
    public ResponseEntity<ApiResponse<ChargeSheetDto.CompleteDto>> confirmReception(@PathVariable Long id) {
        ChargeSheet updated = service.confirmReception(id);
        ChargeSheetDto.CompleteDto result = service.getChargeSheetComplete(updated.getId());

        ApiResponse<ChargeSheetDto.CompleteDto> response = ApiResponse.success(
                "Réception confirmée avec succès",
                result
        );
        return ResponseEntity.ok(response);
    }
    /**
     * Marque un cahier comme complété.
     * <p>
     * Fait passer le statut de {@code RECEIVED_FROM_SUPPLIER} à {@code COMPLETED}.
     * </p>
     *
     * @return ResponseEntity contenant le DTO complet du cahier mis à jour
     */
    @PutMapping("/{id}/complete")
    @Operation(summary = "Compléter le cahier", description = "Marquer le cahier comme complété")
    public ResponseEntity<ApiResponse<ChargeSheetDto.CompleteDto>> complete(@PathVariable Long id) {
        ChargeSheet updated = service.completeChargeSheet(id);
        ChargeSheetDto.CompleteDto result = service.getChargeSheetComplete(updated.getId());

        ApiResponse<ChargeSheetDto.CompleteDto> response = ApiResponse.success(
                "Cahier des charges complété avec succès",
                result
        );
        return ResponseEntity.ok(response);
    }
    // ============================================================
    // ENDPOINTS - GESTION DES RÉCEPTIONS
    // ============================================================

    /**
     * Prépare les données nécessaires pour la réception.
     * <p>
     * Retourne pour chaque item les quantités commandées, déjà reçues et restantes.
     * </p>
     *
     * @return ResponseEntity contenant les données de réception préparées
     */
    @GetMapping("/{id}/prepare-reception")
    @Operation(summary = "Préparer réception", description = "Préparer les données nécessaires pour la réception")
    @PreAuthorize("hasAuthority('charge_sheet:tech:write')")
    public ResponseEntity<ApiResponse<ReceptionDto.ReceptionResponseDto>> prepareReception(@PathVariable Long id) {
        ReceptionDto.ReceptionResponseDto result = service.prepareReceptionData(id);

        ApiResponse<ReceptionDto.ReceptionResponseDto> response = ApiResponse.success(
                "Données de réception préparées avec succès",
                result
        );
        return ResponseEntity.ok(response);
    }
    /**
     * Confirme une réception (partielle ou complète).
     * <p>
     * Enregistre les quantités reçues, génère les fiches de conformité,
     * met à jour les quantités restantes.
     * </p>
     *
     * @return ResponseEntity contenant le résultat de la réception
     */
    @PostMapping("/{id}/confirm-partial-reception")
    @Operation(summary = "Réception partielle", description = "Confirmer une réception partielle des items")
    @PreAuthorize("hasAuthority('charge_sheet:tech:write')")
    public ResponseEntity<ApiResponse<ReceptionDto.ReceptionResponseDto>> confirmPartialReception(
            @PathVariable Long id,
            @RequestBody ReceptionDto.ReceptionRequestDto request) {
        request.setChargeSheetId(id);
        ReceptionDto.ReceptionResponseDto result = service.confirmPartialReception(request);

        String message = result.isComplete()
                ? "Réception complète enregistrée avec succès"
                : "Réception partielle enregistrée avec succès";

        ApiResponse<ReceptionDto.ReceptionResponseDto> response = ApiResponse.success(
                message,
                result
        );
        return ResponseEntity.ok(response);
    }
    /**
     * Récupère l'historique complet des réceptions d'un cahier.
     *
     * @return ResponseEntity contenant la liste des réceptions
     */
    @GetMapping("/{id}/reception-history")
    @Operation(summary = "Historique réception", description = "Afficher l’historique des réceptions d’un cahier")
    @PreAuthorize("hasAuthority('charge_sheet:all:read')")
    public ResponseEntity<ApiResponse<List<ReceptionHistoryDto>>> getReceptionHistory(@PathVariable Long id) {
        List<ReceptionHistoryDto> result = service.getReceptionHistoryDto(id);

        ApiResponse<List<ReceptionHistoryDto>> response = ApiResponse.success(
                "Historique des réceptions récupéré avec succès",
                result
        );
        return ResponseEntity.ok(response);
    }
    // ============================================================
    // ENDPOINTS - STATISTIQUES
    // ============================================================

    /**
     * Récupère les statistiques pour le tableau de bord.
     * <p>
     * Retourne : nombre de cahiers par statut, répartition par projet,
     * réclamations par priorité, évolution mensuelle.
     * </p>
     *
     * @return ResponseEntity contenant les statistiques du dashboard
     */
    @GetMapping("/stats")
    @Operation(summary = "Statistiques", description = "Récupérer les statistiques pour le dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = service.getDashboardStats();

        ApiResponse<Map<String, Object>> response = ApiResponse.success(
                "Statistiques récupérées avec succès",
                stats
        );
        return ResponseEntity.ok(response);
    }
    /**
     * Calcule la variation entre deux mois spécifiques.
     *
     * @param month1 Premier mois (format YYYY-MM)
     * @param month2 Deuxième mois (format YYYY-MM)
     * @param project Projet optionnel pour filtrer
     * @return ResponseEntity contenant la variation calculée
     */
    @GetMapping("/stats/monthly-variation")
    @Operation(summary = "Variation mensuelle", description = "Calcule la variation entre deux mois spécifiques")
    @PreAuthorize("hasAuthority('charge_sheet:all:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyVariation(
            @RequestParam String month1,
            @RequestParam String month2,
            @RequestParam(required = false) String project) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userProject = (project != null && !project.isEmpty()) ? project : currentUser.getProjetsNames();

        Map<String, Object> variation = service.getVariationBetweenMonths(userProject, month1, month2);

        ApiResponse<Map<String, Object>> response = ApiResponse.success(
                "Variation mensuelle calculée avec succès",
                variation
        );
        return ResponseEntity.ok(response);
    }
    /**
     * Récupère les statistiques de création mensuelles.
     *
     * @param project Projet optionnel pour filtrer
     * @param months Nombre de mois à analyser (défaut: 6)
     * @return ResponseEntity contenant les statistiques mensuelles
     */
    @GetMapping("/stats/monthly-creation")
    @Operation(summary = "Statistiques de création mensuelles",
            description = "Nombre de cahiers créés par mois avec variations")
    @PreAuthorize("hasAuthority('charge_sheet:all:read')")
    public ResponseEntity<ApiResponse<MonthlyStatsDto>> getMonthlyCreationStats(
            @RequestParam(required = false) String project,
            @RequestParam(defaultValue = "6") int months) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userProject = (project != null && !project.isEmpty()) ? project : currentUser.getProjetsNames();

        MonthlyStatsDto stats = service.getMonthlyCreationStats(userProject, months);

        ApiResponse<MonthlyStatsDto> response = ApiResponse.success(
                "Statistiques de création mensuelles récupérées avec succès",
                stats
        );
        return ResponseEntity.ok(response);
    }
    /**
     * Calcule automatiquement la variation entre les deux derniers mois disponibles.
     *
     * @param project Projet optionnel pour filtrer
     * @return ResponseEntity contenant la variation calculée
     */
    @GetMapping("/stats/last-two-months")
    @Operation(summary = "Variation deux derniers mois",
            description = "Calcule automatiquement la variation entre les deux derniers mois disponibles")
    @PreAuthorize("hasAuthority('charge_sheet:all:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLastTwoMonthsVariation(
            @RequestParam(required = false) String project) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userProject = (project != null && !project.isEmpty()) ? project : currentUser.getProjetsNames();

        Map<String, Object> variation = service.getLastTwoMonthsVariation(userProject);

        ApiResponse<Map<String, Object>> response = ApiResponse.success(
                "Variation des deux derniers mois calculée avec succès",
                variation
        );
        return ResponseEntity.ok(response);
    }
}