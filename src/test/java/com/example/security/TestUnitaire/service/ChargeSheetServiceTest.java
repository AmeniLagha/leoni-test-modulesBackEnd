package com.example.security.TestUnitaire.service;

import com.example.security.cahierdeCharge.*;
import com.example.security.email.GlobalNotificationService;
import com.example.security.reception.*;
import com.example.security.site.Site;
import com.example.security.user.Role;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
class ChargeSheetServiceTest {

    @Mock
    private ChargeSheetRepository repository;

    @Mock
    private ChargeSheetItemRepository itemRepository;

    @Mock
    private GlobalNotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReceptionHistoryRepository receptionHistoryRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ChargeSheetService chargeSheetService;



    private User ingUser;
    private User ptUser;
    private User adminUser;
    private ChargeSheet testSheet;
    private ChargeSheetItem testItem;
    private Site testSite;

    @BeforeEach
    void setUp() {
        testSite = new Site();
        testSite.setId(1L);
        testSite.setName("MH1");
        testSite.setActive(true);

        ingUser = User.builder()
                .id(1)
                .email("ing@test.com")
                .firstname("Ing")
                .lastname("User")
                .role(Role.ING)
                .build();
        ReflectionTestUtils.setField(ingUser, "site", null);
        ReflectionTestUtils.setField(ingUser, "projets", Set.of());

        ptUser = User.builder()
                .id(2)
                .email("pt@test.com")
                .firstname("Pt")
                .lastname("User")
                .role(Role.PT)
                .build();

        adminUser = User.builder()
                .id(3)
                .email("admin@test.com")
                .firstname("Admin")
                .lastname("User")
                .role(Role.ADMIN)
                .build();

        testSheet = ChargeSheet.builder()
                .id(1L)
                .plant("MH1")
                .project("FORD")
                .harnessRef("HARNESS-001")
                .issuedBy("John Doe")
                .emailAddress("john@test.com")
                .phoneNumber("123456789")
                .orderNumber("ORD-001")
                .costCenterNumber("CC-001")
                .date(LocalDate.now())
                .preferredDeliveryDate(LocalDate.now().plusDays(30))
                .status(ChargeSheetStatus.DRAFT)  // ← Changé pour le test de statut valide
                .createdBy(ingUser.getEmail())
                .createdAt(LocalDate.now())
                .items(new ArrayList<>())
                .build();

        testItem = ChargeSheetItem.builder()
                .id(1L)
                .chargeSheet(testSheet)
                .itemNumber("1")
                .quantityOfTestModules(10)
                .itemStatus("DRAFT")
                .build();

        testSheet.getItems().add(testItem);
    }

    // Helper method to set authentication context
    private void setAuthUser(User user) {
        Authentication auth = new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of();
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return user;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            }

