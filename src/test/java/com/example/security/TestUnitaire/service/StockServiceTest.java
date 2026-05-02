package com.example.security.TestUnitaire.service;

import com.example.security.cahierdeCharge.*;
import com.example.security.fichierTechnique.*;
import com.example.security.site.Site;
import com.example.security.site.SiteRepository;
import com.example.security.stock.*;
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
@MockitoSettings(strictness = Strictness.LENIENT)  // ← AJOUTER CETTE LIGNE
class StockServiceTest {

    @Mock
    private StockModuleRepository stockRepository;

    @Mock
    private TechnicalFileService technicalFileService;

    @Mock
    private SiteRepository siteRepository;

    @InjectMocks
    private StockService stockService;

    private User ppUser;
    private Site testSite;
    private ChargeSheet testSheet;
    private ChargeSheetItem testChargeSheetItem;
    private TechnicalFile testTechnicalFile;
    private TechnicalFileItem testTechnicalFileItem;
    private StockModule testStockModule;

    @BeforeEach
    void setUp() {
        ppUser = User.builder()
                .id(1)
                .email("pp@test.com")
                .firstname("PP")
                .lastname("User")
                .role(Role.PP)
                .build();

        testSite = new Site();
        testSite.setId(1L);
        testSite.setName("MH1");
        testSite.setActive(true);

        ppUser.setSite(testSite);

        testSheet = ChargeSheet.builder()
                .id(1L)
                .plant("MH1")
                .project("FORD")
                .orderNumber("ORD-001")
                .build();

        testChargeSheetItem = ChargeSheetItem.builder()
                .id(1L)
                .chargeSheet(testSheet)
                .itemNumber("1")
                .quantityOfTestModules(5)
                .build();

        testTechnicalFile = TechnicalFile.builder()
                .id(1L)
                .reference("TF-001")
                .build();

        testTechnicalFileItem = TechnicalFileItem.builder()
                .id(1L)
                .technicalFile(testTechnicalFile)
                .chargeSheetItem(testChargeSheetItem)
                .position("Position 1")
                .displacementPathM1("10.5")
                .displacementPathM2("11.0")
                .displacementPathM3("12.0")
                .programmedSealingValueM1("5.0")
                .programmedSealingValueM2("5.5")
                .programmedSealingValueM3("6.0")
                .detectionsM1("OK")
                .detectionsM2("OK")
                .detectionsM3("OK")
                .validationStatus(TechnicalFileItemStatus.VALIDATED_PP)
                .build();

        testStockModule = StockModule.builder()
                .id(1L)
                .technicalFile(testTechnicalFile)
                .technicalFileItem(testTechnicalFileItem)
                .chargeSheetItemId(1L)
                .itemNumber("1")
                .position("Position 1")
                .finalDisplacement(12.0)
                .finalProgrammedSealing(6.0)
                .finalDetection("OK")
                .status(StockModule.StockStatus.AVAILABLE)
                .movedBy(ppUser.getEmail())
                .movedAt(LocalDate.now())
                .site(testSite)
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(ppUser);
        when(auth.getName()).thenReturn(ppUser.getEmail());
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    // ==================== Tests moveItemToStock ====================

    @Test
    void moveItemToStock_WhenItemValid_ShouldCreateStock() {
        when(technicalFileService.getTechnicalFileItemById(1L)).thenReturn(testTechnicalFileItem);
        when(stockRepository.findByTechnicalFileItemId(1L)).thenReturn(Optional.empty());
        when(stockRepository.save(any(StockModule.class))).thenAnswer(inv -> inv.getArgument(0));

        StockModuleDto dto = StockModuleDto.builder()
                .casier("A1")
                .quantite(5)
                .build();

        StockModule result = stockService.moveItemToStock(1L, dto);

        assertThat(result).isNotNull();
        assertThat(result.getItemNumber()).isEqualTo("1");
        assertThat(result.getStatus()).isEqualTo(StockModule.StockStatus.AVAILABLE);
        verify(stockRepository).save(any(StockModule.class));
    }

    @Test
    void moveItemToStock_WhenItemAlreadyInStock_ShouldUpdate() {
        when(technicalFileService.getTechnicalFileItemById(1L)).thenReturn(testTechnicalFileItem);
        when(stockRepository.findByTechnicalFileItemId(1L)).thenReturn(Optional.of(testStockModule));
        when(stockRepository.save(any(StockModule.class))).thenAnswer(inv -> inv.getArgument(0));

        StockModuleDto dto = StockModuleDto.builder()
                .casier("B2")
                .quantite(10)
                .build();

        StockModule result = stockService.moveItemToStock(1L, dto);

        assertThat(result).isNotNull();
        assertThat(result.getCasier()).isEqualTo("B2");
    }

    @Test
    void moveItemToStock_WhenItemNotValidated_ShouldThrowException() {
        testTechnicalFileItem.setValidationStatus(TechnicalFileItemStatus.DRAFT);
        when(technicalFileService.getTechnicalFileItemById(1L)).thenReturn(testTechnicalFileItem);

        assertThatThrownBy(() -> stockService.moveItemToStock(1L, new StockModuleDto()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("doit être validé");
    }

    // ==================== Tests getAllStock ====================

    @Test
    void getAllStock_ShouldReturnFilteredBySite() {
        List<StockModule> allStock = List.of(testStockModule);
        when(stockRepository.findAll()).thenReturn(allStock);

        List<StockModule> result = stockService.getAllStock();

        assertThat(result).hasSize(1);
    }

    // ==================== Tests getStockById ====================

    @Test
    void getStockById_WhenExists_ShouldReturn() {
        when(stockRepository.findById(1L)).thenReturn(Optional.of(testStockModule));

        StockModule result = stockService.getStockById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }




    // ==================== Tests changeStatus ====================

    @Test
    void changeStatus_ShouldUpdateStatus() {
        when(stockRepository.findById(1L)).thenReturn(Optional.of(testStockModule));
        when(stockRepository.save(any(StockModule.class))).thenAnswer(inv -> inv.getArgument(0));

        StockModule result = stockService.changeStatus(1L, StockModule.StockStatus.USED);

        assertThat(result.getStatus()).isEqualTo(StockModule.StockStatus.USED);
    }

    @Test
    void changeStatus_WhenModuleNotFound_ShouldReturnNull() {
        when(stockRepository.findById(999L)).thenReturn(Optional.empty());

        StockModule result = stockService.changeStatus(999L, StockModule.StockStatus.USED);

        assertThat(result).isNull();
    }

    // ==================== Tests createStockModule ====================

    @Test
    void createStockModule_ShouldCreate() {
        StockModuleDto dto = StockModuleDto.builder()
                .itemNumber("NEW-001")
                .position("New Position")
                .quantite(10)
                .status(StockModule.StockStatus.AVAILABLE)
                .siteName("MH1")
                .build();

        when(siteRepository.findByName("MH1")).thenReturn(Optional.of(testSite));
        when(stockRepository.save(any(StockModule.class))).thenAnswer(inv -> inv.getArgument(0));

        StockModule result = stockService.createStockModule(dto);

        assertThat(result).isNotNull();
        assertThat(result.getItemNumber()).isEqualTo("NEW-001");
    }

    // ==================== Tests updateStockModule ====================

    @Test
    void updateStockModule_ShouldUpdate() {
        StockModuleDto dto = StockModuleDto.builder()
                .casier("C3")
                .quantite(15)
                .status(StockModule.StockStatus.USED)
                .build();

        when(stockRepository.findById(1L)).thenReturn(Optional.of(testStockModule));
        when(stockRepository.save(any(StockModule.class))).thenAnswer(inv -> inv.getArgument(0));

        StockModule result = stockService.updateStockModule(1L, dto);

        assertThat(result.getCasier()).isEqualTo("C3");
        assertThat(result.getQuantite()).isEqualTo(15);
        assertThat(result.getStatus()).isEqualTo(StockModule.StockStatus.USED);
    }

    // ==================== Tests getStockStatistics ====================

    @Test
    void getStockStatistics_ShouldReturnStats() {
        List<StockModule> allStock = List.of(testStockModule);
        when(stockRepository.findAll()).thenReturn(allStock);

        Map<String, Object> stats = stockService.getStockStatistics();

        assertThat(stats.get("total")).isEqualTo(1L);
        assertThat(stats.get("available")).isEqualTo(1L);
        assertThat(stats.get("used")).isEqualTo(0L);
    }

    // ==================== Tests getPreStockInfo ====================

    @Test
    void getPreStockInfo_ShouldReturnItemInfo() {
        when(technicalFileService.getTechnicalFileItemById(1L)).thenReturn(testTechnicalFileItem);

        Map<String, Object> info = stockService.getPreStockInfo(1L);

        assertThat(info.get("itemNumber")).isEqualTo("1");
        assertThat(info.get("position")).isEqualTo("Position 1");
        assertThat(info.get("finalDisplacement")).isEqualTo(12.0);
        assertThat(info.get("finalDetection")).isEqualTo("OK");
    }
}