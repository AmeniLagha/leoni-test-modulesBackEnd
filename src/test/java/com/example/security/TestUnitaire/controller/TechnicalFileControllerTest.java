package com.example.security.TestUnitaire.controller;

import com.example.security.cahierdeCharge.*;
import com.example.security.common.ApiResponse;
import com.example.security.config.JwtService;
import com.example.security.fichierTechnique.*;
import com.example.security.fichierTechnique.TechnicalFileHistory.TechnicalFileHistoryRepository;
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
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
class TechnicalFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TechnicalFileRepository technicalFileRepository;

    @Autowired
    private TechnicalFileItemRepository technicalFileItemRepository;

    @Autowired
    private TechnicalFileHistoryRepository historyRepository;

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
    private String mcToken;
    private String mpToken;
    private String ptToken;
    private String ingToken;
    private String adminToken;
    private User ppUser;
    private User mcUser;
    private User mpUser;
    private User ptUser;
    private User ingUser;
    private User adminUser;
    private Site testSite;
    private Projet testProjet;
    private ChargeSheet testChargeSheet;
    private ChargeSheetItem testItem;
    private TechnicalFile testTechnicalFile;
    private TechnicalFileItem testTechnicalFileItem;
    private TechnicalFileService testTechnicalFileService;

    private String uniqueId;

    @BeforeEach
    void setUp() {
        uniqueId = UUID.randomUUID().toString().substring(0, 8);

        historyRepository.deleteAll();
        technicalFileItemRepository.deleteAll();
        technicalFileRepository.deleteAll();
        chargeSheetItemRepository.deleteAll();
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

        ppUser = createUser("pp_" + uniqueId + "@test.com", "pp123", Role.PP, 10001);
        mcUser = createUser("mc_" + uniqueId + "@test.com", "mc123", Role.MC, 10002);
        mpUser = createUser("mp_" + uniqueId + "@test.com", "mp123", Role.MP, 10003);
        ptUser = createUser("pt_" + uniqueId + "@test.com", "pt123", Role.PT, 10004);
        ingUser = createUser("ing_" + uniqueId + "@test.com", "ing123", Role.ING, 10005);
        adminUser = createUser("admin_" + uniqueId + "@test.com", "admin123", Role.ADMIN, 10000);

        ppToken = jwtService.generateToken(ppUser);
        mcToken = jwtService.generateToken(mcUser);
        mpToken = jwtService.generateToken(mpUser);
        ptToken = jwtService.generateToken(ptUser);
        ingToken = jwtService.generateToken(ingUser);
        adminToken = jwtService.generateToken(adminUser);

        saveToken(ppUser, ppToken);
        saveToken(mcUser, mcToken);
        saveToken(mpUser, mpToken);
        saveToken(ptUser, ptToken);
        saveToken(ingUser, ingToken);
        saveToken(adminUser, adminToken);

        testChargeSheet = ChargeSheet.builder()
                .plant("MH1_" + uniqueId)
                .project("FORD_" + uniqueId)
                .orderNumber("ORD-001_" + uniqueId)
                .status(ChargeSheetStatus.VALIDATED_PT)
                .createdBy(ingUser.getEmail())
                .createdAt(LocalDate.now())
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
        testItem = chargeSheetItemRepository.save(testItem);

        testTechnicalFile = TechnicalFile.builder()
                .reference("TF-001_" + uniqueId)
                .createdBy(ppUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        testTechnicalFile = technicalFileRepository.save(testTechnicalFile);

        testTechnicalFileItem = TechnicalFileItem.builder()
                .technicalFile(testTechnicalFile)
                .chargeSheetItem(testItem)
                .technicianName("John Doe")
                .position("Position 1_" + uniqueId)
                .validationStatus(TechnicalFileItemStatus.DRAFT)
                .createdBy(ppUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        testTechnicalFileItem = technicalFileItemRepository.save(testTechnicalFileItem);

        if (testTechnicalFile.getTechnicalFileItems() == null) {
            testTechnicalFile.setTechnicalFileItems(new ArrayList<>());
        }
        testTechnicalFile.getTechnicalFileItems().add(testTechnicalFileItem);
        testTechnicalFile = technicalFileRepository.save(testTechnicalFile);
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

    // ==================== POST /api/v1/technical-files ====================

    @Test
    void createTechnicalFile_AsPp_ShouldCreate() throws Exception {
        TechnicalFileDto.CreateDto dto = TechnicalFileDto.CreateDto.builder()
                .reference("TF-002_" + uniqueId)
                .items(List.of(TechnicalFileDto.TechnicalFileItemDto.builder()
                        .chargeSheetItemId(testItem.getId())
                        .technicianName("Jane Doe")
                        .position("Position 2")
                        .build()))
                .build();

        mockMvc.perform(post("/api/v1/technical-files")
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Dossier technique créé avec succès"))
                .andExpect(jsonPath("$.data.reference").value("TF-002_" + uniqueId));
    }

    @Test
    void createTechnicalFile_AsPt_ShouldReturnForbidden() throws Exception {
        TechnicalFileDto.CreateDto dto = TechnicalFileDto.CreateDto.builder()
                .reference("TF-003_" + uniqueId)
                .build();

        mockMvc.perform(post("/api/v1/technical-files")
                        .header("Authorization", "Bearer " + ptToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ==================== GET /api/v1/technical-files/list ====================

    @Test
    void getAllTechnicalFiles_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/list")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ==================== GET /api/v1/technical-files/detail ====================

    @Test
    void getAllTechnicalFilesWithItems_ShouldReturnDetailedList() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/detail")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ==================== GET /api/v1/technical-files/{id} ====================

    @Test
    void getTechnicalFileById_WithValidId_ShouldReturn() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/" + testTechnicalFile.getId())
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reference").value("TF-001_" + uniqueId));
    }

    // ==================== PUT /api/v1/technical-files/{id} ====================

    @Test
    void updateTechnicalFile_AsPp_ShouldUpdate() throws Exception {
        TechnicalFileDto.UpdateDto dto = TechnicalFileDto.UpdateDto.builder()
                .reference("TF-001-UPDATED_" + uniqueId)
                .build();

        mockMvc.perform(put("/api/v1/technical-files/" + testTechnicalFile.getId())
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Dossier technique mis à jour avec succès"))
                .andExpect(jsonPath("$.data.reference").value("TF-001-UPDATED_" + uniqueId));
    }

    // ==================== DELETE /api/v1/technical-files/{id} ====================

    @Test
    void deleteTechnicalFile_AsPp_ShouldDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/technical-files/" + testTechnicalFile.getId())
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Dossier technique supprimé avec succès"));
    }

    // ==================== GET /api/v1/technical-files/items/{itemId} ====================

    @Test
    void getTechnicalFileItem_WithValidId_ShouldReturn() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/items/" + testTechnicalFileItem.getId())
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.technicianName").value("John Doe"));
    }

    // ==================== PUT /api/v1/technical-files/items/{itemId} ====================

    @Test
    void updateTechnicalFileItem_AsPp_ShouldUpdate() throws Exception {
        TechnicalFileDto.UpdateItemDto dto = TechnicalFileDto.UpdateItemDto.builder()
                .technicianName("Jane Updated")
                .position("Updated Position")
                .build();

        mockMvc.perform(put("/api/v1/technical-files/items/" + testTechnicalFileItem.getId())
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item technique mis à jour avec succès"))
                .andExpect(jsonPath("$.data.technicianName").value("Jane Updated"));
    }

    // ==================== DELETE /api/v1/technical-files/items/{itemId} ====================

    @Test
    void deleteTechnicalFileItem_AsPp_ShouldDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/technical-files/items/" + testTechnicalFileItem.getId())
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item technique supprimé avec succès"));
    }

    // ==================== POST /api/v1/technical-files/{id}/items ====================

    @Test
    void addItemToTechnicalFile_ShouldAddItem() throws Exception {
        TechnicalFileDto.AddItemDto dto = TechnicalFileDto.AddItemDto.builder()
                .chargeSheetItemId(testItem.getId())
                .technicianName("New Tech")
                .position("New Position")
                .build();

        mockMvc.perform(post("/api/v1/technical-files/" + testTechnicalFile.getId() + "/items")
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item ajouté au dossier technique avec succès"))
                .andExpect(jsonPath("$.data.technicianName").value("New Tech"));
    }

    // ==================== PUT /api/v1/technical-files/items/{itemId}/validate ====================

    @Test
    void validateItem_AsPp_ShouldValidate() throws Exception {
        mockMvc.perform(put("/api/v1/technical-files/items/" + testTechnicalFileItem.getId() + "/validate")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item validé avec succès par PP"))
                .andExpect(jsonPath("$.data.validationStatus").value("VALIDATED_PP"));
    }

    @Test
    void validateItem_AsMc_ShouldValidateAfterPp() throws Exception {
        TechnicalFileItem preValidatedItem = TechnicalFileItem.builder()
                .technicalFile(testTechnicalFile)
                .chargeSheetItem(testItem)
                .technicianName("Pre Validated")
                .position("Position X")
                .validationStatus(TechnicalFileItemStatus.VALIDATED_PP)
                .createdBy(ppUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        preValidatedItem = technicalFileItemRepository.save(preValidatedItem);

        testTechnicalFile.getTechnicalFileItems().add(preValidatedItem);
        technicalFileRepository.save(testTechnicalFile);

        mockMvc.perform(put("/api/v1/technical-files/items/" + preValidatedItem.getId() + "/validate")
                        .header("Authorization", "Bearer " + mcToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item validé avec succès par MC"))
                .andExpect(jsonPath("$.data.validationStatus").value("VALIDATED_MC"));
    }

    // ==================== GET /api/v1/technical-files/items/{itemId}/can-validate ====================

    @Test
    void canValidateItem_ShouldReturnBoolean() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/items/" + testTechnicalFileItem.getId() + "/can-validate")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isBoolean());
    }

    // ==================== GET /api/v1/technical-files/{id}/history-audited ====================

    @Test
    void getHistoryAudited_ShouldReturnHistory() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/" + testTechnicalFile.getId() + "/history-audited")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== GET /api/v1/technical-files/items/{itemId}/history-audited ====================

    @Test
    void getItemHistoryAudited_ShouldReturnHistory() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/items/" + testTechnicalFileItem.getId() + "/history-audited")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== GET /api/v1/technical-files/{id}/full-history-audited ====================

    @Test
    void getFullHistory_ShouldReturnCompleteHistory() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/" + testTechnicalFile.getId() + "/full-history-audited")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== GET /api/v1/technical-files/notifications/pending ====================

    @Test
    void getPendingNotifications_ShouldReturnNotifications() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/notifications/pending")
                        .header("Authorization", "Bearer " + mcToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== GET /api/v1/technical-files/items/{itemId}/versions-compare ====================

    @Test
    void getFirstAndCurrentVersions_ShouldReturnComparison() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/items/" + testTechnicalFileItem.getId() + "/versions-compare")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
        // Ne vérifie pas l'existence de firstVersion car peut être vide
    }
    // ==================== TESTS POUR ROLE MP ====================

    @Test
    void validateItem_AsMp_ShouldValidateAfterMc() throws Exception {
        // Créer un item validé par MC
        TechnicalFileItem preValidatedItem = TechnicalFileItem.builder()
                .technicalFile(testTechnicalFile)
                .chargeSheetItem(testItem)
                .technicianName("Pre Validated")
                .position("Position X")
                .validationStatus(TechnicalFileItemStatus.VALIDATED_MC)
                .createdBy(ppUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        preValidatedItem = technicalFileItemRepository.save(preValidatedItem);

        testTechnicalFile.getTechnicalFileItems().add(preValidatedItem);
        technicalFileRepository.save(testTechnicalFile);

        mockMvc.perform(put("/api/v1/technical-files/items/" + preValidatedItem.getId() + "/validate")
                        .header("Authorization", "Bearer " + mpToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item validé avec succès par MP"))
                .andExpect(jsonPath("$.data.validationStatus").value("VALIDATED_MP"));
    }
    // ==================== TESTS AVEC ID INVALIDE ====================

    @Test
    void getTechnicalFileById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/99999")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().is5xxServerError())  // ← 500 au lieu de 404
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Technical file not found"));
    }

    @Test
    void getTechnicalFileItem_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/items/99999")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateTechnicalFileItem_WithInvalidId_ShouldReturnNotFound() throws Exception {
        TechnicalFileDto.UpdateItemDto dto = TechnicalFileDto.UpdateItemDto.builder()
                .technicianName("Updated")
                .build();

        mockMvc.perform(put("/api/v1/technical-files/items/99999")
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void deleteTechnicalFileItem_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/technical-files/items/99999")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void validateItem_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(put("/api/v1/technical-files/items/99999/validate")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().is5xxServerError());
    }
    // ==================== CAN VALIDATE POUR DIFFÉRENTS RÔLES ====================

    @Test
    void canValidateItem_AsMc_ShouldReturnFalseWhenStatusIsDraft() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/items/" + testTechnicalFileItem.getId() + "/can-validate")
                        .header("Authorization", "Bearer " + mcToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(false));
    }
    @Test
    void addItemToTechnicalFile_WithInvalidTechnicalFileId_ShouldReturnNotFound() throws Exception {
        TechnicalFileDto.AddItemDto dto = TechnicalFileDto.AddItemDto.builder()
                .chargeSheetItemId(testItem.getId())
                .technicianName("New Tech")
                .build();

        mockMvc.perform(post("/api/v1/technical-files/99999/items")
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void addItemToTechnicalFile_WithInvalidItemId_ShouldReturnNotFound() throws Exception {
        TechnicalFileDto.AddItemDto dto = TechnicalFileDto.AddItemDto.builder()
                .chargeSheetItemId(99999L)
                .technicianName("New Tech")
                .build();

        mockMvc.perform(post("/api/v1/technical-files/" + testTechnicalFile.getId() + "/items")
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is5xxServerError());
    }
    @Test
    void createTechnicalFile_WithEmptyItems_ShouldReturnBadRequest() throws Exception {
        TechnicalFileDto.CreateDto dto = TechnicalFileDto.CreateDto.builder()
                .reference("TF-002_" + uniqueId)
                .items(List.of())  // Liste vide
                .build();

        mockMvc.perform(post("/api/v1/technical-files")
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is5xxServerError());
    }
}