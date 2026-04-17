// ProjetController.java
package com.example.security.projet;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/projets")
@RequiredArgsConstructor
public class ProjetController {

    private final ProjetService projetService;

    @GetMapping
    public ResponseEntity<List<ProjetDto>> getAllProjets() {
        return ResponseEntity.ok(projetService.getAllProjets());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjetDto> getProjetById(@PathVariable Long id) {
        return ResponseEntity.ok(projetService.getProjetById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjetDto> createProjet(@RequestBody ProjetDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projetService.createProjet(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjetDto> updateProjet(@PathVariable Long id, @RequestBody ProjetDto dto) {
        return ResponseEntity.ok(projetService.updateProjet(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProjet(@PathVariable Long id) {
        projetService.deleteProjet(id);
        return ResponseEntity.noContent().build();
    }

    // ProjetController.java - Ajouter cet endpoint
    @GetMapping("/site/{siteName}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjetDto>> getProjetsBySiteName(@PathVariable String siteName) {
        return ResponseEntity.ok(projetService.getProjetsBySiteName(siteName));
    }
    // ProjetController.java - Ajouter cet endpoint
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjetDto>> getActiveProjets() {
        return ResponseEntity.ok(projetService.getActiveProjets());
    }
}