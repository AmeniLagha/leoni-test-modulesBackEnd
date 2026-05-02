package com.example.security.TestUnitaire.service;

import com.example.security.cahierdeCharge.*;
import com.example.security.conformité.*;
import com.example.security.email.GlobalNotificationService;
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
    private ChargeSheet testSheet;
    private ChargeSheetItem testItem;
    private Compliance testCompliance;

    @BeforeEach
    void setUp() {
        ppUser = User.builder()
                .id(1)
                .email("pp@test.com")
                .firstname("PP")
                .lastname("User")
                .role(Role.PP)
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

        // ✅ FORCER l'injection du reminderService (solution de contournement)
        ReflectionTestUtils.setField(complianceService, "reminderService", reminderService);
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
}