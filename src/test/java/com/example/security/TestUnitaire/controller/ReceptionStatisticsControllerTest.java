package com.example.security.TestUnitaire.controller;

import com.example.security.TestUnitaire.BaseIntegrationTest;
import com.example.security.cahierdeCharge.*;
import com.example.security.config.JwtService;
import com.example.security.projet.Projet;
import com.example.security.projet.ProjetRepository;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReceptionStatisticsControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChargeSheetRepository chargeSheetRepository;

    @Autowired
    private ChargeSheetItemRepository chargeSheetItemRepository;

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

    private String adminToken;
    private String ingToken;
    private String ppToken;
    private Site testSite;
    private Projet testProjet;

    @BeforeEach
    void setUp() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        receptionHistoryRepository.deleteAll();
        chargeSheetItemRepository.deleteAll();
        chargeSheetRepository.deleteAll();
        tokenRepository.deleteAll();
        userRepository.deleteAll();
        siteRepository.deleteAll();
        projetRepository.deleteAll();

        testSite = new Site();
        testSite.setName("MH1_" + uid);
        testSite.setActive(true);
        testSite = siteRepository.save(testSite);

        testProjet = new Projet();
        testProjet.setName("FORD_" + uid);
        testProjet.setActive(true);
        testProjet = projetRepository.save(testProjet);

        User admin = createUser("admin_" + uid + "@leoni.com", Role.ADMIN, 77001);
        User ing = createUser("ing_" + uid + "@leoni.com", Role.ING, 77002);
        User pp = createUser("pp_" + uid + "@leoni.com", Role.PP, 77003);

        adminToken = jwtService.generateToken(admin);
        ingToken = jwtService.generateToken(ing);
        ppToken = jwtService.generateToken(pp);

        saveToken(admin, adminToken);
        saveToken(ing, ingToken);
        saveToken(pp, ppToken);

        // Create a ChargeSheet with items and receptions for stats
        ChargeSheet sheet = ChargeSheet.builder()
                .plant(testSite.getName())
                .project(testProjet.getName())
                .orderNumber("ORD-STAT-" + uid)
                .status(ChargeSheetStatus.RECEIVED_FROM_SUPPLIER)
                .createdBy(ing.getEmail())
                .createdAt(LocalDate.now())
                .build();
        sheet = chargeSheetRepository.save(sheet);

        ChargeSheetItem item = ChargeSheetItem.builder()
                .chargeSheet(sheet)
                .itemNumber("ITEM-001")
                .quantityOfTestModules(10)
                .build();
        item = chargeSheetItemRepository.save(item);

        ReceptionHistory reception = ReceptionHistory.builder()
                .item(item)
                .quantityReceived(5)
                .receptionDate(LocalDate.now().minusDays(3))
                .createdAt(LocalDate.now().minusDays(3))
                .receivedBy(pp.getEmail())
                .build();
        receptionHistoryRepository.save(reception);
    }

    private User createUser(String email, Role role, int matricule) {
        Set<Projet> projets = new HashSet<>();
        projets.add(testProjet);
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode("pass123"))
                .firstname("Test").lastname("User")
                .matricule(matricule)
                .role(role)
                .site(testSite).projets(projets)
                .build();
        return userRepository.save(user);
    }

    private void saveToken(User user, String token) {
        tokenRepository.deleteAll(tokenRepository.findByUser(user));
        Token t = Token.builder()
                .token(token).tokenType(TokenType.BEARER)
                .expired(false).revoked(false).user(user)
                .build();
        tokenRepository.save(t);
    }

    // ==================== GET /api/charge-sheets/statistics/receptions ====================

    @Test
    void getReceptionStatistics_AsAdmin_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/charge-sheets/statistics/receptions")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getReceptionStatistics_AsIng_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/charge-sheets/statistics/receptions")
                        .header("Authorization", "Bearer " + ingToken))
                .andExpect(status().isOk());
    }

    @Test
    void getReceptionStatistics_AsPp_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/charge-sheets/statistics/receptions")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk());
    }

    @Test
    void getReceptionStatistics_WithProjectFilter_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/charge-sheets/statistics/receptions")
                        .param("project", testProjet.getName())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getReceptionStatistics_WithSiteFilter_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/charge-sheets/statistics/receptions")
                        .param("site", testSite.getName())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getReceptionStatistics_WithMonthsParam_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/charge-sheets/statistics/receptions")
                        .param("months", "6")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getReceptionStatistics_WithAllParams_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/charge-sheets/statistics/receptions")
                        .param("project", testProjet.getName())
                        .param("site", testSite.getName())
                        .param("months", "12")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getReceptionStatistics_WithDefaultMonths_ShouldReturnOk() throws Exception {
        // Default is 12 months
        mockMvc.perform(get("/api/charge-sheets/statistics/receptions")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getReceptionStatistics_WithoutToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/charge-sheets/statistics/receptions"))
                .andExpect(status().isForbidden());
    }
}