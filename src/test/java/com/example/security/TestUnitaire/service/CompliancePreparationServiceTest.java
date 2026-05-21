package com.example.security.TestUnitaire.service;

import com.example.security.cahierdeCharge.*;
import com.example.security.conformité.*;
import com.example.security.reception.ReceptionHistory;
import com.example.security.reception.ReceptionHistoryRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // ✅ Évite les UnnecessaryStubbingException
class CompliancePreparationServiceTest {

    @Mock
    private ChargeSheetItemRepository itemRepository;

    @Mock
    private ReceptionHistoryRepository receptionHistoryRepository;

    @Mock
    private ComplianceRepository complianceRepository;

    @InjectMocks
    private CompliancePreparationService compliancePreparationService;

    private ChargeSheetItem testItem;
    private ReceptionHistory reception1;
    private ReceptionHistory reception2;
    private User ppUser;

    @BeforeEach
    void setUp() {
        ppUser = User.builder()
                .id(1)
                .email("pp@test.com")
                .role(Role.PP)
                .build();

        ChargeSheet sheet = ChargeSheet.builder()
                .id(1L)
                .plant("MH1")
                .project("FORD")
                .orderNumber("ORD-001")
                .build();

        testItem = ChargeSheetItem.builder()
                .id(1L)
                .itemNumber("ITEM-001")
                .chargeSheet(sheet)
                .quantityOfTestModules(10)
                .housingReferenceSupplierCustomer("REF_1_N_T")  // ✅ Ajouté pour le test
                .housingReferenceLeoni("LEONI-REF-001")
                .build();

        reception1 = ReceptionHistory.builder()
                .id(1L)
                .item(testItem)
                .quantityReceived(3)
                .previousTotalReceived(0)
                .newTotalReceived(3)
                .receptionDate(LocalDate.now().minusDays(10))
                .createdAt(LocalDate.now().minusDays(10))
                .deliveryNoteNumber("DN-001")
                .build();

        reception2 = ReceptionHistory.builder()
                .id(2L)
                .item(testItem)
                .quantityReceived(2)
                .previousTotalReceived(3)
                .newTotalReceived(5)
                .receptionDate(LocalDate.now().minusDays(5))
                .createdAt(LocalDate.now().minusDays(5))
                .deliveryNoteNumber("DN-002")
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(ppUser);
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    // ==================== TESTS ====================

    @Test
    void prepareComplianceForItem_WhenNoPendingCompliance_ShouldReturnEmpty() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        // ✅ Utiliser ArrayList modifiable au lieu de List.of()
        List<ReceptionHistory> receptions = new ArrayList<>();
        receptions.add(reception1);
        receptions.add(reception2);
        when(receptionHistoryRepository.findByItemId(1L)).thenReturn(receptions);

        List<Compliance> existing = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            existing.add(Compliance.builder().id((long) i).build());
        }
        when(complianceRepository.findByItemId(1L)).thenReturn(existing);

        List<ComplianceDto.PrepareComplianceDto> result = compliancePreparationService.prepareComplianceForItem(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void prepareComplianceForItem_WhenPendingCompliance_ShouldReturnQuantity() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        List<ReceptionHistory> receptions = new ArrayList<>();
        receptions.add(reception1);
        receptions.add(reception2);
        when(receptionHistoryRepository.findByItemId(1L)).thenReturn(receptions);

        List<Compliance> existing = new ArrayList<>();
        existing.add(Compliance.builder().id(1L).build());
        existing.add(Compliance.builder().id(2L).build());
        when(complianceRepository.findByItemId(1L)).thenReturn(existing);

        List<ComplianceDto.PrepareComplianceDto> result = compliancePreparationService.prepareComplianceForItem(1L);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getQuantityToCreate()).isEqualTo(3);
    }

    @Test
    void prepareComplianceForItem_WhenNoReceptions_ShouldReturnEmpty() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(receptionHistoryRepository.findByItemId(1L)).thenReturn(new ArrayList<>());
        when(complianceRepository.findByItemId(1L)).thenReturn(new ArrayList<>());

        List<ComplianceDto.PrepareComplianceDto> result = compliancePreparationService.prepareComplianceForItem(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void prepareComplianceForItem_WhenItemNotFound_ShouldThrow() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> compliancePreparationService.prepareComplianceForItem(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Item not found");
    }

    @Test
    void createComplianceForReceivedQuantity_ShouldCreateCorrectNumberOfSheets() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        List<ReceptionHistory> receptions = new ArrayList<>();
        receptions.add(reception1);
        receptions.add(reception2);
        when(receptionHistoryRepository.findByItemId(1L)).thenReturn(receptions);
        when(complianceRepository.findByItemId(1L)).thenReturn(new ArrayList<>());
        when(complianceRepository.save(any(Compliance.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Compliance> result = compliancePreparationService.createComplianceForReceivedQuantity(1L, 3);

        assertThat(result).hasSize(3);
        verify(complianceRepository, times(3)).save(any(Compliance.class));
    }

    @Test
    void createComplianceForReceivedQuantity_WhenItemNotFound_ShouldThrow() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> compliancePreparationService.createComplianceForReceivedQuantity(999L, 3))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Item not found");
    }
    @Test
    void createComplianceForReceivedQuantity_WithComplexReference_ShouldParseCorrectly() {
        testItem.setHousingReferenceSupplierCustomer("COMPLEX_5_SUPPLIER_TYPE");

        List<ReceptionHistory> receptions = new ArrayList<>();
        receptions.add(reception1);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(receptionHistoryRepository.findByItemId(1L)).thenReturn(receptions);  // ← Liste modifiable
        when(complianceRepository.findByItemId(1L)).thenReturn(new ArrayList<>());  // ← Déjà modifiable
        when(complianceRepository.save(any(Compliance.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Compliance> result = compliancePreparationService.createComplianceForReceivedQuantity(1L, 3);

        assertThat(result).isNotEmpty();
    }

}