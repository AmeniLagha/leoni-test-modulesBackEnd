// ProjetService.java
package com.example.security.projet;

import com.example.security.site.Site;
import com.example.security.site.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final SiteRepository siteRepository;

    public List<ProjetDto> getAllProjets() {
        return projetRepository.findAllByOrderByNameAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ProjetDto> getProjetsBySite(Long siteId) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new RuntimeException("Site non trouvé"));
        return site.getProjets().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ProjetDto getProjetById(Long id) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));
        return convertToDto(projet);
    }

    @Transactional
    public ProjetDto createProjet(ProjetDto dto) {
        if (projetRepository.findByName(dto.getName()).isPresent()) {
            throw new RuntimeException("Un projet avec ce nom existe déjà");
        }

        Projet projet = Projet.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .active(dto.isActive())
                .build();

        // ✅ Ajouter les sites associés
        if (dto.getSiteIds() != null && !dto.getSiteIds().isEmpty()) {
            List<Site> sites = siteRepository.findAllById(dto.getSiteIds());
            projet.setSites(sites);
        }

        return convertToDto(projetRepository.save(projet));
    }

    @Transactional
    public ProjetDto updateProjet(Long id, ProjetDto dto) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        if (dto.getName() != null && !dto.getName().equals(projet.getName())) {
            if (projetRepository.findByName(dto.getName()).isPresent()) {
                throw new RuntimeException("Un projet avec ce nom existe déjà");
            }
            projet.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            projet.setDescription(dto.getDescription());
        }

        projet.setActive(dto.isActive());

        // ✅ Mettre à jour les sites associés
        if (dto.getSiteIds() != null) {
            List<Site> sites = siteRepository.findAllById(dto.getSiteIds());
            projet.setSites(sites);
        }

        return convertToDto(projetRepository.save(projet));
    }

    @Transactional
    public void deleteProjet(Long id) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));
        projetRepository.delete(projet);
    }

    private ProjetDto convertToDto(Projet projet) {
        return ProjetDto.builder()
                .id(projet.getId())
                .name(projet.getName())
                .description(projet.getDescription())
                .active(projet.isActive())
                .siteNames(projet.getSites().stream().map(Site::getName).collect(Collectors.toList()))
                .siteIds(projet.getSites().stream().map(Site::getId).collect(Collectors.toList()))
                .build();
    }
    // ProjetService.java - Ajouter cette méthode
    public List<ProjetDto> getProjetsBySiteName(String siteName) {
        Site site = siteRepository.findByName(siteName)
                .orElseThrow(() -> new RuntimeException("Site non trouvé: " + siteName));

        return site.getProjets().stream()
                .filter(Projet::isActive)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    // ProjetService.java - Ajouter cette méthode
    public List<ProjetDto> getActiveProjets() {
        return projetRepository.findAllByActiveTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}