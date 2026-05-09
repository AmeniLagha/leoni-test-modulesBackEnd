package com.example.security.TestUnitaire.controller;

import com.example.security.TestUnitaire.BaseIntegrationTest;
import com.example.security.cahierdeCharge.ChargeSheet;
import com.example.security.cahierdeCharge.ChargeSheetRepository;
import com.example.security.cahierdeCharge.ChargeSheetStatus;
import com.example.security.config.JwtService;
import com.example.security.projet.Projet;
import com.example.security.projet.ProjetRepository;
import com.example.security.reclamation.*;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClaimControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ChargeSheetRepository chargeSheetRepository;

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
    private Claim testClaim;

    @BeforeEach
    void setUp() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        claimRepository.deleteAll();
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
                .plant(testSite.getName())
                .project(testProjet.getName())
                .orderNumber("ORD-001_" + uniqueId)
                .status(ChargeSheetStatus.VALIDATED_PT)
                .createdBy(ppUser.getEmail())
                .createdAt(LocalDate.now())
                .build();
        testChargeSheet = chargeSheetRepository.save(testChargeSheet);

        testClaim = Claim.builder()
                .chargeSheetId(testChargeSheet.getId())
                .title("Test Claim_" + uniqueId)
                .description("This is a test claim for unit testing")
                .priority(Claim.Priority.HIGH)
                .category("TEST")
                .status(Claim.ClaimStatus.ASSIGNED)
                .reportedBy(ppUser.getEmail())
                .reportedDate(LocalDate.now())
                .assignedTo(mcUser.getEmail())
                .assignedDate(LocalDate.now())
                .createdBy(ppUser.getEmail())
                .createdAt(LocalDate.now())
                .plant(testSite.getName())
                .build();
        testClaim = claimRepository.save(testClaim);
    }

    private User createUser(String email, String password, Role role, int matricule) {
        Set<Projet> projetsSet = new HashSet<>();
        projetsSet.add(testProjet);

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstname("Test")
                .lastname("User")
                .matricule(matricule)
                .role(role)
                .site(testSite)
                .projets(projetsSet)
                .build();
        return userRepository.save(user);
    }

    private void saveToken(User user, String token) {
        tokenRepository.deleteAll(tokenRepository.findByUser(user));
        Token tokenEntity = Token.builder()
                .token(token)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .user(user)
                .build();
        tokenRepository.save(tokenEntity);
    }

    // ==================== POST /api/v1/claims ====================

    @Test
    void createClaim_AsPp_ShouldCreate() throws Exception {
        ClaimDto.CreateDto dto = ClaimDto.CreateDto.builder()
                .chargeSheetId(testChargeSheet.getId())
                .title("New Claim")
                .description("Description of new claim")
                .priority(Claim.Priority.MEDIUM)
                .category("HARDWARE")
                .assignedTo(mcUser.getEmail())
                .plant("MH1")
                .build();

        mockMvc.perform(post("/api/v1/claims")
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Réclamation créée avec succès"))
                .andExpect(jsonPath("$.data.title").value("New Claim"));
    }

    @Test
    void createClaim_AsIng_ShouldReturnForbidden() throws Exception {
        ClaimDto.CreateDto dto = ClaimDto.CreateDto.builder()
                .chargeSheetId(testChargeSheet.getId())
                .title("New Claim")
                .description("Description")
                .build();

        mockMvc.perform(post("/api/v1/claims")
                        .header("Authorization", "Bearer " + ingToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ==================== GET /api/v1/claims ====================

    @Test
    void getAllClaims_AsAdmin_ShouldReturnAll() throws Exception {
        mockMvc.perform(get("/api/v1/claims")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getAllClaims_AsPp_ShouldReturnFiltered() throws Exception {
        mockMvc.perform(get("/api/v1/claims")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== GET /api/v1/claims/{id} ====================


    @Test
    void getClaimById_WithInvalidId_ShouldReturnError() throws Exception {
        mockMvc.perform(get("/api/v1/claims/99999")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Claim not found"));
    }

    // ==================== PUT /api/v1/claims/{id} ====================

    @Test
    void updateClaim_AsPp_ShouldUpdate() throws Exception {
        ClaimDto.UpdateDto dto = ClaimDto.UpdateDto.builder()
                .title("Updated Title")
                .description("Updated description")
                .priority(Claim.Priority.CRITICAL)
                .build();

        mockMvc.perform(put("/api/v1/claims/" + testClaim.getId())
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Réclamation mise à jour avec succès"))
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    // ==================== PUT /api/v1/claims/{id}/assign ====================

    @Test
    void assignClaim_ShouldAssignAndNotify() throws Exception {
        ClaimDto.AssignmentDto dto = ClaimDto.AssignmentDto.builder()
                .assignedTo(mpUser.getEmail())
                .estimatedResolutionDate(LocalDate.now().plusDays(7))
                .build();

        mockMvc.perform(put("/api/v1/claims/" + testClaim.getId() + "/assign")
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Réclamation assignée avec succès"))
                .andExpect(jsonPath("$.data.assignedTo").value(mpUser.getEmail()));
    }

    // ==================== PUT /api/v1/claims/{id}/resolve ====================

    @Test
    void resolveClaim_ShouldResolve() throws Exception {
        ClaimDto.ResolutionDto dto = ClaimDto.ResolutionDto.builder()
                .actionTaken("Fixed the issue")
                .resolution("Replaced faulty component")
                .build();

        mockMvc.perform(put("/api/v1/claims/" + testClaim.getId() + "/resolve")
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Réclamation résolue avec succès"))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));
    }

    // ==================== PATCH /api/v1/claims/{id}/status/{status} ====================

    @Test
    void updateClaimStatus_ShouldChangeStatus() throws Exception {
        mockMvc.perform(patch("/api/v1/claims/" + testClaim.getId() + "/status/IN_PROGRESS")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Statut de la réclamation mis à jour avec succès"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    // ==================== GET /api/v1/claims/charge-sheet/{chargeSheetId} ====================

    @Test
    void getClaimsByChargeSheet_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/v1/claims/charge-sheet/" + testChargeSheet.getId())
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ==================== GET /api/v1/claims/my-reported ====================

    @Test
    void getMyReportedClaims_ShouldReturnClaims() throws Exception {
        mockMvc.perform(get("/api/v1/claims/my-reported")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== GET /api/v1/claims/priority/{priority} ====================

    @Test
    void getClaimsByPriority_ShouldReturnFiltered() throws Exception {
        mockMvc.perform(get("/api/v1/claims/priority/HIGH")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== GET /api/v1/claims/category/{category} ====================

    @Test
    void getClaimsByCategory_ShouldReturnFiltered() throws Exception {
        mockMvc.perform(get("/api/v1/claims/category/TEST")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== GET /api/v1/claims/search ====================

    @Test
    void searchClaims_ShouldReturnResults() throws Exception {
        mockMvc.perform(get("/api/v1/claims/search")
                        .param("keyword", "Test")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== GET /api/v1/claims/summary/{chargeSheetId} ====================

    @Test
    void getClaimSummary_ShouldReturnStats() throws Exception {
        mockMvc.perform(get("/api/v1/claims/summary/" + testChargeSheet.getId())
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    // ==================== DELETE /api/v1/claims/{id} ====================

    @Test
    void deleteClaim_AsPp_ShouldDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/claims/" + testClaim.getId())
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Réclamation supprimée avec succès"));
    }

    @Test
    void deleteClaim_WrongUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/claims/" + testClaim.getId())
                        .header("Authorization", "Bearer " + ingToken))
                .andExpect(status().isForbidden());
    }
}