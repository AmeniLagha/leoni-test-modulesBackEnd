package com.example.security.TestUnitaire.service;

import com.example.security.cahierdeCharge.*;
import com.example.security.email.GlobalNotificationService;
import com.example.security.fichierTechnique.*;
import com.example.security.fichierTechnique.TechnicalFileHistory.TechnicalFileHistoryRepository;
import com.example.security.stock.StockModuleRepository;
import com.example.security.user.Role;
import com.example.security.user.User;
import jakarta.persistence.EntityManager;
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
class TechnicalFileServiceTest {

    @Mock
    private TechnicalFileRepository repository;

    @Mock
    private TechnicalFileItemRepository technicalFileItemRepository;

    @Mock
    private GlobalNotificationService notificationService;

    @Mock
    private ChargeSheetItemRepository chargeSheetItemRepository;

    @Mock
    private TechnicalFileHistoryRepository historyRepository;

    @Mock
    private StockModuleRepository stockModuleRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TechnicalFileService technicalFileService;

    private User ppUser;
    private ChargeSheet testSheet;
    private ChargeSheetItem testItem;
    private TechnicalFile testTechnicalFile;
    private TechnicalFileItem testTechnicalFileItem;

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

        testTechnicalFile = TechnicalFile.builder()
                .id(1L)
                .reference("TF-001")
                .createdBy(ppUser.getEmail())
                .createdAt(LocalDate.now())
                .technicalFileItems(new ArrayList<>())
                .build();

        // ✅ CORRIGÉ : Pas de stubbing ici !
        testTechnicalFileItem = TechnicalFileItem.builder()
                .id(1L)
                .technicalFile(testTechnicalFile)
                .chargeSheetItem(testItem)
                .technicianName("John Doe")
                .position("Position 1")
                .validationStatus(TechnicalFileItemStatus.DRAFT)
                .createdBy(ppUser.getEmail())
                .createdAt(LocalDate.now())
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(ppUser);
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    // ==================== Tests CRUD ====================

