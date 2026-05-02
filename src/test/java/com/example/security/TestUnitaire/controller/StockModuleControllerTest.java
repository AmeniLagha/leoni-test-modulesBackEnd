package com.example.security.TestUnitaire.controller;

import com.example.security.cahierdeCharge.*;
import com.example.security.config.JwtService;
import com.example.security.fichierTechnique.*;
import com.example.security.projet.Projet;
import com.example.security.projet.ProjetRepository;
import com.example.security.site.Site;
import com.example.security.site.SiteRepository;
import com.example.security.stock.*;
import com.example.security.token.Token;
import com.example.security.token.TokenRepository;
import com.example.security.token.TokenType;
import com.example.security.user.Role;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class StockModuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StockModuleRepository stockRepository;

    @Autowired
    private TechnicalFileRepository technicalFileRepository;

    @Autowired
    private TechnicalFileItemRepository technicalFileItemRepository;

    @Autowired
    private ChargeSheetRepository chargeSheetRepository;

    @Autowired
    private ChargeSheetItemRepository chargeSheetItemRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private ProjetRepository projetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String ppToken;
    private String adminToken;
    private User ppUser;
    private User adminUser;
    private Site testSite;
    private Projet testProjet;
    private ChargeSheet testChargeSheet;
    private ChargeSheetItem testChargeSheetItem;
    private TechnicalFile testTechnicalFile;
    private TechnicalFileItem testTechnicalFileItem;
    private StockModule testStockModule;

    private String uniqueId;

    @BeforeEach
    void setUp() {
        // Générer un ID unique pour ce test
        uniqueId = UUID.randomUUID().toString().substring(0, 8);

        // Nettoyage dans l'ordre inverse des dépendances
        stockRepository.deleteAll();
        technicalFileItemRepository.deleteAll();
        technicalFileRepository.deleteAll();
        chargeSheetItemRepository.deleteAll();
        chargeSheetRepository.deleteAll();
        tokenRepository.deleteAll();
        userRepository.deleteAll();
        siteRepository.deleteAll();
        projetRepository.deleteAll();

        // Création site avec nom unique
        testSite = new Site();
        testSite.setName("MH1_" + uniqueId);
        testSite.setActive(true);
        testSite = siteRepository.save(testSite);

        // Création projet avec nom unique
        testProjet = new Projet();
        testProjet.setName("FORD_" + uniqueId);
        testProjet.setActive(true);
        testProjet = projetRepository.save(testProjet);

        // Création utilisateurs avec emails uniques
        ppUser = createUser("pp_" + uniqueId + "@test.com", "pp123", Role.PP, 10001);
        adminUser = createUser("admin_" + uniqueId + "@test.com", "admin123", Role.ADMIN, 10000);

        // Génération des tokens
        ppToken = jwtService.generateToken(ppUser);
        adminToken = jwtService.generateToken(adminUser);

        saveToken(ppUser, ppToken);
        saveToken(adminUser, adminToken);

        // Création cahier de test
        testChargeSheet = ChargeSheet.builder()
                .plant(testSite.getName())
                .project(testProjet.getName())
                .orderNumber("ORD-001_" + uniqueId)
                .status(ChargeSheetStatus.VALIDATED_PT)
                .createdBy(ppUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        testChargeSheet = chargeSheetRepository.save(testChargeSheet);

        // Création item cahier
        testChargeSheetItem = ChargeSheetItem.builder()
                .chargeSheet(testChargeSheet)
                .itemNumber("1_" + uniqueId)
                .quantityOfTestModules(5)
                .itemStatus("TECH_FILLED")
                .createdBy(ppUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        testChargeSheetItem = chargeSheetItemRepository.save(testChargeSheetItem);

        // Création dossier technique
        testTechnicalFile = TechnicalFile.builder()
                .reference("TF-001_" + uniqueId)
                .createdBy(ppUser.getEmail())
                .createdAt(LocalDate.now())
                .technicalFileItems(new ArrayList<>())
                .build();
        testTechnicalFile = technicalFileRepository.save(testTechnicalFile);

        // Création item technique
        testTechnicalFileItem = TechnicalFileItem.builder()
                .technicalFile(testTechnicalFile)
                .chargeSheetItem(testChargeSheetItem)
                .position("Position 1_" + uniqueId)
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
                .createdBy(ppUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        testTechnicalFileItem = technicalFileItemRepository.save(testTechnicalFileItem);

        // Ajouter l'item au TechnicalFile
        testTechnicalFile = technicalFileRepository.findById(testTechnicalFile.getId()).orElseThrow();
        testTechnicalFile.getTechnicalFileItems().add(testTechnicalFileItem);
        testTechnicalFile = technicalFileRepository.save(testTechnicalFile);

        // Création module en stock
        testStockModule = StockModule.builder()
                .technicalFile(testTechnicalFile)
                .technicalFileItem(testTechnicalFileItem)
                .chargeSheetItemId(testChargeSheetItem.getId())
                .itemNumber("1_" + uniqueId)
                .position("Position 1_" + uniqueId)
                .finalDisplacement(12.0)
                .finalProgrammedSealing(6.0)
                .finalDetection("OK")
                .status(StockModule.StockStatus.AVAILABLE)
                .movedBy(ppUser.getEmail())
                .movedAt(LocalDate.now())
                .site(testSite)
                .build();
        testStockModule = stockRepository.save(testStockModule);
    }

    private User createUser(String email, String password, Role role, int matricule) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstname("Test")
                .lastname("User")
                .matricule(matricule)
                .role(role)
                .site(testSite)
                .projets(Set.of(testProjet))
                .build();
        return userRepository.save(user);
    }

    private void saveToken(User user, String token) {
        Token tokenEntity = Token.builder()
                .token(token)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .user(user)
                .build();
        tokenRepository.save(tokenEntity);
    }

    // ==================== POST /api/v1/stock/move-item/{technicalFileItemId} ====================

    @Test
    void moveItemToStock_ShouldMoveItem() throws Exception {
        // Créer un nouvel item technique pour le test
        TechnicalFileItem newItem = TechnicalFileItem.builder()
                .technicalFile(testTechnicalFile)
                .chargeSheetItem(testChargeSheetItem)
                .position("Position 2_" + uniqueId)
                .validationStatus(TechnicalFileItemStatus.VALIDATED_PP)
                .createdBy(ppUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        newItem = technicalFileItemRepository.save(newItem);

        // Ajouter l'item au TechnicalFile
        testTechnicalFile.getTechnicalFileItems().add(newItem);
        technicalFileRepository.save(testTechnicalFile);

        StockModuleDto dto = StockModuleDto.builder()
                .casier("A1_" + uniqueId)
                .stuffNumr("STUFF-001_" + uniqueId)
                .quantite(5)
                .build();

        mockMvc.perform(post("/api/v1/stock/move-item/" + newItem.getId())
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    // ==================== GET /api/v1/stock ====================

    @Test
    void getAllStock_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/v1/stock")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllStock_AsAdmin_ShouldReturnAll() throws Exception {
        mockMvc.perform(get("/api/v1/stock")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // ==================== GET /api/v1/stock/statistics ====================

    @Test
    void getStockStatistics_ShouldReturnStats() throws Exception {
        mockMvc.perform(get("/api/v1/stock/statistics")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.available").value(1));
    }

    // ==================== GET /api/v1/stock/my-site ====================

    @Test
    void getMySiteStock_ShouldReturnSiteStock() throws Exception {
        mockMvc.perform(get("/api/v1/stock/my-site")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk());
    }

    // ==================== GET /api/v1/stock/site/{siteName} ====================

    @Test
    void getStockBySite_ShouldReturnStock() throws Exception {
        mockMvc.perform(get("/api/v1/stock/site/" + testSite.getName())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ==================== GET /api/v1/stock/sites ====================

    @Test
    void getSitesWithStock_ShouldReturnSites() throws Exception {
        mockMvc.perform(get("/api/v1/stock/sites")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ==================== GET /api/v1/stock/{id} ====================

    @Test
    void getStockById_WithValidId_ShouldReturn() throws Exception {
        mockMvc.perform(get("/api/v1/stock/" + testStockModule.getId())
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemNumber").value("1_" + uniqueId));
    }


    // ==================== PATCH /api/v1/stock/{id}/status ====================

    @Test
    void updateStatus_ShouldChangeStatus() throws Exception {
        mockMvc.perform(patch("/api/v1/stock/" + testStockModule.getId() + "/status")
                        .param("status", "USED")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("USED"));
    }

    // ==================== POST /api/v1/stock ====================

    @Test
    void createStockModule_ShouldCreate() throws Exception {
        StockModuleDto dto = StockModuleDto.builder()
                .itemNumber("NEW-001_" + uniqueId)
                .position("New Position_" + uniqueId)
                .quantite(10)
                .status(StockModule.StockStatus.AVAILABLE)
                .siteName(testSite.getName())
                .build();

        mockMvc.perform(post("/api/v1/stock")
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemNumber").value("NEW-001_" + uniqueId));
    }

    // ==================== PUT /api/v1/stock/{id} ====================

    @Test
    void updateStockModule_ShouldUpdate() throws Exception {
        StockModuleDto dto = StockModuleDto.builder()
                .casier("B2_" + uniqueId)
                .quantite(8)
                .status(StockModule.StockStatus.USED)
                .build();

        mockMvc.perform(put("/api/v1/stock/" + testStockModule.getId())
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.casier").value("B2_" + uniqueId));
    }

    // ==================== GET /api/v1/stock/dto ====================

    @Test
    void getAllStockDto_ShouldReturnDtoList() throws Exception {
        mockMvc.perform(get("/api/v1/stock/dto")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ==================== GET /api/v1/stock/item/{technicalFileItemId}/pre-stock-info ====================

    @Test
    void getPreStockInfo_ShouldReturnItemInfo() throws Exception {
        mockMvc.perform(get("/api/v1/stock/item/" + testTechnicalFileItem.getId() + "/pre-stock-info")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemNumber").value("1_" + uniqueId))
                .andExpect(jsonPath("$.position").value("Position 1_" + uniqueId));
    }
}