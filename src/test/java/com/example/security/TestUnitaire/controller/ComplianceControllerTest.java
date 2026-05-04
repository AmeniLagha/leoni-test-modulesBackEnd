package com.example.security.TestUnitaire.controller;

import com.example.security.cahierdeCharge.*;
import com.example.security.common.ApiResponse;
import com.example.security.config.JwtService;
import com.example.security.conformité.*;
import com.example.security.projet.Projet;
import com.example.security.projet.ProjetRepository;
import com.example.security.site.Site;
import com.example.security.site.SiteRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ComplianceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ComplianceRepository complianceRepository;

    @Autowired
    private ChargeSheetRepository chargeSheetRepository;

    @Autowired
    private ChargeSheetItemRepository itemRepository;

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
    private String ptToken;
    private String ingToken;
    private String adminToken;
    private User ppUser;
    private User ptUser;
    private User ingUser;
    private User adminUser;
    private Site testSite;
    private Projet testProjet;
    private ChargeSheet testChargeSheet;
    private ChargeSheetItem testItem;
    private Compliance testCompliance;

    private String uniqueId;

    @BeforeEach
    void setUp() {
        uniqueId = UUID.randomUUID().toString().substring(0, 8);

        complianceRepository.deleteAll();
        itemRepository.deleteAll();
        chargeSheetRepository.deleteAll();
        tokenRepository.deleteAll();
        userRepository.deleteAll();
        siteRepository.deleteAll();
        projetRepository.deleteAll();

        testSite = new Site();
        testSite.setName("MH1_" + uniqueId);
        testSite.setActive(true);
        testSite = siteRepository.save(testSite);

        testProjet = new Projet();
        testProjet.setName("FORD_" + uniqueId);
        testProjet.setActive(true);
        testProjet = projetRepository.save(testProjet);

        ppUser = createAndSaveUser("pp_" + uniqueId + "@test.com", "pp123", Role.PP, 10001);
        ptUser = createAndSaveUser("pt_" + uniqueId + "@test.com", "pt123", Role.PT, 10002);
        ingUser = createAndSaveUser("ing_" + uniqueId + "@test.com", "ing123", Role.ING, 10003);
        adminUser = createAndSaveUser("admin_" + uniqueId + "@test.com", "admin123", Role.ADMIN, 10000);

        ppToken = jwtService.generateToken(ppUser);
        ptToken = jwtService.generateToken(ptUser);
        ingToken = jwtService.generateToken(ingUser);
        adminToken = jwtService.generateToken(adminUser);

        saveToken(ppUser, ppToken);
        saveToken(ptUser, ptToken);
        saveToken(ingUser, ingToken);
        saveToken(adminUser, adminToken);

        testChargeSheet = ChargeSheet.builder()
                .plant(testSite.getName())
                .project(testProjet.getName())
                .orderNumber("ORD-001_" + uniqueId)
                .status(ChargeSheetStatus.VALIDATED_PT)
                .createdBy(ingUser.getEmail())
                .createdAt(LocalDate.now())
                .items(new ArrayList<>())
                .build();
        testChargeSheet = chargeSheetRepository.save(testChargeSheet);

        testItem = ChargeSheetItem.builder()
                .chargeSheet(testChargeSheet)
                .itemNumber("1_" + uniqueId)
                .quantityOfTestModules(5)
                .itemStatus("TECH_FILLED")
                .createdBy(ingUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        testItem = itemRepository.save(testItem);

        testChargeSheet.getItems().add(testItem);
        testChargeSheet = chargeSheetRepository.save(testChargeSheet);

        testCompliance = Compliance.builder()
                .item(testItem)
                .chargeSheetId(testChargeSheet.getId())
                .orderNumber("ORD-001_" + uniqueId)
                .indexValue(1)
                .testDateTime(LocalDate.now())
                .technicianName("John Doe_" + uniqueId)
                .rfidNumber("RFID-001_" + uniqueId)
                .leoniPartNumber("LEONI-001_" + uniqueId)
                .producer("N")
                .type("T")
                .qualifiedTestModule(true)
                .conditionallyQualifiedTestModule(false)
                .notQualifiedTestModule(false)
                .createdBy(ppUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        testCompliance = complianceRepository.save(testCompliance);
    }

    private User createAndSaveUser(String email, String password, Role role, int matricule) {
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

    // ==================== POST /api/v1/compliance ====================

    @Test
    void createCompliance_AsPp_ShouldCreateCompliance() throws Exception {
        ComplianceDto.CreateDto dto = ComplianceDto.CreateDto.builder()
                .itemId(testItem.getId())
                .orderNumber("ORD-002_" + uniqueId)
                .orderitemNumber("ITEM-001_" + uniqueId)
                .testDateTime(LocalDate.now())
                .technicianName("Jane Doe_" + uniqueId)
                .rfidNumber("RFID-002_" + uniqueId)
                .leoniPartNumber("LEONI-002_" + uniqueId)
                .indexValue(1)
                .producer("N")
                .type("T")
                .qualifiedTestModule(true)
                .build();

        mockMvc.perform(post("/api/v1/compliance")
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())  // ✅ 201
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Fiche de conformité créée avec succès"))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-002_" + uniqueId));
    }

    @Test
    void createCompliance_AsPt_ShouldReturnForbidden() throws Exception {
        ComplianceDto.CreateDto dto = ComplianceDto.CreateDto.builder()
                .itemId(testItem.getId())
                .orderNumber("ORD-003_" + uniqueId)
                .build();

        mockMvc.perform(post("/api/v1/compliance")
                        .header("Authorization", "Bearer " + ptToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
        // Pas de vérification JSON car réponse vide
    }

    // ==================== GET /api/v1/compliance/{id} ====================

    @Test
    void getComplianceById_WithValidId_ShouldReturnCompliance() throws Exception {
        mockMvc.perform(get("/api/v1/compliance/" + testCompliance.getId())
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-001_" + uniqueId))
                .andExpect(jsonPath("$.data.technicianName").value("John Doe_" + uniqueId));
    }

    @Test
    void getComplianceById_WithInvalidId_ShouldReturnError() throws Exception {
        mockMvc.perform(get("/api/v1/compliance/99999")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Compliance not found"));  // ✅ Corrigé
    }

    // ==================== PUT /api/v1/compliance/{id} ====================

    @Test
    void updateCompliance_AsPp_ShouldUpdateCompliance() throws Exception {
        ComplianceDto.UpdateDto dto = ComplianceDto.UpdateDto.builder()
                .orderitemNumber("UPDATED-ITEM_" + uniqueId)
                .sequenceTestPins("PASS")
                .qualifiedTestModule(true)
                .remarks("Updated remarks_" + uniqueId)
                .build();

        mockMvc.perform(put("/api/v1/compliance/" + testCompliance.getId())
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Fiche de conformité modifiée avec succès"))
                .andExpect(jsonPath("$.data.orderitemNumber").value("UPDATED-ITEM_" + uniqueId));
    }

    @Test
    void updateCompliance_AsIng_ShouldReturnForbidden() throws Exception {
        ComplianceDto.UpdateDto dto = ComplianceDto.UpdateDto.builder()
                .orderitemNumber("HACKED_" + uniqueId)
                .build();

        mockMvc.perform(put("/api/v1/compliance/" + testCompliance.getId())
                        .header("Authorization", "Bearer " + ingToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
        // Pas de vérification JSON car réponse vide
    }

    // ==================== DELETE /api/v1/compliance/{id} ====================

    @Test
    void deleteCompliance_AsPp_ShouldDeleteCompliance() throws Exception {
        mockMvc.perform(delete("/api/v1/compliance/" + testCompliance.getId())
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())  // ✅ 200 au lieu de 204
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Fiche de conformité supprimée avec succès"));
    }

    @Test
    void deleteCompliance_AsPt_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/compliance/" + testCompliance.getId())
                        .header("Authorization", "Bearer " + ptToken))
                .andExpect(status().isForbidden());
        // Pas de vérification JSON car réponse vide
    }

    // ==================== GET /api/v1/compliance/charge-sheet/{chargeSheetId} ====================

    @Test
    void getComplianceByChargeSheet_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/v1/compliance/charge-sheet/" + testChargeSheet.getId())
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ==================== GET /api/v1/compliance/item/{itemId} ====================

    @Test
    void getComplianceByItem_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/v1/compliance/item/" + testItem.getId())
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ==================== GET /api/v1/compliance ====================

    @Test
    void getAllCompliance_AsAdmin_ShouldReturnAll() throws Exception {
        mockMvc.perform(get("/api/v1/compliance")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getAllCompliance_AsPp_ShouldReturnAll() throws Exception {
        mockMvc.perform(get("/api/v1/compliance")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== GET /api/v1/compliance/display ====================

    @Test
    void getAllComplianceForDisplay_ShouldReturnEnrichedData() throws Exception {
        mockMvc.perform(get("/api/v1/compliance/display")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ==================== POST /api/v1/compliance/create-for-item/{itemId} ====================

    @Test
    void createForItem_ShouldCreateComplianceForEachModule() throws Exception {
        testItem.setQuantityOfTestModules(3);
        itemRepository.save(testItem);

        mockMvc.perform(post("/api/v1/compliance/create-for-item/" + testItem.getId())
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isCreated())  // ✅ 201
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Fiches de conformité créées avec succès"))
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    // ==================== GET /api/v1/compliance/prepare/{itemId} ====================

    @Test
    void prepareComplianceForItem_ShouldReturnPreparationData() throws Exception {
        mockMvc.perform(get("/api/v1/compliance/prepare/" + testItem.getId())
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}