package com.example.security.TestUnitaire.service;

import com.example.security.cahierdeCharge.*;
import com.example.security.email.GlobalNotificationService;
import com.example.security.reception.ReceptionHistoryRepository;
import com.example.security.user.Role;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @BeforeEach
    void setUp() {
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

        // ⚠️ CORRECTION : S'assurer que testSheet a toutes ses propriétés
        testSheet = ChargeSheet.builder()
                .id(1L)
                .plant("MH1")
                .project("FORD")
                .harnessRef("HARNESS-001")
                .issuedBy("John Doe")
                .emailAddress("john@test.com")
                .phoneNumber("123456789")
                .orderNumber("ORD-001")  // ✅ Déjà présent
                .costCenterNumber("CC-001")
                .date(LocalDate.now())
                .preferredDeliveryDate(LocalDate.now().plusDays(30))
                .status(ChargeSheetStatus.DRAFT)
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

        ChargeSheetDto.CompleteDto result = chargeSheetService.getChargeSheetComplete(1L);

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

    // Helper method to set authentication context - sans stubbing inutile
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
}