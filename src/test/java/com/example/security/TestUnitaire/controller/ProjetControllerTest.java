package com.example.security.TestUnitaire.controller;

import com.example.security.common.ApiResponse;
import com.example.security.config.JwtService;
import com.example.security.projet.Projet;
import com.example.security.projet.ProjetDto;
import com.example.security.projet.ProjetRepository;
import com.example.security.projet.ProjetService;
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
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjetRepository projetRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @SpyBean  // ← Pour pouvoir spy le service
    private ProjetService projetService;

    private String adminToken;
    private String userToken;
    private User adminUser;
    private User normalUser;
    private Site testSite;
    private Projet testProjet1;
    private Projet testProjet2;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
        projetRepository.deleteAll();
        siteRepository.deleteAll();

        testProjet1 = new Projet();
        testProjet1.setName("FORD");
        testProjet1.setDescription("Projet Ford");
        testProjet1.setActive(true);
        testProjet1 = projetRepository.save(testProjet1);

        testProjet2 = new Projet();
        testProjet2.setName("TESLA");
        testProjet2.setDescription("Projet Tesla");
        testProjet2.setActive(true);
        testProjet2 = projetRepository.save(testProjet2);

        testSite = new Site();
        testSite.setName("MH1");
        testSite.setDescription("Site de Manzel Hayet");
        testSite.setActive(true);
        testSite.setProjets(new ArrayList<>(List.of(testProjet1)));
        testSite = siteRepository.save(testSite);

        testProjet1.setSites(List.of(testSite));
        testProjet1 = projetRepository.save(testProjet1);

        adminUser = User.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("admin123"))
                .firstname("Admin")
                .lastname("User")
                .matricule(10000)
                .role(Role.ADMIN)
                .site(testSite)
                .projets(Set.of(testProjet1))
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
                .email("user@test.com")
                .password(passwordEncoder.encode("user123"))
                .firstname("Normal")
                .lastname("User")
                .matricule(10001)
                .role(Role.ING)
                .site(testSite)
                .projets(Set.of(testProjet1))
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
    // ==================== GET /api/v1/projets ====================

    @Test
    void getAllProjets_ShouldReturnAllProjets() throws Exception {
        mockMvc.perform(get("/api/v1/projets")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Liste des projets récupérée avec succès"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("FORD"))
                .andExpect(jsonPath("$.data[1].name").value("TESLA"));
    }

    // ==================== GET /api/v1/projets/{id} ====================

    @Test
    void getProjetById_AsAdmin_ShouldReturnProjet() throws Exception {
        mockMvc.perform(get("/api/v1/projets/" + testProjet1.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testProjet1.getId()))
                .andExpect(jsonPath("$.data.name").value("FORD"));
    }

    @Test
    void getProjetById_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/projets/" + testProjet1.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProjetById_WithoutToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/projets/" + testProjet1.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProjetById_WithNonExistentId_ShouldReturnError() throws Exception {
        mockMvc.perform(get("/api/v1/projets/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Projet non trouvé"));
    }

    // ==================== POST /api/v1/projets ====================

    @Test
    void createProjet_AsAdmin_ShouldCreateProjet() throws Exception {
        ProjetDto newProjet = ProjetDto.builder()
                .name("BMW")
                .description("Projet BMW")
                .active(true)
                .siteIds(List.of(testSite.getId()))
                .build();

        mockMvc.perform(post("/api/v1/projets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProjet)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Projet créé avec succès"))
                .andExpect(jsonPath("$.data.name").value("BMW"));
    }

    @Test
    void createProjet_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        ProjetDto newProjet = ProjetDto.builder()
                .name("BMW")
                .description("Projet BMW")
                .active(true)
                .build();

        mockMvc.perform(post("/api/v1/projets")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProjet)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProjet_WithDuplicateName_ShouldReturnConflict() throws Exception {
        ProjetDto duplicateProjet = ProjetDto.builder()
                .name("FORD")
                .description("Duplicata")
                .active(true)
                .build();

        mockMvc.perform(post("/api/v1/projets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateProjet)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Un projet avec ce nom existe déjà"));
    }

    // ==================== PUT /api/v1/projets/{id} ====================

    @Test
    void updateProjet_AsAdmin_ShouldUpdateProjet() throws Exception {
        ProjetDto updateProjet = ProjetDto.builder()
                .name("FORD_UPDATED")
                .description("Description modifiée")
                .active(false)
                .siteIds(List.of(testSite.getId()))
                .build();

        mockMvc.perform(put("/api/v1/projets/" + testProjet1.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProjet)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Projet mis à jour avec succès"))
                .andExpect(jsonPath("$.data.name").value("FORD_UPDATED"));
    }

    @Test
    void updateProjet_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        ProjetDto updateProjet = ProjetDto.builder()
                .name("HACKED")
                .build();

        mockMvc.perform(put("/api/v1/projets/" + testProjet1.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProjet)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProjet_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        ProjetDto updateProjet = ProjetDto.builder()
                .name("Test")
                .build();

        mockMvc.perform(put("/api/v1/projets/99999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProjet)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Projet non trouvé"));
    }

    // ==================== DELETE /api/v1/projets/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProjet_AsAdmin_ShouldDeleteProjet() throws Exception {
        // ✅ SOLUTION : Créer un projet vraiment indépendant
        Projet independentProjet = new Projet();
        independentProjet.setName("Temp_" + System.currentTimeMillis());
        independentProjet.setDescription("Projet temporaire sans association");
        independentProjet.setActive(true);
        independentProjet.setSites(null);  // ← Forcer à null
        independentProjet = projetRepository.save(independentProjet);

        // Vérifier qu'il n'y a pas d'association
        System.out.println("=== PROJET CRÉÉ ===");
        System.out.println("ID: " + independentProjet.getId());
        System.out.println("Nom: " + independentProjet.getName());
        System.out.println("Sites: " + independentProjet.getSites());

        mockMvc.perform(delete("/api/v1/projets/" + independentProjet.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(result -> System.out.println("Response status: " + result.getResponse().getStatus()))
                .andDo(result -> System.out.println("Response body: " + result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Projet supprimé avec succès"));
    }

    @Test
    void deleteProjet_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/projets/" + testProjet1.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteProjet_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/projets/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Projet non trouvé"));
    }

    // ==================== GET /api/v1/projets/site/{siteName} ====================

    @Test
    void getProjetsBySiteName_AsAuthenticated_ShouldReturnProjets() throws Exception {
        mockMvc.perform(get("/api/v1/projets/site/MH1")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Liste des projets par site récupérée avec succès"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("FORD"));
    }

    @Test
    void getProjetsBySiteName_WithoutToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/projets/site/MH1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProjetsBySiteName_WithNonExistentSite_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/projets/site/NonExistentSite")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Site non trouvé: NonExistentSite"));
    }

    // ==================== GET /api/v1/projets/active ====================

    @Test
    void getActiveProjets_AsAuthenticated_ShouldReturnActiveProjets() throws Exception {
        mockMvc.perform(get("/api/v1/projets/active")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Liste des projets actifs récupérée avec succès"))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void getActiveProjets_WithoutToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/projets/active"))
                .andExpect(status().isForbidden());
    }
}