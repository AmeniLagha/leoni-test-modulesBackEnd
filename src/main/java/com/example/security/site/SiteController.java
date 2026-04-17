// SiteController.java
package com.example.security.site;

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
    public ResponseEntity<List<SiteDto>> getAllSites() {
        return ResponseEntity.ok(siteService.getAllSites());
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SiteDto> getSiteById(@PathVariable Long id) {
        return ResponseEntity.ok(siteService.getSiteById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SiteDto> createSite(@RequestBody SiteDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(siteService.createSite(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SiteDto> updateSite(@PathVariable Long id, @RequestBody SiteDto dto) {
        return ResponseEntity.ok(siteService.updateSite(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSite(@PathVariable Long id) {
        siteService.deleteSite(id);
        return ResponseEntity.noContent().build();
    }
    // SiteController.java - Ajouter
    @GetMapping("/{siteId}/projets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjetDto>> getProjetsBySite(@PathVariable Long siteId) {
        return ResponseEntity.ok(siteService.getProjetsBySite(siteId));
    }
    // SiteController.java - Ajouter
    @PostMapping("/{siteId}/add-projet/{projetId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SiteDto> addProjetToSite(
            @PathVariable Long siteId,
            @PathVariable Long projetId) {
        return ResponseEntity.ok(siteService.addProjetToSite(siteId, projetId));
    }
    // SiteController.java - Ajouter
    @PutMapping("/{siteId}/projets")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SiteDto> updateSiteProjets(
            @PathVariable Long siteId,
            @RequestBody Map<String, List<Long>> body) {
        List<Long> projetIds = body.get("projetIds");
        return ResponseEntity.ok(siteService.updateSiteProjets(siteId, projetIds));
    }
}