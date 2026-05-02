// SiteService.java
package com.example.security.site;

import com.example.security.projet.Projet;
import com.example.security.projet.ProjetDto;
import com.example.security.projet.ProjetRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;
    private final ProjetRepository projetRepository;

    public List<SiteDto> getAllSites() {
        return siteRepository.findAllByOrderByNameAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public SiteDto getSiteById(Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site non trouvé"));
        return convertToDto(site);
    }

    @Transactional
    public SiteDto createSite(SiteDto dto) {
        if (siteRepository.findByName(dto.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un site avec ce nom existe déjà");
        }

        Site site = Site.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .active(dto.isActive())
                .build();

        // ✅ Ajouter les projets associés
        if (dto.getProjetIds() != null && !dto.getProjetIds().isEmpty()) {
            List<Projet> projets = projetRepository.findAllById(dto.getProjetIds());
            site.setProjets(projets);
        }

        return convertToDto(siteRepository.save(site));
    }

    @Transactional
    public SiteDto updateSite(Long id, SiteDto dto) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site non trouvé"));

        if (dto.getName() != null && !dto.getName().equals(site.getName())) {
            if (siteRepository.findByName(dto.getName()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Un site avec ce nom existe déjà");
            }
            site.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            site.setDescription(dto.getDescription());
        }

        site.setActive(dto.isActive());

        // ✅ Mettre à jour les projets associés
        if (dto.getProjetIds() != null) {
            List<Projet> projets = projetRepository.findAllById(dto.getProjetIds());
            site.setProjets(projets);
        }

        return convertToDto(siteRepository.save(site));
    }

    @Transactional
    public void deleteSite(Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site non trouvé"));
        siteRepository.delete(site);
    }

    private SiteDto convertToDto(Site site) {
        return SiteDto.builder()
                .id(site.getId())
                .name(site.getName())
                .description(site.getDescription())
                .active(site.isActive())
                .projetNames(site.getProjets().stream().map(Projet::getName).collect(Collectors.toList()))
                .projetIds(site.getProjets().stream().map(Projet::getId).collect(Collectors.toList()))
                .build();
    }
    // SiteService.java - Ajouter
    public List<ProjetDto> getProjetsBySite(Long siteId) {
        // ✅ Utiliser la méthode avec FETCH pour charger les projets
        Site site = siteRepository.findByIdWithProjets(siteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site non trouvé"));

        return site.getProjets().stream()
                .map(projet -> ProjetDto.builder()
                        .id(projet.getId())
                        .name(projet.getName())
                        .description(projet.getDescription())
                        .active(projet.isActive())
                        .build())
                .collect(Collectors.toList());
    }
    @Transactional
    public SiteDto addProjetToSite(Long siteId, Long projetId) {
        System.out.println("🔍 addProjetToSite - siteId: " + siteId + ", projetId: " + projetId);

        // ✅ Récupérer le site avec un JOIN FETCH pour charger les projets
        Site site = siteRepository.findByIdWithProjets(siteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site non trouvé"));

        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projet non trouvé"));

        if (!site.getProjets().contains(projet)) {
            site.getProjets().add(projet);
            site = siteRepository.save(site);
            System.out.println("✅ Projet " + projetId + " ajouté au site " + siteId);
        } else {
            System.out.println("⚠️ Le projet " + projetId + " est déjà associé au site " + siteId);
        }

        return convertToDto(site);
    }

    @Transactional
    public SiteDto updateSiteProjets(Long siteId, List<Long> projetIds) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site non trouvé"));

        // Vider les associations existantes
        site.getProjets().clear();

        // Créer les nouvelles associations
        if (projetIds != null && !projetIds.isEmpty()) {
            List<Projet> projets = projetRepository.findAllById(projetIds);
            site.getProjets().addAll(projets);
        }

        site = siteRepository.save(site);
        return convertToDto(site);
    }
}