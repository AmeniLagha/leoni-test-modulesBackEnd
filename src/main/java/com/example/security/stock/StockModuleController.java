package com.example.security.stock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<StockModule> moveToStock(@PathVariable Long technicalFileId){
        StockModule stock = stockService.moveToStock(technicalFileId);
        return ResponseEntity.ok(stock);
    }
    // Dans StockModuleController.java
    // StockController.java - Ajouter/modifier ces méthodes
    @PostMapping("/move-item/{technicalFileItemId}")
    @Operation(summary = "Déplacer un item en stock", description = "Déplace un item technique vers le stock avec ses dernières valeurs")
    @PreAuthorize("hasAuthority('stock:write')")
    public ResponseEntity<StockModule> moveItemToStock(
            @PathVariable Long technicalFileItemId,
            @RequestBody(required = false) StockModuleDto additionalInfo) {

        if (additionalInfo == null) {
            additionalInfo = new StockModuleDto();
        }

        StockModule stock = stockService.moveItemToStock(technicalFileItemId, additionalInfo);
        return ResponseEntity.ok(stock);
    }
    // StockModuleController.java - Ajouter cet endpoint
    @GetMapping("/sites")
    @Operation(summary = "Liste des sites avec stock", description = "Récupère tous les sites qui ont des modules en stock")
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<List<Map<String, Object>>> getSitesWithStock() {
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

        return ResponseEntity.ok(new ArrayList<>(siteMap.values()));
    }

    @GetMapping("/item/{technicalFileItemId}/pre-stock-info")
    @Operation(summary = "Récupérer les infos d'un item pour pré-remplir le stock")
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<Map<String, Object>> getPreStockInfo(@PathVariable Long technicalFileItemId) {
        Map<String, Object> info = stockService.getPreStockInfo(technicalFileItemId);
        return ResponseEntity.ok(info);
    }
    // Lister tous les modules en stock
    // StockController.java - Ajouter/modifier ces endpoints
    @GetMapping
    @Operation(summary = "Lister tous les stocks", description = "Liste des stocks filtrée par site")
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<List<StockModuleDto>> getAllStock() {
        List<StockModule> stocks = stockService.getAllStock();
        List<StockModuleDto> dtos = stocks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Statistiques du stock", description = "Statistiques du stock pour le site de l'utilisateur")
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<Map<String, Object>> getStockStatistics() {
        return ResponseEntity.ok(stockService.getStockStatistics());
    }

    @GetMapping("/my-site")
    @Operation(summary = "Stock de mon site", description = "Liste des stocks uniquement pour le site de l'utilisateur connecté")
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<List<StockModule>> getMySiteStock() {
        return ResponseEntity.ok(stockService.getAllStock()); // Déjà filtré par site dans le service
    }

    // StockModuleController.java - Modifier l'endpoint getStockBySite
    @GetMapping("/site/{siteName}")
    @Operation(summary = "Stock par site", description = "Liste des stocks pour un site spécifique")
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<List<StockModuleDto>> getStockBySite(@PathVariable String siteName) {
        List<StockModule> stocks = stockService.getStockBySite(siteName);
        List<StockModuleDto> dtos = stocks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    // Détail d’un module stocké
    @GetMapping("/{id}")
    @Operation(
            summary = "Détail d’un module",
            description = "Récupérer les informations détaillées d’un module stocké"
    )
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<StockModule> getStockById(@PathVariable Long id){
        return ResponseEntity.ok(stockService.getStockById(id));
    }
    // Changer le statut d’un module stocké
    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Changer le statut",
            description = "Mettre à jour le statut d’un module en stock (ex: AVAILABLE, USED, DEFECTIVE)"
    )
    @PreAuthorize("hasAuthority('stock:write')")
    public ResponseEntity<StockModule> updateStatus(
            @PathVariable Long id,
            @RequestParam("status") StockModule.StockStatus status) {
        StockModule module = stockService.changeStatus(id, status);
        return ResponseEntity.ok(module);
    }
    // StockModuleController.java - Ajouter ces endpoints

    // Créer un module avec formulaire complet
    @PostMapping
    @Operation(summary = "Créer un module en stock", description = "Crée un nouveau module avec tous les champs du formulaire")
    @PreAuthorize("hasAuthority('stock:write')")
    public ResponseEntity<StockModule> createStockModule(@RequestBody StockModuleDto dto) {
        return ResponseEntity.ok(stockService.createStockModule(dto));
    }

    // Mettre à jour un module
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un module", description = "Met à jour tous les champs d'un module existant")
    @PreAuthorize("hasAuthority('stock:write')")
    public ResponseEntity<StockModule> updateStockModule(@PathVariable Long id, @RequestBody StockModuleDto dto) {
        return ResponseEntity.ok(stockService.updateStockModule(id, dto));
    }

    // Récupérer tous les modules (version avec DTO)
    @GetMapping("/dto")
    @Operation(summary = "Lister le stock (DTO)", description = "Afficher tous les modules avec tous les champs")
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<List<StockModuleDto>> getAllStockDto() {
        List<StockModule> stocks = stockService.getAllStock();
        List<StockModuleDto> dtos = stocks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
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