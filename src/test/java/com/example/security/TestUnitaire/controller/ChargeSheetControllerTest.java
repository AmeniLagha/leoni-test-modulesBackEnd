package com.example.security.TestUnitaire.controller;

import com.example.security.cahierdeCharge.*;
import com.example.security.config.JwtService;
import com.example.security.projet.Projet;
import com.example.security.projet.ProjetRepository;
import com.example.security.reception.ReceptionDto;
import com.example.security.reception.ReceptionHistory;
import com.example.security.reception.ReceptionHistoryRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ChargeSheetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChargeSheetRepository chargeSheetRepository;

    @Autowired
    private ChargeSheetItemRepository itemRepository;

    @Autowired
    private ReceptionHistoryRepository receptionHistoryRepository;

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

    private String ingToken;
    private String ptToken;
    private String adminToken;
    private User ingUser;
    private User ptUser;
    private User adminUser;
    private Site testSite;
    private Projet testProjet;
    private ChargeSheet testChargeSheet;
    private ChargeSheetItem testItem;
    private String uniqueId;

    @BeforeEach
    void setUp() {
        uniqueId = UUID.randomUUID().toString().substring(0, 8);

        receptionHistoryRepository.deleteAll();
        itemRepository.deleteAll();
        chargeSheetRepository.deleteAll();
        tokenRepository.deleteAll();
        userRepository.deleteAll();
        projetRepository.deleteAll();
        siteRepository.deleteAll();

        testSite = new Site();
        testSite.setName("MH1_" + uniqueId);
        testSite.setActive(true);
        testSite = siteRepository.save(testSite);

        testProjet = new Projet();
        testProjet.setName("FORD_" + uniqueId);
        testProjet.setActive(true);
        testProjet = projetRepository.save(testProjet);

        Set<Projet> projetsSet = new HashSet<>();
        projetsSet.add(testProjet);

        ingUser = User.builder()
                .email("ing_" + uniqueId + "@test.com")
                .password(passwordEncoder.encode("ing123"))
                .firstname("Ing")
                .lastname("User")
                .matricule(10001)
                .role(Role.ING)
                .site(testSite)
                .projets(projetsSet)
                .build();
        ingUser = userRepository.save(ingUser);
        ingToken = jwtService.generateToken(ingUser);

        projetsSet = new HashSet<>();
        projetsSet.add(testProjet);

        ptUser = User.builder()
                .email("pt_" + uniqueId + "@test.com")
                .password(passwordEncoder.encode("pt123"))
                .firstname("Pt")
                .lastname("User")
                .matricule(10002)
                .role(Role.PT)
                .site(testSite)
                .projets(projetsSet)
                .build();
        ptUser = userRepository.save(ptUser);
        ptToken = jwtService.generateToken(ptUser);

        projetsSet = new HashSet<>();
        projetsSet.add(testProjet);

        adminUser = User.builder()
                .email("admin_" + uniqueId + "@test.com")
                .password(passwordEncoder.encode("admin123"))
                .firstname("Admin")
                .lastname("User")
                .matricule(10000)
                .role(Role.ADMIN)
                .site(testSite)
                .projets(projetsSet)
                .build();
        adminUser = userRepository.save(adminUser);
        adminToken = jwtService.generateToken(adminUser);

        saveToken(ingUser, ingToken);
        saveToken(ptUser, ptToken);
        saveToken(adminUser, adminToken);

        testChargeSheet = ChargeSheet.builder()
                .plant(testSite.getName())
                .project(testProjet.getName())
                .harnessRef("HARNESS-001_" + uniqueId)
                .issuedBy("John Doe_" + uniqueId)
                .emailAddress("john_" + uniqueId + "@test.com")
                .phoneNumber("123456789_" + uniqueId)
                .orderNumber("ORD-001_" + uniqueId)
                .costCenterNumber("CC-001_" + uniqueId)
                .date(LocalDate.now())
                .preferredDeliveryDate(LocalDate.now().plusDays(30))
                .status(ChargeSheetStatus.DRAFT)
                .createdBy(ingUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        testChargeSheet = chargeSheetRepository.save(testChargeSheet);

        testItem = ChargeSheetItem.builder()
                .chargeSheet(testChargeSheet)
                .itemNumber("1_" + uniqueId)
                .samplesExist("Yes")
                .quantityOfTestModules(10)
                .itemStatus("DRAFT")
                .createdBy(ingUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        testItem = itemRepository.save(testItem);
        List<ChargeSheetItem> items = new ArrayList<>();
        items.add(testItem);
        testChargeSheet.setItems(items);
        testChargeSheet = chargeSheetRepository.save(testChargeSheet);
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

    // ==================== POST /api/v1/charge-sheets ====================

    @Test
    void createChargeSheet_AsIng_ShouldCreateSheet() throws Exception {
        ChargeSheetDto.CreateDto dto = ChargeSheetDto.CreateDto.builder()
                .project(testProjet.getName())
                .harnessRef("HARNESS-002_" + uniqueId)
                .issuedBy("Jane Doe_" + uniqueId)
                .emailAddress("jane_" + uniqueId + "@test.com")
                .phoneNumber("987654321_" + uniqueId)
                .orderNumber("ORD-002_" + uniqueId)
                .costCenterNumber("CC-002_" + uniqueId)
                .date(LocalDate.now())
                .preferredDeliveryDate(LocalDate.now().plusDays(30))
                .items(List.of(ChargeSheetDto.ItemDto.builder()
                        .itemNumber("1_" + uniqueId)
                        .samplesExist("Yes")
                        .quantityOfTestModules(5)
                        .build()))
                .build();

        mockMvc.perform(post("/api/v1/charge-sheets")
                        .header("Authorization", "Bearer " + ingToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())  // ✅ Changé: 201 au lieu de 200
                .andExpect(jsonPath("$.success").value(true))  // ✅ Nouveau
                .andExpect(jsonPath("$.message").value("Cahier des charges créé avec succès"))  // ✅ Nouveau
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-002_" + uniqueId));  // ✅ data. devant
    }

    @Test
    void createChargeSheet_AsPt_ShouldReturnForbidden() throws Exception {
        ChargeSheetDto.CreateDto dto = ChargeSheetDto.CreateDto.builder()
                .project(testProjet.getName())
                .build();

        mockMvc.perform(post("/api/v1/charge-sheets")
                        .header("Authorization", "Bearer " + ptToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));  // ✅ Vérifie que le corps est vide (pas de JSON)
        // Ne pas vérifier $.success car il n'y a pas de corps
    }

    // ==================== GET /api/v1/charge-sheets ====================

    @Test
    void getAllChargeSheets_AsIng_ShouldReturnSheets() throws Exception {
        mockMvc.perform(get("/api/v1/charge-sheets")
                        .header("Authorization", "Bearer " + ingToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ==================== GET /api/v1/charge-sheets/{id} ====================

    @Test
    void getChargeSheetById_WithValidId_ShouldReturnSheet() throws Exception {
        mockMvc.perform(get("/api/v1/charge-sheets/" + testChargeSheet.getId())
                        .header("Authorization", "Bearer " + ingToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-001_" + uniqueId));
    }

    // ==================== PUT /api/v1/charge-sheets/{id} ====================

    @Test
    void updateChargeSheet_AsIng_ShouldUpdateSheet() throws Exception {
        ChargeSheetDto.UpdateGeneralDto dto = ChargeSheetDto.UpdateGeneralDto.builder()
                .project(testProjet.getName())
                .harnessRef("HARNESS-UPDATED_" + uniqueId)
                .phoneNumber("111111111_" + uniqueId)
                .orderNumber("ORD-001_" + uniqueId)
                .costCenterNumber("CC-001_" + uniqueId)
                .date(LocalDate.now().toString())
                .preferredDeliveryDate(LocalDate.now().plusDays(30).toString())
                .build();

        mockMvc.perform(put("/api/v1/charge-sheets/" + testChargeSheet.getId())
                        .header("Authorization", "Bearer " + ingToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.harnessRef").value("HARNESS-UPDATED_" + uniqueId));
    }

    // ==================== PUT /api/v1/charge-sheets/{id}/validate-ing ====================

    @Test
    @Transactional
    @Rollback
    void validateByIng_WhenDraft_ShouldValidate() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(ingUser, null, ingUser.getAuthorities())
        );

        testChargeSheet = chargeSheetRepository.findById(testChargeSheet.getId()).orElseThrow();
        testItem = itemRepository.findById(testItem.getId()).orElseThrow();

        testChargeSheet.setStatus(ChargeSheetStatus.DRAFT);
        testChargeSheet = chargeSheetRepository.save(testChargeSheet);

        mockMvc.perform(put("/api/v1/charge-sheets/" + testChargeSheet.getId() + "/validate-ing")
                        .header("Authorization", "Bearer " + ingToken)
                        .with(request -> {
                            request.setUserPrincipal(() -> ingUser.getEmail());
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("VALIDATED_ING"));
    }

    // ==================== PUT /api/v1/charge-sheets/{id}/validate-pt ====================

    @Test
    void validateByPt_WhenTechFilled_ShouldValidate() throws Exception {
        testChargeSheet.setStatus(ChargeSheetStatus.TECH_FILLED);
        testChargeSheet = chargeSheetRepository.save(testChargeSheet);
        testItem.setItemStatus("TECH_FILLED");
        itemRepository.save(testItem);

        mockMvc.perform(put("/api/v1/charge-sheets/" + testChargeSheet.getId() + "/validate-pt")
                        .header("Authorization", "Bearer " + ptToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("VALIDATED_PT"));
    }

    // ==================== PUT /api/v1/charge-sheets/{id}/send-supplier ====================

    @Test
    void sendToSupplier_WhenValidatedPt_ShouldSend() throws Exception {
        testChargeSheet.setStatus(ChargeSheetStatus.VALIDATED_PT);
        testChargeSheet = chargeSheetRepository.save(testChargeSheet);

        mockMvc.perform(put("/api/v1/charge-sheets/" + testChargeSheet.getId() + "/send-supplier")
                        .header("Authorization", "Bearer " + ptToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SENT_TO_SUPPLIER"));
    }

    // ==================== POST /api/v1/charge-sheets/{sheetId}/items ====================

    @Test
    void addItem_AsIng_ShouldAddItem() throws Exception {
        ChargeSheetDto.ItemDto newItem = ChargeSheetDto.ItemDto.builder()
                .itemNumber("2_" + uniqueId)
                .samplesExist("Yes")
                .quantityOfTestModules(3)
                .build();

        mockMvc.perform(post("/api/v1/charge-sheets/" + testChargeSheet.getId() + "/items")
                        .header("Authorization", "Bearer " + ingToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items.length()").value(2));
    }

    // ==================== DELETE /api/v1/charge-sheets/{sheetId}/items/{itemId} ====================

    @Test
    void removeItem_AsIng_ShouldRemoveItem() throws Exception {
        mockMvc.perform(delete("/api/v1/charge-sheets/" + testChargeSheet.getId() + "/items/" + testItem.getId())
                        .header("Authorization", "Bearer " + ingToken))
                .andExpect(status().isOk())  // ✅ 200 au lieu de 204
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item supprimé avec succès"));
    }

    // ==================== PUT /api/v1/charge-sheets/{sheetId}/items/{itemId}/tech ====================

    @Test
    void updateItemTech_AsPt_ShouldUpdateItem() throws Exception {
        testChargeSheet.setStatus(ChargeSheetStatus.VALIDATED_ING);
        chargeSheetRepository.save(testChargeSheet);

        ChargeSheetDto.UpdateTechDto dto = ChargeSheetDto.UpdateTechDto.builder()
                .housingReferenceLeoni("REF-123_" + uniqueId)
                .quantityOfTestModules(15)
                .outsideHousingExist("*")
                .build();

        mockMvc.perform(put("/api/v1/charge-sheets/" + testChargeSheet.getId() + "/items/" + testItem.getId() + "/tech")
                        .header("Authorization", "Bearer " + ptToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.housingReferenceLeoni").value("REF-123_" + uniqueId));
    }

    // ==================== GET /api/v1/charge-sheets/stats ====================

    @Test
    void getDashboardStats_ShouldReturnStats() throws Exception {
        mockMvc.perform(get("/api/v1/charge-sheets/stats")
                        .header("Authorization", "Bearer " + ingToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userRole").value("ING"));
    }

    // ==================== DELETE /api/v1/charge-sheets/{id} ====================

    @Test
    void deleteChargeSheet_AsAdmin_ShouldDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/charge-sheets/" + testChargeSheet.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())  // ✅ 200 au lieu de 204
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cahier des charges supprimé avec succès"));
    }

    // ==================== GET /api/v1/charge-sheets/{id}/prepare-reception ====================

    @Test
    void prepareReception_WhenSentToSupplier_ShouldReturnData() throws Exception {
        testChargeSheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);
        chargeSheetRepository.save(testChargeSheet);

        mockMvc.perform(get("/api/v1/charge-sheets/" + testChargeSheet.getId() + "/prepare-reception")
                        .header("Authorization", "Bearer " + ptToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.chargeSheetId").value(testChargeSheet.getId()));
    }

    // ==================== POST /api/v1/charge-sheets/{id}/confirm-partial-reception ====================

    @Test
    void confirmPartialReception_ShouldRecordReception() throws Exception {
        testChargeSheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);
        chargeSheetRepository.save(testChargeSheet);

        ReceptionDto.ReceptionRequestDto request = ReceptionDto.ReceptionRequestDto.builder()
                .chargeSheetId(testChargeSheet.getId())
                .deliveryNoteNumber("DN-001_" + uniqueId)
                .receptionDate(LocalDate.now().toString())
                .items(List.of(ReceptionDto.ReceptionItemDto.builder()
                        .itemId(testItem.getId())
                        .quantityReceived(5)
                        .build()))
                .build();

        mockMvc.perform(post("/api/v1/charge-sheets/" + testChargeSheet.getId() + "/confirm-partial-reception")
                        .header("Authorization", "Bearer " + ptToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== GET /api/v1/charge-sheets/{id}/reception-history ====================

    @Test
    void getReceptionHistory_ShouldReturnHistory() throws Exception {
        mockMvc.perform(get("/api/v1/charge-sheets/" + testChargeSheet.getId() + "/reception-history")
                        .header("Authorization", "Bearer " + ptToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}