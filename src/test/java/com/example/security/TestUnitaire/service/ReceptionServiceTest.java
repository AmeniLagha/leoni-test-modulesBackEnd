package com.example.security.TestUnitaire.service;

import com.example.security.cahierdeCharge.*;
import com.example.security.email.GlobalNotificationService;
import com.example.security.reception.*;
import com.example.security.user.Role;
import com.example.security.user.User;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReceptionServiceTest {

    @Mock
    private ChargeSheetRepository chargeSheetRepository;

    @Mock
    private ChargeSheetItemRepository itemRepository;

    @Mock
    private ReceptionHistoryRepository receptionHistoryRepository;

    @Mock
    private GlobalNotificationService notificationService;

    @Mock
    private EntityManager entityManager;  // ← Le mock est bien déclaré

    @InjectMocks
    private ChargeSheetService chargeSheetService;


    private ChargeSheet testSheet;
    private ChargeSheetItem testItem;
    private ReceptionHistory testHistory;
    private User ptUser;

    @BeforeEach
    void setUp() {
        ptUser = User.builder()
                .id(1)
                .email("pt@test.com")
                .role(Role.PT)
                .build();

        testSheet = ChargeSheet.builder()
                .id(1L)
                .orderNumber("ORD-001")
                .project("FORD")
                .plant("MH1")
                .status(ChargeSheetStatus.SENT_TO_SUPPLIER)
                .build();

        testItem = ChargeSheetItem.builder()
                .id(1L)
                .chargeSheet(testSheet)
                .itemNumber("1")
                .quantityOfTestModules(10)
                .build();

        List<ChargeSheetItem> items = new ArrayList<>();
        items.add(testItem);
        testSheet.setItems(items);

        testHistory = ReceptionHistory.builder()
                .id(1L)
                .chargeSheet(testSheet)
                .item(testItem)
                .quantityReceived(5)
                .previousTotalReceived(0)
                .newTotalReceived(5)
                .quantityOrdered(10)
                .deliveryNoteNumber("DN-001")
                .receptionDate(LocalDate.now())
                .receivedBy("pt@test.com")
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(ptUser);
        when(auth.getName()).thenReturn("pt@test.com");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    // ==================== TESTS PREPARE RECEPTION ====================

    @Test
    void prepareReceptionData_WithValidSheet_ShouldReturnData() {
        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(receptionHistoryRepository.findByChargeSheetId(1L)).thenReturn(Collections.emptyList());

        ReceptionDto.ReceptionResponseDto result = chargeSheetService.prepareReceptionData(1L);

        assertThat(result).isNotNull();
        assertThat(result.getChargeSheetId()).isEqualTo(1L);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getQuantityOrdered()).isEqualTo(10);
        assertThat(result.getItems().get(0).getQuantityRemaining()).isEqualTo(10);
        assertThat(result.isComplete()).isFalse();
    }

    @Test
    void prepareReceptionData_WithExistingHistory_ShouldCalculateRemaining() {
        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(receptionHistoryRepository.findByChargeSheetId(1L)).thenReturn(List.of(testHistory));

        ReceptionDto.ReceptionResponseDto result = chargeSheetService.prepareReceptionData(1L);

        assertThat(result.getItems().get(0).getQuantityRemaining()).isEqualTo(5);
    }

    @Test
    void prepareReceptionData_WithCompleteReception_ShouldReturnComplete() {
        testHistory.setQuantityReceived(10);
        testHistory.setNewTotalReceived(10);

        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(receptionHistoryRepository.findByChargeSheetId(1L)).thenReturn(List.of(testHistory));

        ReceptionDto.ReceptionResponseDto result = chargeSheetService.prepareReceptionData(1L);

        assertThat(result.getItems().get(0).getQuantityRemaining()).isEqualTo(0);
        assertThat(result.isComplete()).isTrue();
    }

    @Test
    void prepareReceptionData_WithInvalidStatus_ShouldThrowException() {
        testSheet.setStatus(ChargeSheetStatus.DRAFT);
        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));

        assertThatThrownBy(() -> chargeSheetService.prepareReceptionData(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SENT_TO_SUPPLIER");
    }

    // ==================== TESTS CONFIRM PARTIAL RECEPTION ====================

    @Test
    void confirmPartialReception_WithValidData_ShouldSaveHistory() {
        testSheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);

        ReceptionDto.ReceptionRequestDto request = ReceptionDto.ReceptionRequestDto.builder()
                .chargeSheetId(1L)
                .deliveryNoteNumber("DN-002")
                .receptionDate(LocalDate.now().toString())
                .items(List.of(
                        ReceptionDto.ReceptionItemDto.builder()
                                .itemId(1L)
                                .quantityReceived(3)
                                .build()
                ))
                .build();

        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(receptionHistoryRepository.findByChargeSheetId(1L)).thenReturn(Collections.emptyList());
        when(receptionHistoryRepository.save(any(ReceptionHistory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(chargeSheetRepository.save(any(ChargeSheet.class))).thenAnswer(inv -> inv.getArgument(0));

        ReceptionDto.ReceptionResponseDto result = chargeSheetService.confirmPartialReception(request);

        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("Réception partielle");
        verify(receptionHistoryRepository).save(any(ReceptionHistory.class));
    }

    @Test
    void confirmPartialReception_WithCompleteReception_ShouldChangeStatus() {
        testSheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);

        ReceptionDto.ReceptionRequestDto request = ReceptionDto.ReceptionRequestDto.builder()
                .chargeSheetId(1L)
                .deliveryNoteNumber("DN-003")
                .items(List.of(
                        ReceptionDto.ReceptionItemDto.builder()
                                .itemId(1L)
                                .quantityReceived(10)
                                .build()
                ))
                .build();

        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(receptionHistoryRepository.findByChargeSheetId(1L)).thenReturn(Collections.emptyList());
        when(receptionHistoryRepository.save(any(ReceptionHistory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(chargeSheetRepository.save(any(ChargeSheet.class))).thenAnswer(inv -> inv.getArgument(0));

        ReceptionDto.ReceptionResponseDto result = chargeSheetService.confirmPartialReception(request);

        assertThat(result.isComplete()).isTrue();
        assertThat(result.getMessage()).contains("Tous les items");
        verify(chargeSheetRepository).save(any(ChargeSheet.class));
    }

    @Test
    void confirmPartialReception_WithExcessiveQuantity_ShouldThrowException() {
        testSheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);

        ReceptionDto.ReceptionRequestDto request = ReceptionDto.ReceptionRequestDto.builder()
                .chargeSheetId(1L)
                .items(List.of(
                        ReceptionDto.ReceptionItemDto.builder()
                                .itemId(1L)
                                .quantityReceived(15)
                                .build()
                ))
                .build();

        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(receptionHistoryRepository.findByChargeSheetId(1L)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> chargeSheetService.confirmPartialReception(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("dépasse la quantité commandée");
    }

    @Test
    void confirmPartialReception_WithInvalidStatus_ShouldThrowException() {
        testSheet.setStatus(ChargeSheetStatus.DRAFT);
        ReceptionDto.ReceptionRequestDto request = ReceptionDto.ReceptionRequestDto.builder()
                .chargeSheetId(1L)
                .build();

        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));

        assertThatThrownBy(() -> chargeSheetService.confirmPartialReception(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SENT_TO_SUPPLIER");
    }

    // ==================== TESTS RECEPTION HISTORY ====================

    @Test
    void getReceptionHistoryDto_ShouldReturnList() {
        when(receptionHistoryRepository.findByChargeSheetIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(testHistory));

        List<ReceptionHistoryDto> result = chargeSheetService.getReceptionHistoryDto(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeliveryNoteNumber()).isEqualTo("DN-001");
        assertThat(result.get(0).getItem().getItemNumber()).isEqualTo("1");
    }

    @Test
    void getReceptionHistoryDto_WithNoHistory_ShouldReturnEmpty() {
        when(receptionHistoryRepository.findByChargeSheetIdOrderByCreatedAtDesc(1L))
                .thenReturn(Collections.emptyList());

        List<ReceptionHistoryDto> result = chargeSheetService.getReceptionHistoryDto(1L);

        assertThat(result).isEmpty();
    }
    @Test
    void confirmPartialReception_WithZeroQuantity_ShouldSkipItem() {
        // given
        testSheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);
        ReceptionDto.ReceptionRequestDto request = ReceptionDto.ReceptionRequestDto.builder()
                .chargeSheetId(1L)
                .deliveryNoteNumber("DN-004")
                .items(List.of(
                        ReceptionDto.ReceptionItemDto.builder()
                                .itemId(1L)
                                .quantityReceived(0)  // ← Quantité zéro
                                .build()
                ))
                .build();

        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(receptionHistoryRepository.findByChargeSheetId(1L)).thenReturn(Collections.emptyList());

        // when
        ReceptionDto.ReceptionResponseDto result = chargeSheetService.confirmPartialReception(request);

        // then
        assertThat(result.getMessage()).contains("Réception partielle");
        verify(receptionHistoryRepository, never()).save(any());  // Pas d'historique sauvegardé
    }

    @Test
    void confirmPartialReception_WithoutComment_ShouldWork() {
        testSheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);

        ReceptionDto.ReceptionRequestDto request = ReceptionDto.ReceptionRequestDto.builder()
                .chargeSheetId(1L)
                .deliveryNoteNumber("DN-005")
                .items(List.of(
                        ReceptionDto.ReceptionItemDto.builder()
                                .itemId(1L)
                                .quantityReceived(5)
                                .build()
                ))
                // Pas de comments
                .build();

        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(receptionHistoryRepository.findByChargeSheetId(1L)).thenReturn(Collections.emptyList());
        when(receptionHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        ReceptionDto.ReceptionResponseDto result = chargeSheetService.confirmPartialReception(request);

        // then
        assertThat(result).isNotNull();
        verify(receptionHistoryRepository).save(argThat(history -> history.getComments() == null));
    }

    @Test
    void confirmPartialReception_WithoutReceptionDate_ShouldUseCurrentDate() {
        testSheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);

        ReceptionDto.ReceptionRequestDto request = ReceptionDto.ReceptionRequestDto.builder()
                .chargeSheetId(1L)
                .deliveryNoteNumber("DN-006")
                .receptionDate(null)  // ← Date null
                .items(List.of(
                        ReceptionDto.ReceptionItemDto.builder()
                                .itemId(1L)
                                .quantityReceived(5)
                                .build()
                ))
                .build();

        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(receptionHistoryRepository.findByChargeSheetId(1L)).thenReturn(Collections.emptyList());
        when(receptionHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        chargeSheetService.confirmPartialReception(request);

        // then
        verify(receptionHistoryRepository).save(argThat(history ->
                history.getReceptionDate() != null &&
                        history.getReceptionDate().equals(LocalDate.now())
        ));
    }
    @Test
    void confirmPartialReception_ShouldSendEmailNotification() {
        testSheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);

        ReceptionDto.ReceptionRequestDto request = ReceptionDto.ReceptionRequestDto.builder()
                .chargeSheetId(1L)
                .deliveryNoteNumber("DN-007")
                .items(List.of(
                        ReceptionDto.ReceptionItemDto.builder()
                                .itemId(1L)
                                .quantityReceived(5)
                                .build()
                ))
                .build();

        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(receptionHistoryRepository.findByChargeSheetId(1L)).thenReturn(Collections.emptyList());
        when(receptionHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        chargeSheetService.confirmPartialReception(request);

        // then
        verify(notificationService, atLeastOnce()).sendNotificationToProjectAndSiteUsers(
                anyString(),  // subject
                anyString(),  // textMessage
                eq(testSheet.getProject()),
                eq(testSheet.getPlant())
        );
    }
    @Test
    void prepareReceptionData_WithNoItems_ShouldReturnEmptyList() {
        testSheet.setItems(new ArrayList<>());  // Plus d'items
        testSheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);

        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(receptionHistoryRepository.findByChargeSheetId(1L)).thenReturn(Collections.emptyList());

        ReceptionDto.ReceptionResponseDto result = chargeSheetService.prepareReceptionData(1L);

        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void prepareReceptionData_WhenQuantityOrderedIsNull_ShouldTreatAsZero() {
        testItem.setQuantityOfTestModules(null);  // ← Quantité null
        testSheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);

        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(receptionHistoryRepository.findByChargeSheetId(1L)).thenReturn(Collections.emptyList());

        ReceptionDto.ReceptionResponseDto result = chargeSheetService.prepareReceptionData(1L);

        assertThat(result.getItems().get(0).getQuantityOrdered()).isEqualTo(0);
        assertThat(result.getItems().get(0).getQuantityRemaining()).isEqualTo(0);
    }
    @Test
    void confirmPartialReception_WhenItemNotFound_ShouldThrowException() {
        testSheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);

        ReceptionDto.ReceptionRequestDto request = ReceptionDto.ReceptionRequestDto.builder()
                .chargeSheetId(1L)
                .items(List.of(
                        ReceptionDto.ReceptionItemDto.builder()
                                .itemId(999L)  // ID inexistant
                                .quantityReceived(5)
                                .build()
                ))
                .build();

        when(chargeSheetRepository.findById(1L)).thenReturn(Optional.of(testSheet));
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chargeSheetService.confirmPartialReception(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Item not found");
    }

    @Test
    void confirmPartialReception_WhenChargeSheetNotFound_ShouldThrowException() {
        ReceptionDto.ReceptionRequestDto request = ReceptionDto.ReceptionRequestDto.builder()
                .chargeSheetId(999L)
                .build();

        when(chargeSheetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chargeSheetService.confirmPartialReception(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Charge sheet not found");
    }
    @Test
    void getReceptionHistoryDto_WithMultipleEntries_ShouldReturnSortedByDate() {
        ReceptionHistory olderHistory = ReceptionHistory.builder()
                .id(2L)
                .chargeSheet(testSheet)
                .item(testItem)
                .quantityReceived(2)
                .deliveryNoteNumber("DN-OLDER")
                .receptionDate(LocalDate.now().minusDays(5))
                .createdAt(LocalDate.now().minusDays(5))
                .build();

        ReceptionHistory newerHistory = ReceptionHistory.builder()
                .id(3L)
                .chargeSheet(testSheet)
                .item(testItem)
                .quantityReceived(3)
                .deliveryNoteNumber("DN-NEWER")
                .receptionDate(LocalDate.now())
                .createdAt(LocalDate.now())
                .build();

        when(receptionHistoryRepository.findByChargeSheetIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(newerHistory, olderHistory));

        List<ReceptionHistoryDto> result = chargeSheetService.getReceptionHistoryDto(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDeliveryNoteNumber()).isEqualTo("DN-NEWER");
        assertThat(result.get(1).getDeliveryNoteNumber()).isEqualTo("DN-OLDER");
    }
}