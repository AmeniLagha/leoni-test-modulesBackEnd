package com.example.security.TestUnitaire.service;

import com.example.security.projet.Projet;
import com.example.security.projet.ProjetDto;
import com.example.security.projet.ProjetRepository;
import com.example.security.site.Site;
import com.example.security.site.SiteDto;
import com.example.security.site.SiteRepository;
import com.example.security.site.SiteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SiteServiceTest {

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private ProjetRepository projetRepository;

    @InjectMocks
    private SiteService siteService;

    private Site testSite;
    private Projet testProjet1;
    private Projet testProjet2;
    private SiteDto testSiteDto;

    @BeforeEach
    void setUp() {
        testProjet1 = new Projet();
        testProjet1.setId(1L);
        testProjet1.setName("FORD");
        testProjet1.setActive(true);

        testProjet2 = new Projet();
        testProjet2.setId(2L);
        testProjet2.setName("TESLA");
        testProjet2.setActive(true);

        testSite = new Site();
        testSite.setId(1L);
        testSite.setName("MH1");
        testSite.setDescription("Site de Manzel Hayet");
        testSite.setActive(true);
        testSite.setProjets(new ArrayList<>(List.of(testProjet1)));

        testSiteDto = SiteDto.builder()
                .id(1L)
                .name("MH1")
                .description("Site de Manzel Hayet")
                .active(true)
                .projetIds(List.of(1L))
                .projetNames(List.of("FORD"))
                .build();
    }

    // ==================== getAllSites ====================

    @Test
    void getAllSites_ShouldReturnListOfSiteDtos() {
        when(siteRepository.findAllByOrderByNameAsc()).thenReturn(List.of(testSite));

        List<SiteDto> result = siteService.getAllSites();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("MH1");
        verify(siteRepository).findAllByOrderByNameAsc();
    }

    @Test
    void getAllSites_WhenNoSites_ShouldReturnEmptyList() {
        when(siteRepository.findAllByOrderByNameAsc()).thenReturn(Collections.emptyList());

        List<SiteDto> result = siteService.getAllSites();

        assertThat(result).isEmpty();
    }

    // ==================== getSiteById ====================

    @Test
    void getSiteById_WhenSiteExists_ShouldReturnSiteDto() {
        when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));

        SiteDto result = siteService.getSiteById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("MH1");
    }

    @Test
    void getSiteById_WhenSiteNotExists_ShouldThrowResponseStatusException() {
        when(siteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> siteService.getSiteById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.NOT_FOUND, "Site non trouvé");
    }

    // ==================== createSite ====================

    @Test
    void createSite_ShouldCreateAndReturnSiteDto() {
        SiteDto newSiteDto = SiteDto.builder()
                .name("MH2")
                .description("Nouveau site")
                .active(true)
                .projetIds(List.of(1L))
                .build();

        when(siteRepository.findByName("MH2")).thenReturn(Optional.empty());
        when(projetRepository.findAllById(List.of(1L))).thenReturn(List.of(testProjet1));
        when(siteRepository.save(any(Site.class))).thenAnswer(inv -> {
            Site site = inv.getArgument(0);
            site.setId(2L);
            return site;
        });

        SiteDto result = siteService.createSite(newSiteDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("MH2");
        verify(siteRepository).save(any(Site.class));
    }

    @Test
    void createSite_WithDuplicateName_ShouldThrowResponseStatusException() {
        when(siteRepository.findByName("MH1")).thenReturn(Optional.of(testSite));

        assertThatThrownBy(() -> siteService.createSite(testSiteDto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.CONFLICT, "Un site avec ce nom existe déjà");
    }

    // ==================== updateSite ====================

    @Test
    void updateSite_WhenSiteExists_ShouldUpdateAndReturnSiteDto() {
        SiteDto updateDto = SiteDto.builder()
                .name("MH1_UPDATED")
                .description("Description modifiée")
                .active(false)
                .projetIds(List.of(1L, 2L))
                .build();

        when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));
        when(siteRepository.findByName("MH1_UPDATED")).thenReturn(Optional.empty());
        when(projetRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(testProjet1, testProjet2));
        when(siteRepository.save(any(Site.class))).thenAnswer(inv -> inv.getArgument(0));

        SiteDto result = siteService.updateSite(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("MH1_UPDATED");
        verify(siteRepository).save(any(Site.class));
    }

    @Test
    void updateSite_WhenSiteNotExists_ShouldThrowResponseStatusException() {
        when(siteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> siteService.updateSite(99L, testSiteDto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.NOT_FOUND, "Site non trouvé");
    }

    @Test
    void updateSite_WithDuplicateName_ShouldThrowResponseStatusException() {
        Site existingSite = new Site();
        existingSite.setId(2L);
        existingSite.setName("OtherSite");

        SiteDto updateDto = SiteDto.builder()
                .name("OtherSite")
                .build();

        when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));
        when(siteRepository.findByName("OtherSite")).thenReturn(Optional.of(existingSite));

        assertThatThrownBy(() -> siteService.updateSite(1L, updateDto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.CONFLICT, "Un site avec ce nom existe déjà");
    }

    // ==================== deleteSite ====================

    @Test
    void deleteSite_WhenSiteExists_ShouldDeleteSite() {
        when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));
        doNothing().when(siteRepository).delete(testSite);

        siteService.deleteSite(1L);

        verify(siteRepository).delete(testSite);
    }

    @Test
    void deleteSite_WhenSiteNotExists_ShouldThrowResponseStatusException() {
        when(siteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> siteService.deleteSite(99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.NOT_FOUND, "Site non trouvé");
    }

    // ==================== getProjetsBySite ====================

    @Test
    void getProjetsBySite_WhenSiteExists_ShouldReturnProjets() {
        when(siteRepository.findByIdWithProjets(1L)).thenReturn(Optional.of(testSite));

        List<ProjetDto> result = siteService.getProjetsBySite(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("FORD");
        verify(siteRepository).findByIdWithProjets(1L);
    }

    @Test
    void getProjetsBySite_WhenSiteNotExists_ShouldThrowResponseStatusException() {
        when(siteRepository.findByIdWithProjets(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> siteService.getProjetsBySite(99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.NOT_FOUND, "Site non trouvé");
    }

    // ==================== addProjetToSite ====================

    @Test
    void addProjetToSite_ShouldAddProjet() {
        when(siteRepository.findByIdWithProjets(1L)).thenReturn(Optional.of(testSite));
        when(projetRepository.findById(2L)).thenReturn(Optional.of(testProjet2));
        when(siteRepository.save(any(Site.class))).thenReturn(testSite);

        SiteDto result = siteService.addProjetToSite(1L, 2L);

        assertThat(result).isNotNull();
        verify(siteRepository).save(any(Site.class));
    }

    @Test
    void addProjetToSite_WhenProjetAlreadyExists_ShouldNotDuplicate() {
        when(siteRepository.findByIdWithProjets(1L)).thenReturn(Optional.of(testSite));
        when(projetRepository.findById(1L)).thenReturn(Optional.of(testProjet1));
        // Pas besoin de mock pour save car il ne sera pas appelé

        SiteDto result = siteService.addProjetToSite(1L, 1L);

        assertThat(result).isNotNull();
        // ✅ Ne pas vérifier save car il n'est pas appelé
        verify(siteRepository, never()).save(any(Site.class));
    }

    @Test
    void addProjetToSite_WhenSiteNotExists_ShouldThrowResponseStatusException() {
        when(siteRepository.findByIdWithProjets(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> siteService.addProjetToSite(99L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.NOT_FOUND, "Site non trouvé");
    }

    @Test
    void addProjetToSite_WhenProjetNotExists_ShouldThrowResponseStatusException() {
        when(siteRepository.findByIdWithProjets(1L)).thenReturn(Optional.of(testSite));
        when(projetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> siteService.addProjetToSite(1L, 99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.NOT_FOUND, "Projet non trouvé");
    }

    // ==================== updateSiteProjets ====================

    @Test
    void updateSiteProjets_ShouldUpdateProjets() {
        when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));
        when(siteRepository.save(any(Site.class))).thenAnswer(inv -> inv.getArgument(0));
        when(projetRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(testProjet1, testProjet2));

        SiteDto result = siteService.updateSiteProjets(1L, List.of(1L, 2L));

        assertThat(result).isNotNull();
        verify(siteRepository).save(any(Site.class));
    }

    @Test
    void updateSiteProjets_WhenSiteNotExists_ShouldThrowResponseStatusException() {
        when(siteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> siteService.updateSiteProjets(99L, List.of(1L)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.NOT_FOUND, "Site non trouvé");
    }

    @Test
    void updateSiteProjets_WithNullProjetIds_ShouldClearProjets() {
        when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));
        when(siteRepository.save(any(Site.class))).thenAnswer(inv -> inv.getArgument(0));

        SiteDto result = siteService.updateSiteProjets(1L, null);

        assertThat(result).isNotNull();
        verify(siteRepository).save(any(Site.class));
    }

    @Test
    void updateSiteProjets_WithEmptyProjetIds_ShouldClearProjets() {
        when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));
        when(siteRepository.save(any(Site.class))).thenAnswer(inv -> inv.getArgument(0));

        SiteDto result = siteService.updateSiteProjets(1L, Collections.emptyList());

        assertThat(result).isNotNull();
        verify(siteRepository).save(any(Site.class));
    }
}