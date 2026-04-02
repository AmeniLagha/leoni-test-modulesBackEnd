package com.example.security.stock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PostMapping("/move-item/{technicalFileItemId}")
    @Operation(
            summary = "Ajouter au stock (item)",
            description = "Déplacer un item technique individuel vers le stock"
    )
    @PreAuthorize("hasAuthority('stock:write')")
    public ResponseEntity<StockModule> moveItemToStock(@PathVariable Long technicalFileItemId){
        StockModule stock = stockService.moveItemToStock(technicalFileItemId);
        return ResponseEntity.ok(stock);
    }
    // Lister tous les modules en stock
    @GetMapping
    @Operation(
            summary = "Lister le stock",
            description = "Afficher tous les modules présents en stock"
    )
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<List<StockModule>> getAllStock(){
        return ResponseEntity.ok(stockService.getAllStock());
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
}