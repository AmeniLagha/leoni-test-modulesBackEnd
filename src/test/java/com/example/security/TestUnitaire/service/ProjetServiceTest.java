package com.example.security.TestUnitaire.service;

import com.example.security.projet.Projet;
import com.example.security.projet.ProjetDto;
import com.example.security.projet.ProjetRepository;
import com.example.security.projet.ProjetService;
import com.example.security.site.Site;
import com.example.security.site.SiteRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjetServiceTest {

    @Mock
    private ProjetRepository projetRepository;

    @Mock
    private SiteRepository siteRepository;

    @InjectMocks
    private ProjetService projetService;

    private Site testSite;
    private Projet testProjet1;
    private Projet testProjet2;
    private ProjetDto testProjetDto;

    @BeforeEach
    void setUp() {
        testSite = new Site();
        testSite.setId(1L);
        testSite.setName("MH1");
        testSite.setActive(true);
        testSite.setProjets(new ArrayList<>());

        testProjet1 = new Projet();
        testProjet1.setId(1L);
        testProjet1.setName("FORD");
        testProjet1.setDescription("Projet Ford");
        testProjet1.setActive(true);
        testProjet1.setSites(List.of(testSite));
        testSite.getProjets().add(testProjet1);

        testProjet2 = new Projet();
        testProjet2.setId(2L);
        testProjet2.setName("TESLA");
        testProjet2.setDescription("Projet Tesla");
        testProjet2.setActive(true);

        testProjetDto = ProjetDto.builder()
                .id(1L)
                .name("FORD")
                .description("Projet Ford")
                .active(true)
                .siteIds(List.of(1L))
                .siteNames(List.of("MH1"))
                .build();
    }

    // ==================== getAllProjets ====================

    @Test
    void getAllProjets_ShouldReturnListOfProjetDtos() {
        when(projetRepository.findAllByOrderByNameAsc()).thenReturn(List.of(testProjet1, testProjet2));

        List<ProjetDto> result = projetService.getAllProjets();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("FORD");
        assertThat(result.get(1).getName()).isEqualTo("TESLA");
        verify(projetRepository).findAllByOrderByNameAsc();
    }

    @Test
    void getAllProjets_WhenNoProjets_ShouldReturnEmptyList() {
        when(projetRepository.findAllByOrderByNameAsc()).thenReturn(Collections.emptyList());

        List<ProjetDto> result = projetService.getAllProjets();

        assertThat(result).isEmpty();
    }

    // ==================== getProjetsBySite ====================

    @Test
    void getProjetsBySite_WhenSiteExists_ShouldReturnProjets() {
        when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));

        List<ProjetDto> result = projetService.getProjetsBySite(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("FORD");
        verify(siteRepository).findById(1L);
    }

    @Test
    void getProjetsBySite_WhenSiteNotExists_ShouldThrowResponseStatusException() {
        when(siteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projetService.getProjetsBySite(99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.NOT_FOUND, "Site non trouvé");
    }

    // ==================== getProjetById ====================

    @Test
    void getProjetById_WhenProjetExists_ShouldReturnProjetDto() {
        when(projetRepository.findById(1L)).thenReturn(Optional.of(testProjet1));

        ProjetDto result = projetService.getProjetById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("FORD");
        verify(projetRepository).findById(1L);
    }

    @Test
    void getProjetById_WhenProjetNotExists_ShouldThrowResponseStatusException() {
        when(projetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projetService.getProjetById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.NOT_FOUND, "Projet non trouvé");
    }

    // ==================== createProjet ====================

    @Test
    void createProjet_ShouldCreateAndReturnProjetDto() {
        ProjetDto newProjetDto = ProjetDto.builder()
                .name("BMW")
                .description("Projet BMW")
                .active(true)
                .siteIds(List.of(1L))
                .build();

        when(projetRepository.findByName("BMW")).thenReturn(Optional.empty());
        when(siteRepository.findAllById(List.of(1L))).thenReturn(List.of(testSite));
        when(projetRepository.save(any(Projet.class))).thenAnswer(inv -> {
            Projet p = inv.getArgument(0);
            p.setId(3L);
            return p;
        });

        ProjetDto result = projetService.createProjet(newProjetDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("BMW");
        verify(projetRepository).save(any(Projet.class));
    }

    @Test
    void createProjet_WithDuplicateName_ShouldThrowResponseStatusException() {
        when(projetRepository.findByName("FORD")).thenReturn(Optional.of(testProjet1));

        assertThatThrownBy(() -> projetService.createProjet(testProjetDto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.CONFLICT, "Un projet avec ce nom existe déjà");

        verify(projetRepository, never()).save(any());
    }

    // ==================== updateProjet ====================

    @Test
    void updateProjet_WhenProjetExists_ShouldUpdateAndReturnProjetDto() {
        ProjetDto updateDto = ProjetDto.builder()
                .name("FORD_UPDATED")
                .description("Description modifiée")
                .active(false)
                .siteIds(List.of(1L))
                .build();

        when(projetRepository.findById(1L)).thenReturn(Optional.of(testProjet1));
        when(projetRepository.findByName("FORD_UPDATED")).thenReturn(Optional.empty());
        when(siteRepository.findAllById(List.of(1L))).thenReturn(List.of(testSite));
        when(projetRepository.save(any(Projet.class))).thenAnswer(inv -> inv.getArgument(0));

        ProjetDto result = projetService.updateProjet(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("FORD_UPDATED");
        assertThat(result.getDescription()).isEqualTo("Description modifiée");
        assertThat(result.isActive()).isFalse();
        verify(projetRepository).save(any(Projet.class));
    }

    @Test
    void updateProjet_WhenProjetNotExists_ShouldThrowResponseStatusException() {
        when(projetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projetService.updateProjet(99L, testProjetDto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.NOT_FOUND, "Projet non trouvé");
    }

    @Test
    void updateProjet_WithDuplicateName_ShouldThrowResponseStatusException() {
        Projet anotherProjet = new Projet();
        anotherProjet.setId(3L);
        anotherProjet.setName("OtherProjet");

        ProjetDto updateDto = ProjetDto.builder()
                .name("OtherProjet")
                .build();

        when(projetRepository.findById(1L)).thenReturn(Optional.of(testProjet1));
        when(projetRepository.findByName("OtherProjet")).thenReturn(Optional.of(anotherProjet));

        assertThatThrownBy(() -> projetService.updateProjet(1L, updateDto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.CONFLICT, "Un projet avec ce nom existe déjà");
    }

    // ==================== deleteProjet ====================

    @Test
    void deleteProjet_WhenProjetExists_ShouldDeleteProjet() {
        when(projetRepository.findById(1L)).thenReturn(Optional.of(testProjet1));
        doNothing().when(projetRepository).delete(testProjet1);

        projetService.deleteProjet(1L);

        verify(projetRepository).delete(testProjet1);
    }

    @Test
    void deleteProjet_WhenProjetNotExists_ShouldThrowResponseStatusException() {
        when(projetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projetService.deleteProjet(99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.NOT_FOUND, "Projet non trouvé");
    }

    // ==================== getProjetsBySiteName ====================

    @Test
    void getProjetsBySiteName_WhenSiteExists_ShouldReturnProjets() {
        when(siteRepository.findByName("MH1")).thenReturn(Optional.of(testSite));

        List<ProjetDto> result = projetService.getProjetsBySiteName("MH1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("FORD");
        verify(siteRepository).findByName("MH1");
    }

    @Test
    void getProjetsBySiteName_WhenSiteNotExists_ShouldThrowResponseStatusException() {
        when(siteRepository.findByName("NonExistentSite")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projetService.getProjetsBySiteName("NonExistentSite"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ==================== getActiveProjets ====================

    @Test
    void getActiveProjets_ShouldReturnOnlyActiveProjets() {
        when(projetRepository.findAllByActiveTrue()).thenReturn(List.of(testProjet1, testProjet2));

        List<ProjetDto> result = projetService.getActiveProjets();

        assertThat(result).hasSize(2);
        verify(projetRepository).findAllByActiveTrue();
    }

    @Test
    void getActiveProjets_WhenNoActiveProjets_ShouldReturnEmptyList() {
        when(projetRepository.findAllByActiveTrue()).thenReturn(Collections.emptyList());

        List<ProjetDto> result = projetService.getActiveProjets();

        assertThat(result).isEmpty();
    }
}