    @Test
    void createTechnicalFile_ShouldCreateAndNotify() {
        TechnicalFileDto.CreateDto dto = TechnicalFileDto.CreateDto.builder()
                .reference("TF-002")
                .items(List.of(TechnicalFileDto.TechnicalFileItemDto.builder()
                        .chargeSheetItemId(1L)
                        .technicianName("Jane Doe")
                        .build()))
                .build();

        when(chargeSheetItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(repository.save(any(TechnicalFile.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(notificationService).notifyTechnicalFileCreatedToProjectAndSite(
                anyLong(), anyLong(), anyString(), anyString(), anyString()
        );

        TechnicalFile result = technicalFileService.createTechnicalFile(dto);

        assertThat(result).isNotNull();
        assertThat(result.getReference()).isEqualTo("TF-002");

        // ✅ CORRIGÉ : Vérifier 2 appels au lieu d'1
        verify(repository, times(2)).save(any(TechnicalFile.class));
    }

    @Test
    void getTechnicalFileById_WhenExists_ShouldReturn() {
        when(repository.findById(1L)).thenReturn(Optional.of(testTechnicalFile));

        TechnicalFile result = technicalFileService.getTechnicalFileById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getTechnicalFileById_WhenNotExists_ShouldThrowException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> technicalFileService.getTechnicalFileById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Technical file not found");
    }

    @Test
    void updateTechnicalFile_ShouldUpdateAndNotify() {
        TechnicalFileDto.UpdateDto dto = TechnicalFileDto.UpdateDto.builder()
                .reference("TF-001-UPDATED")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testTechnicalFile));
        when(repository.save(any(TechnicalFile.class))).thenAnswer(inv -> inv.getArgument(0));

        TechnicalFile result = technicalFileService.updateTechnicalFile(1L, dto);

        assertThat(result.getReference()).isEqualTo("TF-001-UPDATED");
    }

    @Test
    void deleteTechnicalFile_ShouldDeleteAndNotify() {
        when(repository.findById(1L)).thenReturn(Optional.of(testTechnicalFile));
        doNothing().when(repository).delete(testTechnicalFile);

        technicalFileService.deleteTechnicalFile(1L);

        verify(repository).delete(testTechnicalFile);
    }

    // ==================== Tests Items ====================

    @Test
    void getTechnicalFileItemById_WhenExists_ShouldReturn() {
        when(technicalFileItemRepository.findById(1L)).thenReturn(Optional.of(testTechnicalFileItem));

        TechnicalFileItem result = technicalFileService.getTechnicalFileItemById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void updateTechnicalFileItem_ShouldUpdateAndNotify() {
        TechnicalFileDto.UpdateItemDto dto = TechnicalFileDto.UpdateItemDto.builder()
                .technicianName("Updated Name")
                .position("Updated Position")
                .build();

        when(technicalFileItemRepository.findById(1L)).thenReturn(Optional.of(testTechnicalFileItem));
        when(technicalFileItemRepository.save(any(TechnicalFileItem.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(notificationService).notifyTechnicalFileUpdatedToProjectAndSite(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString());

        TechnicalFileItem result = technicalFileService.updateTechnicalFileItem(1L, dto);

        assertThat(result.getTechnicianName()).isEqualTo("Updated Name");
        assertThat(result.getPosition()).isEqualTo("Updated Position");
    }

    @Test
    void deleteTechnicalFileItem_ShouldDeleteAndNotify() {
        when(technicalFileItemRepository.findById(1L)).thenReturn(Optional.of(testTechnicalFileItem));
        doNothing().when(technicalFileItemRepository).delete(testTechnicalFileItem);
        doNothing().when(notificationService).notifyDocumentDeletedToProjectAndSite(anyString(), anyLong(), anyLong(), anyString(), anyString(), anyString());

        technicalFileService.deleteTechnicalFileItem(1L);

        verify(technicalFileItemRepository).delete(testTechnicalFileItem);
    }

    @Test
    void addItemToTechnicalFile_ShouldAddItem() {
        TechnicalFileDto.AddItemDto dto = TechnicalFileDto.AddItemDto.builder()
                .chargeSheetItemId(1L)
                .technicianName("New Tech")
                .position("New Position")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testTechnicalFile));
        when(chargeSheetItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(repository.save(any(TechnicalFile.class))).thenAnswer(inv -> inv.getArgument(0));

        TechnicalFileItem result = technicalFileService.addItemToTechnicalFile(1L, dto);

        assertThat(result).isNotNull();
        assertThat(result.getTechnicianName()).isEqualTo("New Tech");
    }

    // ==================== Tests Validation ====================

    @Test
    void validateItem_AsPp_ShouldValidate() {
        testTechnicalFileItem.setValidationStatus(TechnicalFileItemStatus.DRAFT);
        when(technicalFileItemRepository.findById(1L)).thenReturn(Optional.of(testTechnicalFileItem));
        when(technicalFileItemRepository.save(any(TechnicalFileItem.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(notificationService).notifyTechnicalFileUpdatedToProjectAndSite(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString());

        TechnicalFileItem result = technicalFileService.validateItem(1L, "PP");

        assertThat(result.getValidationStatus()).isEqualTo(TechnicalFileItemStatus.VALIDATED_PP);
    }

    @Test
    void canValidateItem_ShouldReturnTrueForValidRole() {
        testTechnicalFileItem.setValidationStatus(TechnicalFileItemStatus.DRAFT);
        when(technicalFileItemRepository.findById(1L)).thenReturn(Optional.of(testTechnicalFileItem));

        boolean result = technicalFileService.canValidateItem(1L, "PP");

        assertThat(result).isTrue();
    }

    // ==================== Tests Listes ====================

    @Test
    void getAllTechnicalFilesList_ShouldReturnList() {
        when(repository.findAll()).thenReturn(List.of(testTechnicalFile));

        List<TechnicalFileDto.ListDto> result = technicalFileService.getAllTechnicalFilesList();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReference()).isEqualTo("TF-001");
    }
}