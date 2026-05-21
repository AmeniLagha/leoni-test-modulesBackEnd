package com.example.security.TestUnitaire.repository;

import com.example.security.cahierdeCharge.*;
import com.example.security.projet.Projet;
import com.example.security.projet.ProjetRepository;
import com.example.security.site.Site;
import com.example.security.site.SiteRepository;
import com.example.security.user.Role;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.example.security.auth.AuthenticationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChargeSheetRepositoryTest {

    @Autowired
    private ChargeSheetRepository chargeSheetRepository;

    @Autowired
    private ChargeSheetItemRepository itemRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private ProjetRepository projetRepository;

    @Autowired
    private UserRepository userRepository;

    // Mock du service pour éviter l'erreur
    @MockBean
    private AuthenticationService authenticationService;

    private Site testSite;
    private Projet testProjet;
    private User testUser;
    private ChargeSheet testChargeSheet;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        chargeSheetRepository.deleteAll();
        userRepository.deleteAll();
        siteRepository.deleteAll();
        projetRepository.deleteAll();

        testSite = new Site();
        testSite.setName("MH1");
        testSite.setActive(true);
        testSite = siteRepository.save(testSite);

        testProjet = new Projet();
        testProjet.setName("FORD");
        testProjet.setActive(true);
        testProjet = projetRepository.save(testProjet);

        testUser = User.builder()
                .email("test@test.com")
                .password("password")
                .firstname("Test")
                .lastname("User")
                .matricule(12345)
                .role(Role.ING)
                .site(testSite)
                .projets(Set.of(testProjet))
                .build();
        testUser = userRepository.save(testUser);

        testChargeSheet = ChargeSheet.builder()
                .plant("MH1")
                .project("FORD")
                .orderNumber("ORD-001")
                .harnessRef("HARNESS-001")
                .status(ChargeSheetStatus.DRAFT)
                .createdBy(testUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        testChargeSheet = chargeSheetRepository.save(testChargeSheet);
    }

    @Test
    void findByProject_ShouldReturnChargeSheets() {
        List<ChargeSheet> result = chargeSheetRepository.findByProject("FORD");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderNumber()).isEqualTo("ORD-001");
    }

    // ... reste des tests inchangés

    @Test
    void findByProjectAndStatus_ShouldReturnFiltered() {
        List<ChargeSheet> result = chargeSheetRepository.findByProjectAndStatus("FORD", ChargeSheetStatus.DRAFT);

        assertThat(result).isNotEmpty();
    }

    @Test
    void findByProjectAndStatusIn_ShouldReturnFiltered() {
        List<ChargeSheet> result = chargeSheetRepository.findByProjectAndStatusIn(
                "FORD", List.of(ChargeSheetStatus.DRAFT, ChargeSheetStatus.VALIDATED_ING));

        assertThat(result).isNotEmpty();
    }

    @Test
    void countByProject_ShouldReturnCount() {
        long count = chargeSheetRepository.countByProject("FORD");

        assertThat(count).isEqualTo(1);
    }

    @Test
    void countByProjectAndStatus_ShouldReturnCount() {
        long count = chargeSheetRepository.countByProjectAndStatus("FORD", ChargeSheetStatus.DRAFT);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void findByProjectAndPlant_ShouldReturnFiltered() {
        List<ChargeSheet> result = chargeSheetRepository.findByProjectAndPlant("FORD", "MH1");

        assertThat(result).isNotEmpty();
    }

    @Test
    void findByProjectAndPlantAndStatus_ShouldReturnFiltered() {
        List<ChargeSheet> result = chargeSheetRepository.findByProjectAndPlantAndStatus(
                "FORD", "MH1", ChargeSheetStatus.DRAFT);

        assertThat(result).isNotEmpty();
    }

    @Test
    void countByProjectAndPlant_ShouldReturnCount() {
        long count = chargeSheetRepository.countByProjectAndPlant("FORD", "MH1");

        assertThat(count).isEqualTo(1);
    }

    @Test
    void countByStatus_ShouldReturnCount() {
        long count = chargeSheetRepository.countByStatus(ChargeSheetStatus.DRAFT);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void findByProjectInAndPlant_ShouldReturnFiltered() {
        List<ChargeSheet> result = chargeSheetRepository.findByProjectInAndPlant(
                List.of("FORD"), "MH1");

        assertThat(result).isNotEmpty();
    }

    @Test
    void findByProjectInAndPlantAndStatusIn_ShouldReturnFiltered() {
        List<ChargeSheet> result = chargeSheetRepository.findByProjectInAndPlantAndStatusIn(
                List.of("FORD"), "MH1",
                List.of(ChargeSheetStatus.DRAFT, ChargeSheetStatus.VALIDATED_ING));
        assertThat(result).isNotEmpty();
    }

    @Test
    void findByProjectInAndPlantAndStatus_ShouldReturnFiltered() {
        List<ChargeSheet> result = chargeSheetRepository.findByProjectInAndPlantAndStatus(
                List.of("FORD"), "MH1", ChargeSheetStatus.DRAFT);
        assertThat(result).isNotEmpty();
    }

    @Test
    void countByProjectAndPlantAndStatus_ShouldReturnCount() {
        long count = chargeSheetRepository.countByProjectAndPlantAndStatus(
                "FORD", "MH1", ChargeSheetStatus.DRAFT);
        assertThat(count).isEqualTo(1);
    }
}