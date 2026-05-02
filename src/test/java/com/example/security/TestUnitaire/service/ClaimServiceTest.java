package com.example.security.TestUnitaire.service;

import com.example.security.cahierdeCharge.ChargeSheet;
import com.example.security.cahierdeCharge.ChargeSheetRepository;
import com.example.security.email.GlobalNotificationService;
import com.example.security.projet.Projet;
import com.example.security.reclamation.*;
import com.example.security.site.Site;
import com.example.security.user.Role;
import com.example.security.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // ✅ AJOUTE CETTE LIGNE
class ClaimServiceTest {

    @Mock
    private ClaimRepository repository;

    @Mock
    private GlobalNotificationService notificationService;

    @Mock
    private ChargeSheetRepository chargeSheetRepository;

    @InjectMocks
    private ClaimService claimService;

    private User ppUser;
    private User mcUser;
    private ChargeSheet testSheet;
    private Claim testClaim;

    @BeforeEach
    void setUp() {
        // Création des objets pour les tests
        Site userSite = new Site();
        userSite.setId(1L);
        userSite.setName("MH1");
        userSite.setActive(true);

        Projet userProjet = new Projet();
        userProjet.setId(1L);
        userProjet.setName("FORD");
        userProjet.setActive(true);

        ppUser = User.builder()
                .id(1)
                .email("pp@test.com")
                .firstname("PP")
                .lastname("User")
                .role(Role.PP)
                .site(userSite)
                .projets(Set.of(userProjet))
                .build();

        mcUser = User.builder()
                .id(2)
                .email("mc@test.com")
                .firstname("MC")
                .lastname("User")
                .role(Role.MC)
                .site(userSite)
                .projets(Set.of(userProjet))
                .build();

        testSheet = ChargeSheet.builder()
                .id(1L)
                .plant("MH1")
                .project("FORD")
                .orderNumber("ORD-001")
                .build();

        testClaim = Claim.builder()
                .id(1L)
                .chargeSheetId(1L)
                .title("Test Claim")
                .description("Test description")
                .priority(Claim.Priority.HIGH)
                .category("TEST")
                .status(Claim.ClaimStatus.ASSIGNED)
                .reportedBy(ppUser.getEmail())
                .reportedDate(LocalDate.now())
                .assignedTo(mcUser.getEmail())
                .assignedDate(LocalDate.now())
                .createdBy(ppUser.getEmail())
                .createdAt(LocalDate.now())
                .plant("MH1")
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(ppUser);
        when(auth.getName()).thenReturn(ppUser.getEmail());
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }
    // ==================== Tests CRUD ====================

    @Test
    void createClaim_ShouldCreateAndNotify() {
        ClaimDto.CreateDto dto = ClaimDto.CreateDto.builder()
                .chargeSheetId(1L)
                .title("New Claim")
                .description("Description")
                .priority(Claim.Priority.MEDIUM)
                .category("HARDWARE")
                .assignedTo(mcUser.getEmail())
                .plant("MH1")
                .build();

        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(repository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(notificationService).sendNotificationToOneUser(anyString(), anyString(), anyString());  // ✅ Ajouter

        Claim result = claimService.createClaim(dto);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("New Claim");
        verify(repository).save(any(Claim.class));
    }

    @Test
    void getClaimById_WhenExists_ShouldReturn() {
        when(repository.findById(1L)).thenReturn(Optional.of(testClaim));

        Claim result = claimService.getClaimById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getClaimById_WhenNotExists_ShouldThrowException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> claimService.getClaimById(999L))
                .isInstanceOf(ResponseStatusException.class)  // ✅ Changé
                .extracting("status", "reason")
                .containsExactly(HttpStatus.NOT_FOUND, "Claim not found");  // ✅ Ajouté
    }

