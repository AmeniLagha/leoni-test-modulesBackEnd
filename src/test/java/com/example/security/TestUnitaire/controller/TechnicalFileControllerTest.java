package com.example.security.TestUnitaire.controller;

import com.example.security.cahierdeCharge.*;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    @BeforeEach
    void setUp() {
        // Nettoyage
        historyRepository.deleteAll();
        technicalFileItemRepository.deleteAll();
        technicalFileRepository.deleteAll();
        chargeSheetItemRepository.deleteAll();
        chargeSheetRepository.deleteAll();
        tokenRepository.deleteAll();
        userRepository.deleteAll();
        siteRepository.deleteAll();
        projetRepository.deleteAll();

        // Création site
        testSite = new Site();
        testSite.setName("MH1");
        testSite.setActive(true);
        testSite = siteRepository.save(testSite);

        // Création projet
        testProjet = new Projet();
        testProjet.setName("FORD");
        testProjet.setActive(true);
        testProjet = projetRepository.save(testProjet);

        // Création utilisateurs
        ppUser = createUser("pp@test.com", "pp123", Role.PP, 10001);
        mcUser = createUser("mc@test.com", "mc123", Role.MC, 10002);
        mpUser = createUser("mp@test.com", "mp123", Role.MP, 10003);
        ptUser = createUser("pt@test.com", "pt123", Role.PT, 10004);
        ingUser = createUser("ing@test.com", "ing123", Role.ING, 10005);
        adminUser = createUser("admin@test.com", "admin123", Role.ADMIN, 10000);

        // Génération des tokens
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

        // Création cahier de test
        testChargeSheet = ChargeSheet.builder()
                .plant("MH1")
                .project("FORD")
                .orderNumber("ORD-001")
                .status(ChargeSheetStatus.VALIDATED_PT)
                .createdBy(ingUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        testChargeSheet = chargeSheetRepository.save(testChargeSheet);

        // Création item de test
        testItem = ChargeSheetItem.builder()
                .chargeSheet(testChargeSheet)
                .itemNumber("1")
                .quantityOfTestModules(5)
                .itemStatus("TECH_FILLED")
                .createdBy(ingUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        testItem = chargeSheetItemRepository.save(testItem);

        // Création dossier technique
        testTechnicalFile = TechnicalFile.builder()
                .reference("TF-001")
                .createdBy(ppUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        testTechnicalFile = technicalFileRepository.save(testTechnicalFile);

        // ✅ SUPPRIME L'INITIALISATION DE LA COLLECTION
        // if (testTechnicalFile.getTechnicalFileItems() == null) {
        //     testTechnicalFile.setTechnicalFileItems(new ArrayList<>());
        // }

        // Création item dossier technique - SANS ajouter à la collection manuellement
        testTechnicalFileItem = TechnicalFileItem.builder()
                .technicalFile(testTechnicalFile)
                .chargeSheetItem(testItem)
                .technicianName("John Doe")
                .position("Position 1")
                .validationStatus(TechnicalFileItemStatus.DRAFT)
                .createdBy(ppUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        testTechnicalFileItem = technicalFileItemRepository.save(testTechnicalFileItem);

        // ✅ AJOUTER L'ITEM À LA COLLECTION DU DOSSIER TECHNIQUE
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
                .reference("TF-002")
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reference").value("TF-002"));
    }

    @Test
    void createTechnicalFile_AsPt_ShouldReturnForbidden() throws Exception {
        TechnicalFileDto.CreateDto dto = TechnicalFileDto.CreateDto.builder()
                .reference("TF-003")
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
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ==================== GET /api/v1/technical-files/detail ====================

    @Test
    void getAllTechnicalFilesWithItems_ShouldReturnDetailedList() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/detail")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ==================== GET /api/v1/technical-files/{id} ====================

    @Test
    void getTechnicalFileById_WithValidId_ShouldReturn() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/" + testTechnicalFile.getId())
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reference").value("TF-001"));
    }

    // ==================== PUT /api/v1/technical-files/{id} ====================

    @Test
    void updateTechnicalFile_AsPp_ShouldUpdate() throws Exception {
        TechnicalFileDto.UpdateDto dto = TechnicalFileDto.UpdateDto.builder()
                .reference("TF-001-UPDATED")
                .build();

        mockMvc.perform(put("/api/v1/technical-files/" + testTechnicalFile.getId())
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reference").value("TF-001-UPDATED"));
    }

    // ==================== DELETE /api/v1/technical-files/{id} ====================

    @Test
    void deleteTechnicalFile_AsPp_ShouldDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/technical-files/" + testTechnicalFile.getId())
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isNoContent());
    }

    // ==================== GET /api/v1/technical-files/items/{itemId} ====================

    @Test
    void getTechnicalFileItem_WithValidId_ShouldReturn() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/items/" + testTechnicalFileItem.getId())
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.technicianName").value("John Doe"));
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
                .andExpect(jsonPath("$.technicianName").value("Jane Updated"));
    }

    // ==================== DELETE /api/v1/technical-files/items/{itemId} ====================

    @Test
    void deleteTechnicalFileItem_AsPp_ShouldDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/technical-files/items/" + testTechnicalFileItem.getId())
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isNoContent());
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.technicianName").value("New Tech"));
    }

    // ==================== PUT /api/v1/technical-files/items/{itemId}/validate ====================

    @Test
    void validateItem_AsPp_ShouldValidate() throws Exception {
        mockMvc.perform(put("/api/v1/technical-files/items/" + testTechnicalFileItem.getId() + "/validate")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validationStatus").value("VALIDATED_PP"));
    }

    @Test
    void validateItem_AsMc_ShouldValidateAfterPp() throws Exception {
        // Créer un item déjà validé par PP
        TechnicalFileItem preValidatedItem = TechnicalFileItem.builder()
                .technicalFile(testTechnicalFile)
                .chargeSheetItem(testItem)
                .technicianName("Pre Validated")
                .position("Position X")
                .validationStatus(TechnicalFileItemStatus.VALIDATED_PP)  // ← DÉJÀ VALIDÉ
                .createdBy(ppUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        preValidatedItem = technicalFileItemRepository.save(preValidatedItem);

        // Ajouter au dossier
        testTechnicalFile.getTechnicalFileItems().add(preValidatedItem);
        technicalFileRepository.save(testTechnicalFile);

        // Valider par MC
        mockMvc.perform(put("/api/v1/technical-files/items/" + preValidatedItem.getId() + "/validate")
                        .header("Authorization", "Bearer " + mcToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validationStatus").value("VALIDATED_MC"));
    }

    // ==================== GET /api/v1/technical-files/items/{itemId}/can-validate ====================

    @Test
    void canValidateItem_ShouldReturnBoolean() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/items/" + testTechnicalFileItem.getId() + "/can-validate")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk());
    }

    // ==================== GET /api/v1/technical-files/{id}/history-audited ====================

    @Test
    void getHistoryAudited_ShouldReturnHistory() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/" + testTechnicalFile.getId() + "/history-audited")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk());
    }

    // ==================== GET /api/v1/technical-files/items/{itemId}/history-audited ====================

    @Test
    void getItemHistoryAudited_ShouldReturnHistory() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/items/" + testTechnicalFileItem.getId() + "/history-audited")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk());
    }

    // ==================== GET /api/v1/technical-files/{id}/full-history-audited ====================

    @Test
    void getFullHistory_ShouldReturnCompleteHistory() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/" + testTechnicalFile.getId() + "/full-history-audited")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk());
    }

    // ==================== GET /api/v1/technical-files/notifications/pending ====================

    @Test
    void getPendingNotifications_ShouldReturnNotifications() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/notifications/pending")
                        .header("Authorization", "Bearer " + mcToken))
                .andExpect(status().isOk());
    }

    // ==================== GET /api/v1/technical-files/items/{itemId}/versions-compare ====================

    @Test
    void getFirstAndCurrentVersions_ShouldReturnComparison() throws Exception {
        mockMvc.perform(get("/api/v1/technical-files/items/" + testTechnicalFileItem.getId() + "/versions-compare")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk());
    }
}