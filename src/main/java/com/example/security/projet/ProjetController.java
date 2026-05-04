// ProjetController.java
package com.example.security.projet;

import com.example.security.common.ApiResponse;
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
    public ResponseEntity<ApiResponse<List<ProjetDto>>> getAllProjets() {
        List<ProjetDto> projets = projetService.getAllProjets();

        ApiResponse<List<ProjetDto>> response = ApiResponse.success(
                "Liste des projets récupérée avec succès",
                projets
        );
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjetDto>> getProjetById(@PathVariable Long id) {
        ProjetDto projet = projetService.getProjetById(id);

        ApiResponse<ProjetDto> response = ApiResponse.success(
                "Projet récupéré avec succès",
                projet
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjetDto>> createProjet(@RequestBody ProjetDto dto) {
        ProjetDto created = projetService.createProjet(dto);

        ApiResponse<ProjetDto> response = ApiResponse.success(
                "Projet créé avec succès",
                created,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjetDto>> updateProjet(
            @PathVariable Long id,
            @RequestBody ProjetDto dto) {
        ProjetDto updated = projetService.updateProjet(id, dto);

        ApiResponse<ProjetDto> response = ApiResponse.success(
                "Projet mis à jour avec succès",
                updated
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProjet(@PathVariable Long id) {
        projetService.deleteProjet(id);

        ApiResponse<Void> response = ApiResponse.success("Projet supprimé avec succès");
        return ResponseEntity.ok(response);
    }

    // ProjetController.java - Ajouter cet endpoint
    @GetMapping("/site/{siteName}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ProjetDto>>> getProjetsBySiteName(@PathVariable String siteName) {
        List<ProjetDto> projets = projetService.getProjetsBySiteName(siteName);

        ApiResponse<List<ProjetDto>> response = ApiResponse.success(
                "Liste des projets par site récupérée avec succès",
                projets
        );
        return ResponseEntity.ok(response);
    }
    // ProjetController.java - Ajouter cet endpoint
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ProjetDto>>> getActiveProjets() {
        List<ProjetDto> projets = projetService.getActiveProjets();

        ApiResponse<List<ProjetDto>> response = ApiResponse.success(
                "Liste des projets actifs récupérée avec succès",
                projets
        );
        return ResponseEntity.ok(response);
    }
}