package com.example.security.TestUnitaire.service;

import com.example.security.cahierdeCharge.*;
import com.example.security.conformité.*;
import com.example.security.email.GlobalNotificationService;
import com.example.security.projet.Projet;
import com.example.security.site.Site;
import com.example.security.user.Role;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ComplianceServiceTest {

    @Mock
    private ComplianceRepository repository;

    @Mock
    private ChargeSheetItemRepository chargeSheetItemRepository;

    @Mock
    private GlobalNotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ComplianceReminderService reminderService;

    @InjectMocks
    private ComplianceService complianceService;

    private User ppUser;
    private User adminUser;
    private User mcUser;
    private User ptUser;
    private ChargeSheet testSheet;
    private ChargeSheetItem testItem;
    private Compliance testCompliance;

    @BeforeEach
    void setUp() {
        // 1. Créer un site d'abord
        Site testSite = Site.builder()
                .id(1L)
                .name("MH1")
                .active(true)
                .build();

        // 2. Créer un projet
        Projet testProjet = Projet.builder()
                .id(1L)
                .name("FORD")
                .active(true)
                .build();

        // 3. Créer l'utilisateur PP avec site et projets
        ppUser = User.builder()
                .id(1)
                .email("pp@test.com")
                .firstname("PP")
                .lastname("User")
                .role(Role.PP)
                .site(testSite)                    // ✅ Ajouter le site
                .projets(Set.of(testProjet))       // ✅ Ajouter les projets
                .build();

        // 4. Utilisateur PT pour les tests de rôles
        ptUser = User.builder()
                .id(2)
                .email("pt@test.com")
                .firstname("PT")
                .lastname("User")
                .role(Role.PT)
                .site(testSite)
                .projets(Set.of(testProjet))
                .build();

        // 5. Utilisateur MC pour les tests de rôles
        mcUser = User.builder()
                .id(3)
                .email("mc@test.com")
                .firstname("MC")
                .lastname("User")
                .role(Role.MC)
                .site(testSite)
                .projets(Set.of(testProjet))
                .build();

        // 6. Utilisateur ADMIN (pas besoin de site/projet spécifique)
        adminUser = User.builder()
                .id(4)
                .email("admin@test.com")
                .firstname("Admin")
                .lastname("User")
                .role(Role.ADMIN)
                .build();

        testSheet = ChargeSheet.builder()
                .id(1L)
                .plant("MH1")
                .project("FORD")
                .orderNumber("ORD-001")
                .status(ChargeSheetStatus.VALIDATED_PT)
                .build();

        testItem = ChargeSheetItem.builder()
                .id(1L)
                .chargeSheet(testSheet)
                .itemNumber("1")
                .quantityOfTestModules(5)
                .build();

        testCompliance = Compliance.builder()
                .id(1L)
                .item(testItem)
                .chargeSheetId(1L)
                .orderNumber("ORD-001")
                .indexValue(1)
                .qualifiedTestModule(true)
                .createdBy(ppUser.getEmail())
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(ppUser);
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        // ✅ FORCER l'injection du reminderService
        ReflectionTestUtils.setField(complianceService, "reminderService", reminderService);
    }
    private Projet createProjet(String name) {
        Projet projet = new Projet();
        projet.setId(99L);
        projet.setName(name);
        projet.setActive(true);
        return projet;
    }

    @Test
    void createCompliance_ShouldCreateAndNotify() {
        ComplianceDto.CreateDto dto = ComplianceDto.CreateDto.builder()
                .itemId(1L)
                .orderNumber("ORD-002")
                .orderitemNumber("ITEM-001")
                .testDateTime(LocalDate.now())
                .technicianName("John Doe")
                .qualifiedTestModule(true)
                .build();

        when(chargeSheetItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(repository.save(any(Compliance.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(reminderService).resetRemindersForItem(1L);
        doNothing().when(notificationService).notifyComplianceCreatedToProjectAndSite(any(), anyLong(), anyString(), anyString(), anyString());

        Compliance result = complianceService.createCompliance(dto);

        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo("ORD-002");
        verify(repository).save(any(Compliance.class));
        verify(reminderService).resetRemindersForItem(1L);
        verify(notificationService).notifyComplianceCreatedToProjectAndSite(any(), anyLong(), anyString(), anyString(), anyString());
    }


    @Test
    void createCompliance_WithInvalidItem_ShouldThrowException() {
        ComplianceDto.CreateDto dto = ComplianceDto.CreateDto.builder()
                .itemId(999L)
                .build();

        when(chargeSheetItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> complianceService.createCompliance(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Item not found");
    }

    @Test
    void updateCompliance_ShouldUpdateAndNotify() {
        ComplianceDto.UpdateDto dto = ComplianceDto.UpdateDto.builder()
                .orderitemNumber("UPDATED")
                .qualifiedTestModule(true)
                .remarks("Updated remarks")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testCompliance));
        when(repository.save(any(Compliance.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(notificationService).notifyComplianceUpdatedToProjectAndSite(anyLong(), anyLong(), anyString(), anyString(), anyString());

        Compliance result = complianceService.updateCompliance(1L, dto);

        assertThat(result).isNotNull();
        assertThat(result.getOrderitemNumber()).isEqualTo("UPDATED");
        verify(repository).save(any(Compliance.class));
    }

    @Test
    void updateCompliance_WithInvalidId_ShouldThrowException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> complianceService.updateCompliance(999L, new ComplianceDto.UpdateDto()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Compliance not found");
    }

    @Test
    void deleteCompliance_ShouldDeleteAndNotify() {
        when(repository.findById(1L)).thenReturn(Optional.of(testCompliance));
        doNothing().when(repository).deleteById(1L);
        doNothing().when(notificationService).notifyComplianceDeletedToProjectAndSite(anyString(), anyLong(), anyLong(), anyString(), anyString(), anyString());

        complianceService.deleteCompliance(1L);

        verify(repository).deleteById(1L);
        verify(notificationService).notifyComplianceDeletedToProjectAndSite(anyString(), anyLong(), anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void getComplianceById_WhenExists_ShouldReturn() {
        when(repository.findById(1L)).thenReturn(Optional.of(testCompliance));

        Compliance result = complianceService.getComplianceById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getComplianceById_WhenNotExists_ShouldThrowException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> complianceService.getComplianceById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Compliance not found");
    }

    @Test
    void getComplianceByChargeSheetId_ShouldReturnList() {
        when(repository.findByChargeSheetId(1L)).thenReturn(List.of(testCompliance));

        List<Compliance> result = complianceService.getComplianceByChargeSheetId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllCompliance_ShouldReturnAll() {
        when(repository.findAll()).thenReturn(List.of(testCompliance));

        List<Compliance> result = complianceService.getAllCompliance();

        assertThat(result).hasSize(1);
    }

    @Test
    void createComplianceForItem_ShouldCreateForEachModule() {
        testItem.setQuantityOfTestModules(3);

        when(chargeSheetItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(repository.save(any(Compliance.class))).thenAnswer(inv -> inv.getArgument(0));
        // ✅ Ne pas stuber findByItemId ici car la méthode createComplianceForItem ne l'appelle peut-être pas

        List<Compliance> result = complianceService.createComplianceForItem(testItem);

        assertThat(result).hasSize(3);
        verify(repository, times(3)).save(any(Compliance.class));
    }

    @Test
    void getVariationBetweenMonths_ShouldReturnVariation() {
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"2024-02", 20L});
        mockResults.add(new Object[]{"2024-01", 10L});

        when(repository.countByMonthForProject(anyString())).thenReturn(mockResults);

        Map<String, Object> result = complianceService.getVariationBetweenMonths("FORD", "2024-01", "2024-02");

        assertThat(result.get("month1Count")).isEqualTo(10L);
        assertThat(result.get("month2Count")).isEqualTo(20L);
        assertThat(result.get("variation")).isEqualTo(100.0);
        assertThat(result.get("trend")).isEqualTo("hausse");
    }
    @Test
    void getAllComplianceForDisplay_AsAdmin_ShouldReturnAllCompliances() {
        // Given - admin user
        User adminUser = User.builder()
                .id(2)
                .email("admin@test.com")
                .role(Role.ADMIN)
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(adminUser);
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        when(repository.findAll()).thenReturn(List.of(testCompliance));

        List<ComplianceDisplayDto> result = complianceService.getAllComplianceForDisplay();

        assertThat(result).hasSize(1);
    }
    @Test
    void getAllComplianceForDisplay_WhenItemIsNull_ShouldFilterOut() {
        testCompliance.setItem(null);

        when(repository.findAll()).thenReturn(List.of(testCompliance));

        List<ComplianceDisplayDto> result = complianceService.getAllComplianceForDisplay();

        assertThat(result).isEmpty();
    }
    @Test
    void getAllComplianceForDisplay_AsPt_ShouldFilterByStatus() {
        // ✅ Utiliser ptUser déjà créé dans setUp()
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(ptUser);  // ← plus besoin de builder
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        testSheet.setStatus(ChargeSheetStatus.DRAFT);
        when(repository.findByProject("FORD")).thenReturn(List.of(testCompliance));

        List<ComplianceDisplayDto> result = complianceService.getAllComplianceForDisplay();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllComplianceForDisplay_AsMc_ShouldOnlySeeCompleted() {
        // ✅ Utiliser mcUser déjà créé dans setUp()
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(mcUser);  // ← plus besoin de builder
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        // Cas 1: COMPLETED → inclus
        testSheet.setStatus(ChargeSheetStatus.COMPLETED);
        when(repository.findByProject("FORD")).thenReturn(List.of(testCompliance));

        List<ComplianceDisplayDto> result = complianceService.getAllComplianceForDisplay();
        assertThat(result).hasSize(1);

        // Cas 2: VALIDATED_PT → exclus
        testSheet.setStatus(ChargeSheetStatus.VALIDATED_PT);
        result = complianceService.getAllComplianceForDisplay();
        assertThat(result).isEmpty();
    }
    @Test
    void getVariationBetweenMonths_WhenFirstMonthZero_ShouldReturn100Percent() {
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"2024-02", 10L});
        mockResults.add(new Object[]{"2024-01", 0L});

        when(repository.countByMonthForProject("FORD")).thenReturn(mockResults);

        Map<String, Object> result = complianceService.getVariationBetweenMonths("FORD", "2024-01", "2024-02");

        assertThat(result.get("variation")).isEqualTo(100.0);
        assertThat(result.get("trend")).isEqualTo("hausse");
        // ✅ Correction : caster en String
        assertThat((String) result.get("formula")).contains("Création depuis zéro");
    }
    @Test
    void getVariationBetweenMonths_WhenBothMonthsZero_ShouldReturnZero() {
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"2024-02", 0L});
        mockResults.add(new Object[]{"2024-01", 0L});

        when(repository.countByMonthForProject("FORD")).thenReturn(mockResults);

        Map<String, Object> result = complianceService.getVariationBetweenMonths("FORD", "2024-01", "2024-02");

        assertThat(result.get("variation")).isEqualTo(0.0);
        assertThat(result.get("formula")).isEqualTo("Aucune conformité sur les deux mois");
    }
    @Test
    void getLastTwoMonthsVariation_WhenInsufficientData_ShouldReturnMessage() {
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"2024-02", 10L}); // seulement 1 mois

        when(repository.countByMonthForProject("FORD")).thenReturn(mockResults);

        Map<String, Object> result = complianceService.getLastTwoMonthsVariation("FORD");

        assertThat(result.get("message")).isEqualTo("Pas assez de données pour calculer la variation");
    }
    @Test
    void getAllComplianceForDisplay_AsPp_ShouldFilterByStatus() {
        // ✅ Créer un objet Site
        Site testSite = new Site();
        testSite.setId(10L);
        testSite.setName("MH1");
        testSite.setActive(true);

        // ✅ Créer un objet Projet
        Projet testProjet = new Projet();
        testProjet.setId(10L);
        testProjet.setName("FORD");
        testProjet.setActive(true);

        User ppUser2 = User.builder()
                .id(10)
                .email("pp2@test.com")
                .role(Role.PP)
                .site(testSite)                    // ✅ Objet Site
                .projets(Set.of(testProjet))       // ✅ Set de Projet
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(ppUser2);
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        testSheet.setStatus(ChargeSheetStatus.VALIDATED_PT);
        when(repository.findByProject("FORD")).thenReturn(List.of(testCompliance));

        List<ComplianceDisplayDto> result = complianceService.getAllComplianceForDisplay();

        assertThat(result).hasSize(1);
    }
    @Test
    void getAllComplianceForDisplay_AsIng_ShouldSeeAll() {

        // ✅ Créer un objet Site
        Site testSite = new Site();
        testSite.setId(10L);
        testSite.setName("MH1");
        testSite.setActive(true);

        // ✅ Créer un objet Projet
        Projet testProjet = new Projet();
        testProjet.setId(10L);
        testProjet.setName("FORD");
        testProjet.setActive(true);
        User ingUser2 = User.builder()
                .id(11)
                .email("ing2@test.com")
                .role(Role.ING)
                .site(testSite)                    // ✅ Objet Site
                .projets(Set.of(testProjet))
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(ingUser2);
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        testSheet.setStatus(ChargeSheetStatus.DRAFT);
        when(repository.findByProject("FORD")).thenReturn(List.of(testCompliance));

        List<ComplianceDisplayDto> result = complianceService.getAllComplianceForDisplay();

        assertThat(result).hasSize(1);
    }
    @Test
    void getAllComplianceForDisplay_AsMp_ShouldOnlySeeCompleted() {
        // ✅ Créer un objet Site
        Site testSite = new Site();
        testSite.setId(10L);
        testSite.setName("MH1");
        testSite.setActive(true);

        // ✅ Créer un objet Projet
        Projet testProjet = new Projet();
        testProjet.setId(10L);
        testProjet.setName("FORD");
        testProjet.setActive(true);
        User mpUser2 = User.builder()
                .id(12)
                .email("mp@test.com")
                .role(Role.MP)
                .site(testSite)                    // ✅ Objet Site
                .projets(Set.of(testProjet))
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(mpUser2);
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        testSheet.setStatus(ChargeSheetStatus.COMPLETED);
        when(repository.findByProject("FORD")).thenReturn(List.of(testCompliance));

        List<ComplianceDisplayDto> result = complianceService.getAllComplianceForDisplay();
        assertThat(result).hasSize(1);

        testSheet.setStatus(ChargeSheetStatus.VALIDATED_PT);
        result = complianceService.getAllComplianceForDisplay();
        assertThat(result).isEmpty();
    }
    @Test
    void getAllComplianceForDisplay_WhenProjetsNamesNull_ShouldReturnEmpty() {
        // ✅ Créer un objet Site
        Site testSite = new Site();
        testSite.setId(10L);
        testSite.setName("MH1");
        testSite.setActive(true);

        // ✅ Créer un objet Projet
        Projet testProjet = new Projet();
        testProjet.setId(10L);
        testProjet.setName("FORD");
        testProjet.setActive(true);
        User userWithNoProjects = User.builder()
                .id(13)
                .email("null@test.com")
                .role(Role.PT)
                .site(testSite)                    // ✅ Objet Site
                // .projets(null) ou omettre
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userWithNoProjects);
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        List<ComplianceDisplayDto> result = complianceService.getAllComplianceForDisplay();

        assertThat(result).isEmpty();
    }
    @Test
    void getAllComplianceForDisplay_WhenChargeSheetItemIsNull_ShouldFilterOut() {
        testCompliance.setItem(null);

        when(repository.findAll()).thenReturn(List.of(testCompliance));

        List<ComplianceDisplayDto> result = complianceService.getAllComplianceForDisplay();

        assertThat(result).isEmpty();
    }
    @Test
    void getVariationBetweenMonths_AsAdminWithNullProject_ShouldReturnAll() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(adminUser);
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"2024-02", 20L});
        mockResults.add(new Object[]{"2024-01", 10L});

        when(repository.countByMonthForAllProjects()).thenReturn(mockResults);

        Map<String, Object> result = complianceService.getVariationBetweenMonths(null, "2024-01", "2024-02");

        assertThat(result.get("month1Count")).isEqualTo(10L);
        assertThat(result.get("month2Count")).isEqualTo(20L);
    }
    @Test
    void getVariationBetweenMonths_WhenNoProjectAvailable_ShouldReturnMessage() {
        User userWithNoProjects = User.builder()
                .id(14)
                .email("no-proj@test.com")
                .role(Role.PT)
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userWithNoProjects);
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        Map<String, Object> result = complianceService.getVariationBetweenMonths(null, "2024-01", "2024-02");

        assertThat(result.get("message")).isEqualTo("Aucun projet disponible");
    }
    @Test
    void getLastTwoMonthsVariation_WhenNoProjectAvailable_ShouldReturnMessage() {
        User userWithNoProjects = User.builder()
                .id(15)
                .email("no-proj@test.com")
                .role(Role.PT)
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userWithNoProjects);
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        Map<String, Object> result = complianceService.getLastTwoMonthsVariation(null);

        assertThat(result.get("message")).isEqualTo("Aucun projet disponible");
    }
    @Test
    void getLastTwoMonthsVariation_WhenResultsNull_ShouldReturnMessage() {
        when(repository.countByMonthForProject("FORD")).thenReturn(null);

        Map<String, Object> result = complianceService.getLastTwoMonthsVariation("FORD");

        assertThat(result.get("message")).isEqualTo("Pas assez de données pour calculer la variation");
        assertThat(result.get("availableMonths")).isEqualTo(0);
    }
    @Test
    void getVariationBetweenMonths_WhenNegativeVariation_ShouldReturnBaisse() {
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"2024-02", 5L});
        mockResults.add(new Object[]{"2024-01", 10L});

        when(repository.countByMonthForProject("FORD")).thenReturn(mockResults);

        Map<String, Object> result = complianceService.getVariationBetweenMonths("FORD", "2024-01", "2024-02");

        assertThat(result.get("variation")).isEqualTo(-50.0);
        assertThat(result.get("trend")).isEqualTo("baisse");
    }
    @Test
    void createCompliance_WithPtUser_ShouldStillCreate() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(ptUser);
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        ComplianceDto.CreateDto dto = ComplianceDto.CreateDto.builder()
                .itemId(1L)
                .orderNumber("ORD-003")
                .qualifiedTestModule(true)
                .build();

        when(chargeSheetItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(repository.save(any(Compliance.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(reminderService).resetRemindersForItem(1L);
        doNothing().when(notificationService).notifyComplianceCreatedToProjectAndSite(any(), anyLong(), anyString(), anyString(), anyString());

        Compliance result = complianceService.createCompliance(dto);

        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo("ORD-003");
    }
    @Test
    void updateCompliance_WithNullFields_ShouldNotChange() {
        ComplianceDto.UpdateDto dto = ComplianceDto.UpdateDto.builder().build();

        when(repository.findById(1L)).thenReturn(Optional.of(testCompliance));
        when(repository.save(any(Compliance.class))).thenAnswer(inv -> inv.getArgument(0));

        Compliance result = complianceService.updateCompliance(1L, dto);

        assertThat(result.getOrderitemNumber()).isNull();
    }

}