            @Override
            public String getName() {
                return user.getEmail();
            }
        };
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    // ==================== Tests CRUD ====================

    @Test
    void getChargeSheetById_WhenExists_ShouldReturnSheet() {
        setAuthUser(ingUser);
        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));

        ChargeSheet result = chargeSheetService.getChargeSheetById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getChargeSheetById_WhenNotExists_ShouldThrowException() {
        setAuthUser(ingUser);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chargeSheetService.getChargeSheetById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Charge sheet not found");
    }

    @Test
    void getChargeSheetComplete_WhenExists_ShouldReturnCompleteDto() {
        setAuthUser(ingUser);
        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));

        ChargeSheetDto.CompleteDto result =chargeSheetService.getChargeSheetComplete(1L);

        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo("ORD-001");
    }

    // ==================== Tests Validation ====================

    @Test
    void validateByIng_WhenDraft_ShouldValidate() {
        setAuthUser(ingUser);
        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(repository.save(any(ChargeSheet.class))).thenAnswer(inv -> inv.getArgument(0));

        ChargeSheet result = chargeSheetService.validateByIng(1L);

        assertThat(result.getStatus()).isEqualTo(ChargeSheetStatus.VALIDATED_ING);
        verify(repository).save(any(ChargeSheet.class));
    }

    @Test
    void validateByIng_WhenNotDraft_ShouldThrowException() {
        setAuthUser(ingUser);
        testSheet.setStatus(ChargeSheetStatus.VALIDATED_ING);
        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));

        assertThatThrownBy(() -> chargeSheetService.validateByIng(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Le cahier doit être en mode DRAFT pour être validé par ING");
    }

    @Test
    void validateByPt_WhenTechFilled_ShouldValidate() {
        setAuthUser(ptUser);
        testSheet.setStatus(ChargeSheetStatus.TECH_FILLED);
        testItem.setItemStatus("TECH_FILLED");
        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(repository.save(any(ChargeSheet.class))).thenAnswer(inv -> inv.getArgument(0));

        ChargeSheet result = chargeSheetService.validateByPt(1L);

        assertThat(result.getStatus()).isEqualTo(ChargeSheetStatus.VALIDATED_PT);
    }

    @Test
    void validateByPt_WhenItemsNotFilled_ShouldThrowException() {
        setAuthUser(ptUser);
        testSheet.setStatus(ChargeSheetStatus.VALIDATED_ING);
        testItem.setItemStatus("DRAFT");
        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));

        assertThatThrownBy(() -> chargeSheetService.validateByPt(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Tous les items doivent être remplis avant validation PT");
    }

    // ==================== Tests Workflow ====================

    @Test
    void sendToSupplier_WhenValidatedPt_ShouldSend() {
        setAuthUser(ptUser);
        testSheet.setStatus(ChargeSheetStatus.VALIDATED_PT);
        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(repository.save(any(ChargeSheet.class))).thenAnswer(inv -> inv.getArgument(0));

        ChargeSheet result = chargeSheetService.sendToSupplier(1L);

        assertThat(result.getStatus()).isEqualTo(ChargeSheetStatus.SENT_TO_SUPPLIER);
    }

    @Test
    void sendToSupplier_WhenNotValidatedPt_ShouldThrowException() {
        setAuthUser(ptUser);
        testSheet.setStatus(ChargeSheetStatus.DRAFT);
        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));

        assertThatThrownBy(() -> chargeSheetService.sendToSupplier(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void confirmReception_WhenSentToSupplier_ShouldConfirm() {
        setAuthUser(ptUser);
        testSheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);
        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(repository.save(any(ChargeSheet.class))).thenAnswer(inv -> inv.getArgument(0));

        ChargeSheet result = chargeSheetService.confirmReception(1L);

        assertThat(result.getStatus()).isEqualTo(ChargeSheetStatus.RECEIVED_FROM_SUPPLIER);
    }

    @Test
    void completeChargeSheet_WhenReceived_ShouldComplete() {
        setAuthUser(ptUser);
        testSheet.setStatus(ChargeSheetStatus.RECEIVED_FROM_SUPPLIER);
        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(repository.save(any(ChargeSheet.class))).thenAnswer(inv -> inv.getArgument(0));

        ChargeSheet result = chargeSheetService.completeChargeSheet(1L);

        assertThat(result.getStatus()).isEqualTo(ChargeSheetStatus.COMPLETED);
    }

    // ==================== Tests Item Management ====================

    @Test
    void addItem_ShouldAddItemToSheet() {
        setAuthUser(ingUser);
        ChargeSheetDto.ItemDto newItemDto = ChargeSheetDto.ItemDto.builder()
                .itemNumber("2")
                .samplesExist("Yes")
                .quantityOfTestModules(5)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(repository.save(any(ChargeSheet.class))).thenAnswer(inv -> inv.getArgument(0));

        ChargeSheet result = chargeSheetService.addItem(1L, newItemDto);

        assertThat(result.getItems()).hasSize(2);
    }

    @Test
    void removeItem_ShouldRemoveItemFromSheet() {
        setAuthUser(ingUser);
        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        doNothing().when(itemRepository).delete(testItem);
        when(repository.save(any(ChargeSheet.class))).thenAnswer(inv -> inv.getArgument(0));

        chargeSheetService.removeItem(1L, 1L);

        verify(itemRepository).delete(testItem);
    }

    // ==================== Tests Delete ====================

    @Test
    void deleteChargeSheet_ShouldDeleteSheet() {
        setAuthUser(ingUser);
        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));
        doNothing().when(repository).delete(testSheet);

        chargeSheetService.deleteChargeSheet(1L);

        verify(repository).delete(testSheet);
    }

    // ==================== Tests Dashboard Stats ====================

    @Test
    void getDashboardStats_AsAdmin_ShouldReturnStats() {
        setAuthUser(adminUser);

        when(repository.count()).thenReturn(5L);
        when(repository.countByStatus(ChargeSheetStatus.DRAFT)).thenReturn(2L);
        when(repository.countByStatus(ChargeSheetStatus.VALIDATED_ING)).thenReturn(1L);
        when(repository.countByStatus(ChargeSheetStatus.VALIDATED_PT)).thenReturn(1L);
        when(repository.countByStatus(ChargeSheetStatus.COMPLETED)).thenReturn(1L);
        when(repository.countByStatus(ChargeSheetStatus.TECH_FILLED)).thenReturn(0L);

        Map<String, Object> stats = chargeSheetService.getDashboardStats();

        assertThat(stats.get("totalSheets")).isEqualTo(5L);
        assertThat(stats.get("pendingIng")).isEqualTo(2L);
        assertThat(stats.get("userRole")).isEqualTo("ADMIN");
    }


    // ==================== TESTS DE RÉCEPTION ====================

    @Test
    void prepareReceptionData_WhenStatusSentToSupplier_ShouldReturnData() {
        // Given
        testSheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);
        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(receptionHistoryRepository.findByChargeSheetId(1L)).thenReturn(List.of());

        // When
        ReceptionDto.ReceptionResponseDto result = chargeSheetService.prepareReceptionData(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getChargeSheetId()).isEqualTo(1L);
        assertThat(result.isComplete()).isFalse();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getQuantityOrdered()).isEqualTo(10);
        assertThat(result.getItems().get(0).getQuantityRemaining()).isEqualTo(10);
    }

    @Test
    void prepareReceptionData_WhenStatusNotSentToSupplier_ShouldThrowException() {
        // Given
        testSheet.setStatus(ChargeSheetStatus.DRAFT);
        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));

        // When & Then
        assertThatThrownBy(() -> chargeSheetService.prepareReceptionData(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Le cahier doit être en statut SENT_TO_SUPPLIER");
    }

    @Test
    void prepareReceptionData_WithExistingHistory_ShouldCalculateRemaining() {
        // Given
        testSheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);

        ReceptionHistory existingHistory = ReceptionHistory.builder()
                .item(testItem)
                .quantityReceived(3)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(receptionHistoryRepository.findByChargeSheetId(1L)).thenReturn(List.of(existingHistory));

        // When
        ReceptionDto.ReceptionResponseDto result = chargeSheetService.prepareReceptionData(1L);

        // Then
        assertThat(result.getItems().get(0).getQuantityRemaining()).isEqualTo(7);
    }

    @Test
    void confirmPartialReception_ShouldRecordReception() {
        // Given
        testSheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);

        ReceptionDto.ReceptionRequestDto request = ReceptionDto.ReceptionRequestDto.builder()
                .chargeSheetId(1L)
                .deliveryNoteNumber("DN-001")
                .receptionDate(LocalDate.now().toString())
                .items(List.of(
                        ReceptionDto.ReceptionItemDto.builder()
                                .itemId(1L)
                                .quantityReceived(5)
                                .build()
                ))
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(receptionHistoryRepository.findByChargeSheetId(1L)).thenReturn(List.of());
        when(receptionHistoryRepository.save(any(ReceptionHistory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.save(any(ChargeSheet.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        ReceptionDto.ReceptionResponseDto result = chargeSheetService.confirmPartialReception(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("Réception partielle");
        verify(receptionHistoryRepository).save(any(ReceptionHistory.class));
    }

    @Test
    void confirmPartialReception_WhenComplete_ShouldChangeStatus() {
        // Given
        testSheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);

        ReceptionDto.ReceptionRequestDto request = ReceptionDto.ReceptionRequestDto.builder()
                .chargeSheetId(1L)
                .deliveryNoteNumber("DN-001")
                .receptionDate(LocalDate.now().toString())
                .items(List.of(
                        ReceptionDto.ReceptionItemDto.builder()
                                .itemId(1L)
                                .quantityReceived(10)
                                .build()
                ))
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(receptionHistoryRepository.findByChargeSheetId(1L)).thenReturn(List.of());
        when(receptionHistoryRepository.save(any(ReceptionHistory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.save(any(ChargeSheet.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        ReceptionDto.ReceptionResponseDto result = chargeSheetService.confirmPartialReception(request);

        // Then
        assertThat(result.isComplete()).isTrue();
        assertThat(result.getMessage()).contains("Tous les items ont été reçus");
    }

    @Test
    void confirmPartialReception_WhenQuantityExceedsOrdered_ShouldThrowException() {
        // Given
        testSheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);

        ReceptionDto.ReceptionRequestDto request = ReceptionDto.ReceptionRequestDto.builder()
                .chargeSheetId(1L)
                .deliveryNoteNumber("DN-001")
                .items(List.of(
                        ReceptionDto.ReceptionItemDto.builder()
                                .itemId(1L)
                                .quantityReceived(15)
                                .build()
                ))
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(receptionHistoryRepository.findByChargeSheetId(1L)).thenReturn(List.of());

        // When & Then
        assertThatThrownBy(() -> chargeSheetService.confirmPartialReception(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("dépasse la quantité commandée");
    }

    @Test
    void confirmPartialReception_WhenStatusNotSentToSupplier_ShouldThrowException() {
        // Given
        testSheet.setStatus(ChargeSheetStatus.DRAFT);

        ReceptionDto.ReceptionRequestDto request = ReceptionDto.ReceptionRequestDto.builder()
                .chargeSheetId(1L)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));

        // When & Then
        assertThatThrownBy(() -> chargeSheetService.confirmPartialReception(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Le cahier doit être en statut SENT_TO_SUPPLIER");
    }

    @Test
    void getReceptionHistoryDto_ShouldReturnHistoryList() {
        // Given
        ReceptionHistory history = ReceptionHistory.builder()
                .id(1L)
                .chargeSheet(testSheet)
                .item(testItem)
                .quantityReceived(5)
                .previousTotalReceived(0)
                .newTotalReceived(5)
                .quantityOrdered(10)
                .deliveryNoteNumber("DN-001")
                .receptionDate(LocalDate.now())
                .receivedBy("user@test.com")
                .comments("Test comment")
                .createdAt(LocalDate.now())
                .build();

        when(receptionHistoryRepository.findByChargeSheetIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(history));

        // When
        List<ReceptionHistoryDto> result = chargeSheetService.getReceptionHistoryDto(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeliveryNoteNumber()).isEqualTo("DN-001");
        assertThat(result.get(0).getQuantityReceived()).isEqualTo(5);
        assertThat(result.get(0).getItem().getItemNumber()).isEqualTo("1");
    }

    @Test
    void getReceptionHistoryDto_WhenNoHistory_ShouldReturnEmptyList() {
        // Given
        when(receptionHistoryRepository.findByChargeSheetIdOrderByCreatedAtDesc(1L))
                .thenReturn(Collections.emptyList());

        // When
        List<ReceptionHistoryDto> result = chargeSheetService.getReceptionHistoryDto(1L);

        // Then
        assertThat(result).isEmpty();
    }
    // ==================== TESTS STATISTIQUES MENSUELLES ====================

    @Test
    void getMonthlyCreationStats_AsAdmin_ShouldReturnAllStats() {
        setAuthUser(adminUser);

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"2024-03", 15L});
        mockResults.add(new Object[]{"2024-02", 10L});
        mockResults.add(new Object[]{"2024-01", 5L});

        when(repository.countByMonthForAllProjects()).thenReturn(mockResults);

        MonthlyStatsDto result = chargeSheetService.getMonthlyCreationStats(null, 6);

        assertThat(result).isNotNull();
        assertThat(result.getMonthlyCounts()).isNotEmpty();
        assertThat(result.getCurrentMonth()).isEqualTo("2024-03");
        assertThat(result.getPreviousMonth()).isEqualTo("2024-02");
        assertThat(result.getVariationPercentage()).isEqualTo(50.0);
        assertThat(result.getTrend()).isEqualTo("hausse");
    }

    @Test
    void getMonthlyCreationStats_AsUser_ShouldReturnFilteredStats() {
        setAuthUser(ingUser);

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"2024-03", 8L});
        mockResults.add(new Object[]{"2024-02", 6L});

        when(repository.countByMonthForProject("FORD")).thenReturn(mockResults);

        MonthlyStatsDto result = chargeSheetService.getMonthlyCreationStats("FORD", 3);

        assertThat(result).isNotNull();
        assertThat(result.getMonthlyCounts()).hasSize(2);
    }

    @Test
    void getMonthlyCreationStats_WithNoData_ShouldReturnEmpty() {
        setAuthUser(ingUser);

        when(repository.countByMonthForProject("FORD")).thenReturn(Collections.emptyList());

        MonthlyStatsDto result = chargeSheetService.getMonthlyCreationStats("FORD", 6);

        assertThat(result.getMonthlyCounts()).isEmpty();
    }

    @Test
    void getLastTwoMonthsVariation_WithData_ShouldCalculateCorrectly() {
        setAuthUser(adminUser);

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"2024-03", 15L});
        mockResults.add(new Object[]{"2024-02", 10L});

        when(repository.countByMonthForAllProjects()).thenReturn(mockResults);

        Map<String, Object> result = chargeSheetService.getLastTwoMonthsVariation(null);

        assertThat(result.get("currentMonth")).isEqualTo("2024-03");
        assertThat(result.get("currentMonthCount")).isEqualTo(15L);
        assertThat(result.get("previousMonth")).isEqualTo("2024-02");
        assertThat(result.get("previousMonthCount")).isEqualTo(10L);
        assertThat(result.get("variation")).isEqualTo(50.0);
        assertThat(result.get("trend")).isEqualTo("hausse");
    }

    @Test
    void getLastTwoMonthsVariation_WithInsufficientData_ShouldReturnMessage() {
        setAuthUser(adminUser);

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"2024-03", 15L});

        when(repository.countByMonthForAllProjects()).thenReturn(mockResults);

        Map<String, Object> result = chargeSheetService.getLastTwoMonthsVariation(null);

        assertThat(result.get("message")).isEqualTo("Pas assez de données pour calculer la variation");
    }

    @Test
    void getVariationBetweenMonths_ShouldCalculateCorrectly() {
        setAuthUser(adminUser);

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"2024-03", 15L});
        mockResults.add(new Object[]{"2024-02", 10L});
        mockResults.add(new Object[]{"2024-01", 5L});

        when(repository.countByMonthForAllProjects()).thenReturn(mockResults);

        Map<String, Object> result = chargeSheetService.getVariationBetweenMonths(null, "2024-01", "2024-03");

        assertThat(result.get("month1Count")).isEqualTo(5L);
        assertThat(result.get("month2Count")).isEqualTo(15L);
        assertThat(result.get("variation")).isEqualTo(200.0);
        assertThat(result.get("trend")).isEqualTo("hausse");
    }

    @Test
    void getVariationBetweenMonths_WhenMonth1Zero_ShouldHandleCorrectly() {
        setAuthUser(adminUser);

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"2024-03", 15L});
        mockResults.add(new Object[]{"2024-01", 0L});

        when(repository.countByMonthForAllProjects()).thenReturn(mockResults);

        Map<String, Object> result = chargeSheetService.getVariationBetweenMonths(null, "2024-01", "2024-03");

        assertThat(result.get("variation")).isEqualTo(100.0);
        assertThat(result.get("trend")).isEqualTo("hausse");
    }

