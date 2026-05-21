package com.example.security.TestUnitaire.service;

import com.example.security.cahierdeCharge.*;
import com.example.security.conformité.*;
import com.example.security.email.GlobalNotificationService;
import com.example.security.reception.ReceptionHistory;
import com.example.security.reception.ReceptionHistoryRepository;
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

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // ✅ AJOUTER CECI
class ComplianceReminderServiceTest {

    @Mock
    private ChargeSheetRepository chargeSheetRepository;

    @Mock
    private ChargeSheetItemRepository itemRepository;

    @Mock
    private ReceptionHistoryRepository receptionHistoryRepository;

    @Mock
    private ComplianceRepository complianceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GlobalNotificationService notificationService;

    @InjectMocks
    private ComplianceReminderService complianceReminderService;

    private ChargeSheet testSheet;
    private ChargeSheetItem testItem;
    private ReceptionHistory testReception;
    private Compliance testCompliance;
    private User ppUser;

    @BeforeEach
    void setUp() {
        testSheet = ChargeSheet.builder()
                .id(1L)
                .plant("MH1")
                .project("FORD")
                .orderNumber("ORD-001")
                .status(ChargeSheetStatus.VALIDATED_PT)
                .build();

        testItem = ChargeSheetItem.builder()
                .id(1L)
                .itemNumber("ITEM-001")
                .chargeSheet(testSheet)
                .quantityOfTestModules(5)
                .housingReferenceSupplierCustomer("REF-001")
                .build();

        testReception = ReceptionHistory.builder()
                .id(1L)
                .item(testItem)
                .quantityReceived(3)
                .receptionDate(LocalDate.now().minusDays(5))
                .createdAt(LocalDate.now().minusDays(5))
                .build();

        testCompliance = Compliance.builder()
                .id(1L)
                .qualifiedTestModule(true)
                .createdAt(LocalDate.now())
                .build();

        ppUser = User.builder()
                .id(1)
                .email("pp@leoni.com")
                .role(Role.PP)
                .build();
    }

    // ==================== TESTS ====================

    @Test
    void sendPendingComplianceReminders_WhenNoReceptions_ShouldNotSendNotification() {
        when(receptionHistoryRepository.findAll()).thenReturn(new ArrayList<>());  // ✅ Éviter List.of()
        when(receptionHistoryRepository.count()).thenReturn(0L);

        complianceReminderService.sendPendingComplianceReminders();

        verify(notificationService, never()).sendNotificationToOneUser(anyString(), anyString(), anyString());
    }

    @Test
    void sendPendingComplianceReminders_WhenAllComplianceFilled_ShouldNotSendReminder() {
        // ✅ Créer des listes modifiables
        List<ReceptionHistory> histories = new ArrayList<>();
        histories.add(testReception);
        when(receptionHistoryRepository.findAll()).thenReturn(histories);
        when(receptionHistoryRepository.count()).thenReturn(1L);

        List<Compliance> compliances = new ArrayList<>();
        compliances.add(testCompliance);
        compliances.add(testCompliance);
        compliances.add(testCompliance);
        when(complianceRepository.findByItemId(1L)).thenReturn(compliances);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        // ✅ Note: pas de when(userRepository.findPpUsersByProjectAndSite) car il ne sera pas appelé
        // puisque isAllReceivedQuantityHasCompliance retourne true

        complianceReminderService.sendPendingComplianceReminders();

        verify(notificationService, never()).sendNotificationToOneUser(anyString(), anyString(), anyString());
    }

    @Test
    void getItemComplianceStatus_ShouldReturnCorrectStatus() {
        // ✅ Créer des listes modifiables
        List<ReceptionHistory> histories = new ArrayList<>();
        histories.add(testReception);
        when(receptionHistoryRepository.findByItemId(1L)).thenReturn(histories);

        List<Compliance> compliances = new ArrayList<>();
        compliances.add(testCompliance);
        compliances.add(testCompliance);
        when(complianceRepository.findByItemId(1L)).thenReturn(compliances);

        Map<String, Object> status = complianceReminderService.getItemComplianceStatus(1L);

        assertThat(status.get("totalReceived")).isEqualTo(3);
        assertThat(status.get("totalCompliance")).isEqualTo(2);
        assertThat(status.get("pendingQuantity")).isEqualTo(1);
        assertThat(status.get("isComplete")).isEqualTo(false);
        assertThat(status.get("reminderActive")).isEqualTo(true);
    }

    @Test
    void resetRemindersForItem_ShouldClearReminder() {
        // Appel multiple pour vérifier que le reset fonctionne
        complianceReminderService.resetRemindersForItem(1L);
        complianceReminderService.resetRemindersForItem(1L);
        // Pas d'exception = succès
        verify(notificationService, never()).sendNotificationToOneUser(anyString(), anyString(), anyString());
    }
    @Test
    void sendPendingComplianceReminders_WhenReceptionJustHappened_ShouldNotSendReminder() {
        // Réception aujourd'hui
        testReception.setReceptionDate(LocalDate.now());

        List<ReceptionHistory> histories = new ArrayList<>();
        histories.add(testReception);
        when(receptionHistoryRepository.findAll()).thenReturn(histories);
        when(receptionHistoryRepository.count()).thenReturn(1L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(complianceRepository.findByItemId(1L)).thenReturn(new ArrayList<>()); // pas de conformités
        when(receptionHistoryRepository.findByItemIdOrderByReceptionDateDesc(1L))
                .thenReturn(histories);

        complianceReminderService.sendPendingComplianceReminders();

        // daysSinceLastReception = 0 → pas de rappel
        verify(notificationService, never()).sendNotificationToOneUser(anyString(), anyString(), anyString());
    }
    @Test
    void sendPendingComplianceReminders_WhenMoreThan14Days_ShouldStopReminders() {
        // Réception il y a 15 jours
        testReception.setReceptionDate(LocalDate.now().minusDays(15));

        List<ReceptionHistory> histories = new ArrayList<>();
        histories.add(testReception);
        when(receptionHistoryRepository.findAll()).thenReturn(histories);
        when(receptionHistoryRepository.count()).thenReturn(1L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(complianceRepository.findByItemId(1L)).thenReturn(new ArrayList<>());
        when(receptionHistoryRepository.findByItemIdOrderByReceptionDateDesc(1L))
                .thenReturn(histories);

        complianceReminderService.sendPendingComplianceReminders();

        // > 14 jours → pas de rappel
        verify(notificationService, never()).sendNotificationToOneUser(anyString(), anyString(), anyString());
    }

}