// SiteController.java
package com.example.security.site;

import com.example.security.common.ApiResponse;
import com.example.security.projet.ProjetDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SiteDto>>> getAllSites() {
        List<SiteDto> sites = siteService.getAllSites();

        ApiResponse<List<SiteDto>> response = ApiResponse.success(
                "Liste des sites récupérée avec succès",
                sites
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SiteDto>> getSiteById(@PathVariable Long id) {
        SiteDto site = siteService.getSiteById(id);

        ApiResponse<SiteDto> response = ApiResponse.success(
                "Site récupéré avec succès",
                site
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SiteDto>> createSite(@RequestBody SiteDto dto) {
        SiteDto created = siteService.createSite(dto);

        ApiResponse<SiteDto> response = ApiResponse.success(
                "Site créé avec succès",
                created,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SiteDto>> updateSite(
            @PathVariable Long id,
            @RequestBody SiteDto dto) {
        SiteDto updated = siteService.updateSite(id, dto);

        ApiResponse<SiteDto> response = ApiResponse.success(
                "Site mis à jour avec succès",
                updated
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSite(@PathVariable Long id) {
        siteService.deleteSite(id);

        ApiResponse<Void> response = ApiResponse.success("Site supprimé avec succès");
        return ResponseEntity.ok(response);
    }
    // SiteController.java - Ajouter
    @GetMapping("/{siteId}/projets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ProjetDto>>> getProjetsBySite(@PathVariable Long siteId) {
        List<ProjetDto> projets = siteService.getProjetsBySite(siteId);

        ApiResponse<List<ProjetDto>> response = ApiResponse.success(
                "Liste des projets par site récupérée avec succès",
                projets
        );
        return ResponseEntity.ok(response);
    }
    // SiteController.java - Ajouter
    @PostMapping("/{siteId}/add-projet/{projetId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SiteDto>> addProjetToSite(
            @PathVariable Long siteId,
            @PathVariable Long projetId) {
        SiteDto updated = siteService.addProjetToSite(siteId, projetId);

        ApiResponse<SiteDto> response = ApiResponse.success(
                "Projet ajouté au site avec succès",
                updated
        );
        return ResponseEntity.ok(response);
    }
    // SiteController.java - Ajouter
    @PutMapping("/{siteId}/projets")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SiteDto>> updateSiteProjets(
            @PathVariable Long siteId,
            @RequestBody Map<String, List<Long>> body) {
        List<Long> projetIds = body.get("projetIds");
        SiteDto updated = siteService.updateSiteProjets(siteId, projetIds);

        ApiResponse<SiteDto> response = ApiResponse.success(
                "Projets du site mis à jour avec succès",
                updated
        );
        return ResponseEntity.ok(response);
    }
}