// ==================== TESTS REVERT TO ING ====================

    @Test
    void revertToIng_WithValidReason_ShouldRevertToDraft() {
        setAuthUser(ptUser);
        testSheet.setStatus(ChargeSheetStatus.VALIDATED_ING);

        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(itemRepository.save(any(ChargeSheetItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.save(any(ChargeSheet.class))).thenAnswer(inv -> inv.getArgument(0));

        ChargeSheet result = chargeSheetService.revertToIng(1L, "Corrections nécessaires");

        assertThat(result.getStatus()).isEqualTo(ChargeSheetStatus.DRAFT);
        assertThat(result.getItems().get(0).getItemStatus()).isEqualTo("DRAFT");
    }

    @Test
    void revertToIng_WithoutReason_ShouldThrowException() {
        setAuthUser(ptUser);

        assertThatThrownBy(() -> chargeSheetService.revertToIng(1L, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Veuillez indiquer la raison du retour à ING");
    }

    @Test
    void revertToIng_WithInvalidUser_ShouldThrowException() {
        setAuthUser(ingUser);

        // ✅ Ajouter ce mock pour que findById ne retourne pas Optional.empty()
        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));

        assertThatThrownBy(() -> chargeSheetService.revertToIng(1L, "Raison"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Seul PT ou ADMIN");
    }

    @Test
    void revertToIng_WithInvalidStatus_ShouldThrowException() {
        setAuthUser(ptUser);
        testSheet.setStatus(ChargeSheetStatus.DRAFT);

        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));

        assertThatThrownBy(() -> chargeSheetService.revertToIng(1L, "Raison valide"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("VALIDATED_ING ou TECH_FILLED");
    }
    // ==================== TESTS POUR LES BRANCHES NULL DANS UPDATEITEMFROMDTO ====================

    @Test
    void updateTechnicalFields_WithNullFields_ShouldOnlyUpdateNonNullFields() {
        setAuthUser(ptUser);
        testSheet.setStatus(ChargeSheetStatus.VALIDATED_ING);

        ChargeSheetDto.UpdateTechDto dto = ChargeSheetDto.UpdateTechDto.builder()
                .housingReferenceLeoni(null)
                .quantityOfTestModules(null)
                .outsideHousingExist("*")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(ChargeSheetItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.save(any(ChargeSheet.class))).thenAnswer(inv -> inv.getArgument(0));

        ChargeSheetItem result = chargeSheetService.updateTechnicalFields(1L, 1L, dto);

        assertThat(result.getOutsideHousingExist()).isEqualTo("*");
        // Les champs null ne doivent pas écraser les valeurs existantes
    }

// ==================== TESTS POUR LES BRANCHES D'EXCEPTION ====================

    @Test
    void updateTechnicalFields_WithItemNotBelongingToSheet_ShouldThrowException() {
        setAuthUser(ptUser);

        ChargeSheet otherSheet = ChargeSheet.builder()
                .id(2L)
                .plant("OTHER")
                .project("OTHER")
                .status(ChargeSheetStatus.DRAFT)
                .build();

        ChargeSheetItem otherItem = ChargeSheetItem.builder()
                .id(2L)
                .chargeSheet(otherSheet)
                .build();

        ChargeSheetDto.UpdateTechDto dto = ChargeSheetDto.UpdateTechDto.builder()
                .housingReferenceLeoni("REF")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(otherItem));

        assertThatThrownBy(() -> chargeSheetService.updateTechnicalFields(1L, 2L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void addItem_WhenSheetNotFound_ShouldThrowException() {
        setAuthUser(ingUser);
        ChargeSheetDto.ItemDto itemDto = ChargeSheetDto.ItemDto.builder().build();

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chargeSheetService.addItem(99L, itemDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

// ==================== TESTS POUR LA MISE À JOUR DU STATUT GLOBAL ====================

    @Test
    void updateTechnicalFields_WhenAllItemsFilled_ShouldUpdateGlobalStatusToTechFilled() {
        setAuthUser(ptUser);
        testSheet.setStatus(ChargeSheetStatus.VALIDATED_ING);
        testItem.setItemStatus("DRAFT");

        ChargeSheetDto.UpdateTechDto dto = ChargeSheetDto.UpdateTechDto.builder()
                .outsideHousingExist("*")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(ChargeSheetItem.class))).thenAnswer(inv -> {
            ChargeSheetItem saved = inv.getArgument(0);
            saved.setItemStatus("TECH_FILLED");
            return saved;
        });
        when(repository.save(any(ChargeSheet.class))).thenAnswer(inv -> inv.getArgument(0));

        ChargeSheetItem result = chargeSheetService.updateTechnicalFields(1L, 1L, dto);

        // Vérifier que le statut global a changé
        verify(repository).save(argThat(sheet ->
                sheet.getStatus() == ChargeSheetStatus.TECH_FILLED
        ));
    }
}