package com.example.security.stock;

import com.example.security.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/stock")
@Tag(name = "Stock", description = "Gestion des modules en stock et suivi de leur statut")
@RequiredArgsConstructor
public class StockModuleController {

    private final StockService stockService;

    // Déplacer un module en stock
    @PostMapping("/move/{technicalFileId}")
    @Operation(
            summary = "Ajouter au stock (dossier)",
            description = "Déplacer un dossier technique complet vers le stock"
    )
    @PreAuthorize("hasAuthority('stock:write')")
    public ResponseEntity<ApiResponse<StockModule>> moveToStock(@PathVariable Long technicalFileId) {
        StockModule stock = stockService.moveToStock(technicalFileId);

        ApiResponse<StockModule> response = ApiResponse.success(
                "Dossier technique déplacé vers le stock avec succès",
                stock,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    // Dans StockModuleController.java
    // StockController.java - Ajouter/modifier ces méthodes
    @PostMapping("/move-item/{technicalFileItemId}")
    @Operation(summary = "Déplacer un item en stock", description = "Déplace un item technique vers le stock avec ses dernières valeurs")
    @PreAuthorize("hasAuthority('stock:write')")
    public ResponseEntity<ApiResponse<StockModule>> moveItemToStock(
            @PathVariable Long technicalFileItemId,
            @RequestBody(required = false) StockModuleDto additionalInfo) {

        if (additionalInfo == null) {
            additionalInfo = new StockModuleDto();
        }

        StockModule stock = stockService.moveItemToStock(technicalFileItemId, additionalInfo);

        ApiResponse<StockModule> response = ApiResponse.success(
                "Item technique déplacé vers le stock avec succès",
                stock,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    // StockModuleController.java - Ajouter cet endpoint
    @GetMapping("/sites")
    @Operation(summary = "Liste des sites avec stock", description = "Récupère tous les sites qui ont des modules en stock")
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSitesWithStock() {
        List<StockModule> allStock = stockService.getAllStock();

        Map<String, Map<String, Object>> siteMap = new HashMap<>();

        for (StockModule stock : allStock) {
            if (stock.getSite() != null) {
                String siteName = stock.getSite().getName();
                Long siteId = stock.getSite().getId();

                if (!siteMap.containsKey(siteName)) {
                    Map<String, Object> siteInfo = new HashMap<>();
                    siteInfo.put("id", siteId);
                    siteInfo.put("name", siteName);
                    siteInfo.put("count", 0);
                    siteMap.put(siteName, siteInfo);
                }

                Map<String, Object> siteInfo = siteMap.get(siteName);
                siteInfo.put("count", (Integer) siteInfo.get("count") + 1);
            }
        }

        ApiResponse<List<Map<String, Object>>> response = ApiResponse.success(
                "Liste des sites avec stock récupérée avec succès",
                new ArrayList<>(siteMap.values())
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/item/{technicalFileItemId}/pre-stock-info")
    @Operation(summary = "Récupérer les infos d'un item pour pré-remplir le stock")
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPreStockInfo(@PathVariable Long technicalFileItemId) {
        Map<String, Object> info = stockService.getPreStockInfo(technicalFileItemId);

        ApiResponse<Map<String, Object>> response = ApiResponse.success(
                "Informations de pré-stock récupérées avec succès",
                info
        );
        return ResponseEntity.ok(response);
    }
    // Lister tous les modules en stock
    // StockController.java - Ajouter/modifier ces endpoints
    @GetMapping
    @Operation(summary = "Lister tous les stocks", description = "Liste des stocks filtrée par site")
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<ApiResponse<List<StockModuleDto>>> getAllStock() {
        List<StockModule> stocks = stockService.getAllStock();
        List<StockModuleDto> dtos = stocks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        ApiResponse<List<StockModuleDto>> response = ApiResponse.success(
                "Liste des stocks récupérée avec succès",
                dtos
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Statistiques du stock", description = "Statistiques du stock pour le site de l'utilisateur")
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStockStatistics() {
        Map<String, Object> stats = stockService.getStockStatistics();

        ApiResponse<Map<String, Object>> response = ApiResponse.success(
                "Statistiques du stock récupérées avec succès",
                stats
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-site")
    @Operation(summary = "Stock de mon site", description = "Liste des stocks uniquement pour le site de l'utilisateur connecté")
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<ApiResponse<List<StockModule>>> getMySiteStock() {
        List<StockModule> stocks = stockService.getAllStock();

        ApiResponse<List<StockModule>> response = ApiResponse.success(
                "Stock de votre site récupéré avec succès",
                stocks
        );
        return ResponseEntity.ok(response);
    }

    // StockModuleController.java - Modifier l'endpoint getStockBySite
    @GetMapping("/site/{siteName}")
    @Operation(summary = "Stock par site", description = "Liste des stocks pour un site spécifique")
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<ApiResponse<List<StockModuleDto>>> getStockBySite(@PathVariable String siteName) {
        List<StockModule> stocks = stockService.getStockBySite(siteName);
        List<StockModuleDto> dtos = stocks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        ApiResponse<List<StockModuleDto>> response = ApiResponse.success(
                "Stock du site " + siteName + " récupéré avec succès",
                dtos
        );
        return ResponseEntity.ok(response);
    }
    // Détail d’un module stocké
    @GetMapping("/{id}")
    @Operation(
            summary = "Détail d’un module",
            description = "Récupérer les informations détaillées d’un module stocké"
    )
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<ApiResponse<StockModule>> getStockById(@PathVariable Long id) {
        StockModule stock = stockService.getStockById(id);

        ApiResponse<StockModule> response = ApiResponse.success(
                "Module stocké récupéré avec succès",
                stock
        );
        return ResponseEntity.ok(response);
    }

    // Changer le statut d’un module stocké
    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Changer le statut",
            description = "Mettre à jour le statut d’un module en stock (ex: AVAILABLE, USED, DEFECTIVE)"
    )
    @PreAuthorize("hasAuthority('stock:write')")
    public ResponseEntity<ApiResponse<StockModule>> updateStatus(
            @PathVariable Long id,
            @RequestParam("status") StockModule.StockStatus status) {
        StockModule module = stockService.changeStatus(id, status);

        ApiResponse<StockModule> response = ApiResponse.success(
                "Statut du module mis à jour avec succès",
                module
        );
        return ResponseEntity.ok(response);
    }
    // StockModuleController.java - Ajouter ces endpoints

    // Créer un module avec formulaire complet
    @PostMapping
    @Operation(summary = "Créer un module en stock", description = "Crée un nouveau module avec tous les champs du formulaire")
    @PreAuthorize("hasAuthority('stock:write')")
    public ResponseEntity<ApiResponse<StockModule>> createStockModule(@RequestBody StockModuleDto dto) {
        StockModule created = stockService.createStockModule(dto);

        ApiResponse<StockModule> response = ApiResponse.success(
                "Module créé avec succès",
                created,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Mettre à jour un module
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un module", description = "Met à jour tous les champs d'un module existant")
    @PreAuthorize("hasAuthority('stock:write')")
    public ResponseEntity<ApiResponse<StockModule>> updateStockModule(
            @PathVariable Long id,
            @RequestBody StockModuleDto dto) {
        StockModule updated = stockService.updateStockModule(id, dto);

        ApiResponse<StockModule> response = ApiResponse.success(
                "Module mis à jour avec succès",
                updated
        );
        return ResponseEntity.ok(response);
    }

    // Récupérer tous les modules (version avec DTO)
    @GetMapping("/dto")
    @Operation(summary = "Lister le stock (DTO)", description = "Afficher tous les modules avec tous les champs")
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<ApiResponse<List<StockModuleDto>>> getAllStockDto() {
        List<StockModule> stocks = stockService.getAllStock();
        List<StockModuleDto> dtos = stocks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        ApiResponse<List<StockModuleDto>> response = ApiResponse.success(
                "Liste complète des modules récupérée avec succès",
                dtos
        );
        return ResponseEntity.ok(response);
    }

    private StockModuleDto convertToDto(StockModule stock) {
        return StockModuleDto.builder()
                .id(stock.getId())
                .technicalFileId(stock.getTechnicalFileId())
                .technicalFileItemId(stock.getTechnicalFileItemId())
                .chargeSheetItemId(stock.getChargeSheetItemId())
                .itemNumber(stock.getItemNumber())
                .position(stock.getPosition())
                .finalDisplacement(stock.getFinalDisplacement())
                .finalProgrammedSealing(stock.getFinalProgrammedSealing())
                .finalDetection(stock.getFinalDetection())
                .movedBy(stock.getMovedBy())
                .movedAt(stock.getMovedAt())
                .status(stock.getStatus())
                .casier(stock.getCasier())
                .stuffNumr(stock.getStuffNumr())
                .leoniNumr(stock.getLeoniNumr())
                .indexValue(stock.getIndexValue())
                .quantite(stock.getQuantite())
                .fournisseur(stock.getFournisseur())
                .etat(stock.getEtat())
                .caisse(stock.getCaisse())
                .specifications(stock.getSpecifications())
                .dernierMaj(stock.getDernierMaj())
                .infoSurModules(stock.getInfoSurModules())
                .demandeurExplication(stock.getDemandeurExplication())
                .dateDemande(stock.getDateDemande())
                .newQuantite(stock.getNewQuantite())
                // Ajouter les infos du site
                .siteId(stock.getSite() != null ? stock.getSite().getId() : null)
                .siteName(stock.getSite() != null ? stock.getSite().getName() : null)
                .build();
    }
}