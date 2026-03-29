package com.example.security.stock;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockModuleController {

    private final StockService stockService;

    // Déplacer un module en stock
    @PostMapping("/move/{technicalFileId}")
    @PreAuthorize("hasAuthority('stock:write')")
    public ResponseEntity<StockModule> moveToStock(@PathVariable Long technicalFileId){
        StockModule stock = stockService.moveToStock(technicalFileId);
        return ResponseEntity.ok(stock);
    }
    // Dans StockModuleController.java
    @PostMapping("/move-item/{technicalFileItemId}")
    @PreAuthorize("hasAuthority('stock:write')")
    public ResponseEntity<StockModule> moveItemToStock(@PathVariable Long technicalFileItemId){
        StockModule stock = stockService.moveItemToStock(technicalFileItemId);
        return ResponseEntity.ok(stock);
    }
    // Lister tous les modules en stock
    @GetMapping
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<List<StockModule>> getAllStock(){
        return ResponseEntity.ok(stockService.getAllStock());
    }

    // Détail d’un module stocké
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('stock:read')")
    public ResponseEntity<StockModule> getStockById(@PathVariable Long id){
        return ResponseEntity.ok(stockService.getStockById(id));
    }
    // Changer le statut d’un module stocké
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('stock:write')")
    public ResponseEntity<StockModule> updateStatus(
            @PathVariable Long id,
            @RequestParam("status") StockModule.StockStatus status) {
        StockModule module = stockService.changeStatus(id, status);
        return ResponseEntity.ok(module);
    }
}