    @Test
    void updateClaim_ShouldUpdateAndNotify() {
        ClaimDto.UpdateDto dto = ClaimDto.UpdateDto.builder()
                .title("Updated Title")
                .description("Updated Description")
                .priority(Claim.Priority.CRITICAL)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testClaim));
        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(repository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(notificationService).notifyClaimUpdatedToProjectAndSite(anyLong(), anyString(), anyLong(), anyString(), anyString(), anyString(), anyString());

        Claim result = claimService.updateClaim(1L, dto);

        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getPriority()).isEqualTo(Claim.Priority.CRITICAL);
    }

    @Test
    void deleteClaim_ShouldDeleteAndNotify() {
        when(repository.findById(1L)).thenReturn(Optional.of(testClaim));
        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        doNothing().when(repository).deleteById(1L);
        doNothing().when(notificationService).notifyClaimDeletedToProjectAndSite(anyString(), anyLong(), anyLong(), anyString(), anyString(), anyString());

        claimService.deleteClaim(1L);

        verify(repository).deleteById(1L);
    }

    // ==================== Tests Assignment ====================

    @Test
    void assignClaim_ShouldAssignAndNotify() {
        ClaimDto.AssignmentDto dto = ClaimDto.AssignmentDto.builder()
                .assignedTo(mcUser.getEmail())
                .estimatedResolutionDate(LocalDate.now().plusDays(7))
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testClaim));
        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(repository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(notificationService).sendNotificationToOneUser(anyString(), anyString(), anyString());
        doNothing().when(notificationService).notifyClaimUpdatedToProjectAndSite(anyLong(), anyString(), anyLong(), anyString(), anyString(), anyString(), anyString());

        Claim result = claimService.assignClaim(1L, dto);

        assertThat(result.getAssignedTo()).isEqualTo(mcUser.getEmail());
        assertThat(result.getStatus()).isEqualTo(Claim.ClaimStatus.ASSIGNED);
    }

    // ==================== Tests Resolution ====================

    @Test
    void resolveClaim_ShouldResolve() {
        ClaimDto.ResolutionDto dto = ClaimDto.ResolutionDto.builder()
                .actionTaken("Fixed issue")
                .resolution("Replaced component")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testClaim));
        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(repository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(notificationService).notifyClaimUpdatedToProjectAndSite(anyLong(), anyString(), anyLong(), anyString(), anyString(), anyString(), anyString());

        Claim result = claimService.resolveClaim(1L, dto);

        assertThat(result.getStatus()).isEqualTo(Claim.ClaimStatus.RESOLVED);
        assertThat(result.getActionTaken()).isEqualTo("Fixed issue");
        assertThat(result.getResolution()).isEqualTo("Replaced component");
    }

    // ==================== Tests Status Update ====================

    @Test
    void updateClaimStatus_ShouldUpdateStatus() {
        when(repository.findById(1L)).thenReturn(Optional.of(testClaim));
        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(repository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(notificationService).notifyClaimUpdatedToProjectAndSite(anyLong(), anyString(), anyLong(), anyString(), anyString(), anyString(), anyString());

        Claim result = claimService.updateClaimStatus(1L, Claim.ClaimStatus.IN_PROGRESS);

        assertThat(result.getStatus()).isEqualTo(Claim.ClaimStatus.IN_PROGRESS);
    }

    // ==================== Tests Queries ====================

    @Test
    void getClaimsByChargeSheetId_ShouldReturnList() {
        when(repository.findByChargeSheetId(1L)).thenReturn(List.of(testClaim));

        List<Claim> result = claimService.getClaimsByChargeSheetId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getClaimsByReportedBy_ShouldReturnList() {
        when(repository.findByReportedBy("pp@test.com")).thenReturn(List.of(testClaim));

        List<Claim> result = claimService.getClaimsByReportedBy("pp@test.com");

        assertThat(result).hasSize(1);
    }

    @Test
    void getClaimsByAssignedTo_ShouldReturnList() {
        when(repository.findByAssignedTo("mc@test.com")).thenReturn(List.of(testClaim));

        List<Claim> result = claimService.getClaimsByAssignedTo("mc@test.com");

        assertThat(result).hasSize(1);
    }

    @Test
    void getClaimsByStatus_ShouldReturnList() {
        when(repository.findByStatus(Claim.ClaimStatus.ASSIGNED)).thenReturn(List.of(testClaim));

        List<Claim> result = claimService.getClaimsByStatus(Claim.ClaimStatus.ASSIGNED);

        assertThat(result).hasSize(1);
    }

    @Test
    void getClaimsByPriority_ShouldReturnList() {
        when(repository.findByPriority(Claim.Priority.HIGH)).thenReturn(List.of(testClaim));

        List<Claim> result = claimService.getClaimsByPriority(Claim.Priority.HIGH);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllClaims_ShouldReturnFilteredList() {
        when(repository.findAll()).thenReturn(List.of(testClaim));
        // ✅ Ajouter le mock pour chargeSheetRepository
        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));

        List<Claim> result = claimService.getAllClaims();

        assertThat(result).hasSize(1);
    }

    // ==================== Tests Statistics ====================

    @Test
    void getVariationBetweenMonths_ShouldReturnVariation() {
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"2024-02", 20L});
        mockResults.add(new Object[]{"2024-01", 10L});

        when(repository.countByMonthForProject(anyString())).thenReturn(mockResults);

        Map<String, Object> result = claimService.getVariationBetweenMonths("FORD", "2024-01", "2024-02");

        assertThat(result.get("month1Count")).isEqualTo(10L);
        assertThat(result.get("month2Count")).isEqualTo(20L);
        assertThat(result.get("variation")).isEqualTo(100.0);
        assertThat(result.get("trend")).isEqualTo("hausse");
    }

    @Test
    void getLastTwoMonthsVariation_ShouldReturnVariation() {
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"2024-02", 20L});
        mockResults.add(new Object[]{"2024-01", 10L});

        when(repository.countByMonthForProject(anyString())).thenReturn(mockResults);

        Map<String, Object> result = claimService.getLastTwoMonthsVariation("FORD");

        assertThat(result.get("currentMonthCount")).isEqualTo(20L);
        assertThat(result.get("previousMonthCount")).isEqualTo(10L);
    }
}