package com.example.security.TestUnitaire.controller;

import com.example.security.common.ApiResponse;
import com.example.security.config.JwtService;
import com.example.security.projet.Projet;
import com.example.security.projet.ProjetRepository;
import com.example.security.site.Site;
import com.example.security.site.SiteDto;
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

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SiteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private String userToken;
    private User adminUser;
    private User normalUser;
    private Site testSite;
    private Projet testProjet1;
    private Projet testProjet2;

    private String uniqueId;

    @BeforeEach
    void setUp() {
        uniqueId = UUID.randomUUID().toString().substring(0, 8);

        tokenRepository.deleteAll();
        userRepository.deleteAll();

        List<Site> allSites = siteRepository.findAll();
        for (Site site : allSites) {
            site.getProjets().clear();
            siteRepository.save(site);
        }

        siteRepository.deleteAll();
        projetRepository.deleteAll();

        testProjet1 = new Projet();
        testProjet1.setName("FORD_" + uniqueId);
        testProjet1.setActive(true);
        testProjet1 = projetRepository.save(testProjet1);

        testProjet2 = new Projet();
        testProjet2.setName("TESLA_" + uniqueId);
        testProjet2.setActive(true);
        testProjet2 = projetRepository.save(testProjet2);

        testSite = new Site();
        testSite.setName("MH1_" + uniqueId);
        testSite.setDescription("Site de Manzel Hayet");
        testSite.setActive(true);
        testSite.setProjets(new ArrayList<>(List.of(testProjet1)));
        testSite = siteRepository.save(testSite);

        adminUser = User.builder()
                .email("admin_" + uniqueId + "@test.com")
                .password(passwordEncoder.encode("admin123"))
                .firstname("Admin")
                .lastname("User")
                .matricule(10000)
                .role(Role.ADMIN)
                .site(testSite)
                .projets(new HashSet<>(Set.of(testProjet1)))
                .build();
        adminUser = userRepository.save(adminUser);
        adminToken = jwtService.generateToken(adminUser);

        Token adminTokenEntity = Token.builder()
                .token(adminToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .user(adminUser)
                .build();
        tokenRepository.save(adminTokenEntity);

        normalUser = User.builder()
                .email("user_" + uniqueId + "@test.com")
                .password(passwordEncoder.encode("user123"))
                .firstname("Normal")
                .lastname("User")
                .matricule(10001)
                .role(Role.ING)
                .site(testSite)
                .projets(new HashSet<>(Set.of(testProjet1)))
                .build();
        normalUser = userRepository.save(normalUser);
        userToken = jwtService.generateToken(normalUser);

        Token userTokenEntity = Token.builder()
                .token(userToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .user(normalUser)
                .build();
        tokenRepository.save(userTokenEntity);
    }

    // ==================== GET /api/v1/sites ====================

    @Test
    void getAllSites_ShouldReturnAllSites() throws Exception {
        mockMvc.perform(get("/api/v1/sites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Liste des sites récupérée avec succès"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("MH1_" + uniqueId));
    }

    // ==================== GET /api/v1/sites/{id} ====================

    @Test
    void getSiteById_AsAdmin_ShouldReturnSite() throws Exception {
        mockMvc.perform(get("/api/v1/sites/" + testSite.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testSite.getId()))
                .andExpect(jsonPath("$.data.name").value("MH1_" + uniqueId));
    }

    @Test
    void getSiteById_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/sites/" + testSite.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSiteById_WithoutToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/sites/" + testSite.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSiteById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/sites/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Site non trouvé"));
    }

    // ==================== POST /api/v1/sites ====================

    @Test
    void createSite_AsAdmin_ShouldCreateSite() throws Exception {
        SiteDto newSite = SiteDto.builder()
                .name("MH2_" + uniqueId)
                .description("Nouveau site")
                .active(true)
                .projetIds(List.of(testProjet1.getId()))
                .build();

        mockMvc.perform(post("/api/v1/sites")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSite)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Site créé avec succès"))
                .andExpect(jsonPath("$.data.name").value("MH2_" + uniqueId));
    }

    @Test
    void createSite_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        SiteDto newSite = SiteDto.builder()
                .name("MH2_" + uniqueId)
                .description("Nouveau site")
                .active(true)
                .build();

        mockMvc.perform(post("/api/v1/sites")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSite)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createSite_WithDuplicateName_ShouldReturnConflict() throws Exception {
        SiteDto duplicateSite = SiteDto.builder()
                .name("MH1_" + uniqueId)
                .description("Site en double")
                .active(true)
                .build();

        mockMvc.perform(post("/api/v1/sites")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateSite)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Un site avec ce nom existe déjà"));
    }

    // ==================== PUT /api/v1/sites/{id} ====================

    @Test
    void updateSite_AsAdmin_ShouldUpdateSite() throws Exception {
        SiteDto updateSite = SiteDto.builder()
                .name("MH1_UPDATED_" + uniqueId)
                .description("Description modifiée")
                .active(false)
                .projetIds(List.of(testProjet1.getId(), testProjet2.getId()))
                .build();

        mockMvc.perform(put("/api/v1/sites/" + testSite.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSite)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Site mis à jour avec succès"))
                .andExpect(jsonPath("$.data.name").value("MH1_UPDATED_" + uniqueId));
    }

    @Test
    void updateSite_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        SiteDto updateSite = SiteDto.builder()
                .name("HACKED_" + uniqueId)
                .build();

        mockMvc.perform(put("/api/v1/sites/" + testSite.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSite)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateSite_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        SiteDto updateSite = SiteDto.builder()
                .name("Test_" + uniqueId)
                .build();

        mockMvc.perform(put("/api/v1/sites/99999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSite)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Site non trouvé"));
    }

    // ==================== DELETE /api/v1/sites/{id} ====================

    @Test
    void deleteSite_AsAdmin_ShouldDeleteSite() throws Exception {
        Site tempSite = new Site();
        tempSite.setName("TempSite_" + uniqueId);
        tempSite.setDescription("Site temporaire pour suppression");
        tempSite.setActive(true);
        tempSite.setProjets(new ArrayList<>(List.of(testProjet1)));
        tempSite = siteRepository.save(tempSite);

        mockMvc.perform(delete("/api/v1/sites/" + tempSite.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Site supprimé avec succès"));
    }

    @Test
    void deleteSite_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/sites/" + testSite.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteSite_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/sites/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Site non trouvé"));
    }

    // ==================== GET /api/v1/sites/{siteId}/projets ====================

    @Test
    void getProjetsBySite_AsAuthenticated_ShouldReturnProjets() throws Exception {
        mockMvc.perform(get("/api/v1/sites/" + testSite.getId() + "/projets")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getProjetsBySite_WithoutToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/sites/" + testSite.getId() + "/projets"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProjetsBySite_WithNonExistentSite_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/sites/99999/projets")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Site non trouvé"));
    }

    // ==================== POST /api/v1/sites/{siteId}/add-projet/{projetId} ====================

    @Test
    void addProjetToSite_AsAdmin_ShouldAddProjet() throws Exception {
        mockMvc.perform(post("/api/v1/sites/" + testSite.getId() + "/add-projet/" + testProjet2.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Projet ajouté au site avec succès"));
    }

    @Test
    void addProjetToSite_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/sites/" + testSite.getId() + "/add-projet/" + testProjet2.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void addProjetToSite_WithNonExistentSite_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/sites/99999/add-projet/" + testProjet2.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Site non trouvé"));
    }

    // ==================== PUT /api/v1/sites/{siteId}/projets ====================

    @Test
    void updateSiteProjets_AsAdmin_ShouldUpdateProjets() throws Exception {
        Map<String, List<Long>> body = new HashMap<>();
        body.put("projetIds", List.of(testProjet2.getId()));

        mockMvc.perform(put("/api/v1/sites/" + testSite.getId() + "/projets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Projets du site mis à jour avec succès"));
    }

    @Test
    void updateSiteProjets_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        Map<String, List<Long>> body = new HashMap<>();
        body.put("projetIds", List.of(testProjet2.getId()));

        mockMvc.perform(put("/api/v1/sites/" + testSite.getId() + "/projets")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateSiteProjets_WithNonExistentSite_ShouldReturnNotFound() throws Exception {
        Map<String, List<Long>> body = new HashMap<>();
        body.put("projetIds", List.of(testProjet2.getId()));

        mockMvc.perform(put("/api/v1/sites/99999/projets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Site non trouvé"));
    }
}