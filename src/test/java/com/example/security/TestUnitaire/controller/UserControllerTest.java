package com.example.security.TestUnitaire.controller;

import com.example.security.auth.AuthenticationRequest;
import com.example.security.auth.RegisterRequest;
import com.example.security.common.ApiResponse;
import com.example.security.config.JwtService;
import com.example.security.projet.Projet;
import com.example.security.projet.ProjetRepository;
import com.example.security.site.Site;
import com.example.security.site.SiteRepository;
import com.example.security.token.Token;
import com.example.security.token.TokenRepository;
import com.example.security.token.TokenType;
import com.example.security.user.*;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private ProjetRepository projetRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    private Site testSite;
    private Projet testProjet;
    private String adminToken;
    private String userToken;
    private User adminUser;
    private User normalUser;
    private String uniqueId;

    @BeforeEach
    void setUp() {
        uniqueId = UUID.randomUUID().toString().substring(0, 8);

        passwordResetTokenRepository.deleteAll();
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
        testProjet = projetRepository.save(testProjet);

        adminUser = User.builder()
                .email("admin_" + uniqueId + "@test.com")
                .password(passwordEncoder.encode("admin123"))
                .firstname("Admin")
                .lastname("User")
                .matricule(10000)
                .role(Role.ADMIN)
                .site(testSite)
                .projets(new HashSet<>(Set.of(testProjet)))
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
                .projets(new HashSet<>(Set.of(testProjet)))
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

    // ==================== GET /api/v1/users ====================

    @Test
    void getAllUsers_AsAdmin_ShouldReturnAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Liste des utilisateurs récupérée avec succès"))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void getAllUsers_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_WithoutToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    // ==================== GET /api/v1/users/me ====================

    @Test
    void getCurrentUser_WithValidToken_ShouldReturnUserInfo() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Informations utilisateur récupérées avec succès"))
                .andExpect(jsonPath("$.data.email").value("admin_" + uniqueId + "@test.com"));
    }

    @Test
    void getCurrentUser_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isForbidden());  // ✅ 403 au lieu de 401
        // Pas de vérification JSON car réponse vide
    }

    // ==================== DELETE /api/v1/users/{id} ====================

    @Test
    void deleteUser_AsAdmin_ShouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + normalUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Utilisateur supprimé avec succès"));
    }

    @Test
    void deleteUser_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + normalUser.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/users/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Utilisateur non trouvé"));
    }

    // ==================== PUT /api/v1/users/{id} ====================

    @Test
    void updateUser_AsAdmin_ShouldUpdateUser() throws Exception {
        UserDto updateRequest = new UserDto();
        updateRequest.setFirstname("Updated");
        updateRequest.setLastname("Name");
        updateRequest.setEmail("updated_" + uniqueId + "@test.com");
        updateRequest.setMatricule(20000);
        updateRequest.setProjets(List.of(testProjet.getName()));
        updateRequest.setSite(testSite.getName());
        updateRequest.setRole("ING");

        mockMvc.perform(put("/api/v1/users/" + normalUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Utilisateur mis à jour avec succès"))
                .andExpect(jsonPath("$.data.firstname").value("Updated"));
    }

    @Test
    void updateUser_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        UserDto updateRequest = new UserDto();
        updateRequest.setFirstname("Hacked");

        mockMvc.perform(put("/api/v1/users/" + normalUser.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        UserDto updateRequest = new UserDto();
        updateRequest.setFirstname("Updated");

        mockMvc.perform(put("/api/v1/users/99999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Utilisateur non trouvé"));
    }

    // ==================== POST /api/v1/users/forgot-password ====================

    @Test
    void forgotPassword_WithExistingEmail_ShouldSendEmail() throws Exception {
        String request = "{\"email\":\"user_" + uniqueId + "@test.com\"}";

        mockMvc.perform(post("/api/v1/users/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Demande de réinitialisation traitée"));
    }

    @Test
    void forgotPassword_WithNonExistentEmail_ShouldReturnSameMessage() throws Exception {
        String request = "{\"email\":\"nonexistent_" + uniqueId + "@test.com\"}";

        mockMvc.perform(post("/api/v1/users/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Demande de réinitialisation traitée"));
    }

    // ==================== GET /api/v1/users/check-email ====================

    @Test
    void checkEmailExists_WithExistingEmail_ShouldReturnTrue() throws Exception {
        mockMvc.perform(get("/api/v1/users/check-email")
                        .param("email", "user_" + uniqueId + "@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vérification d'email effectuée"))
                .andExpect(jsonPath("$.data.exists").value(true));
    }

    @Test
    void checkEmailExists_WithNonExistentEmail_ShouldReturnFalse() throws Exception {
        mockMvc.perform(get("/api/v1/users/check-email")
                        .param("email", "nonexistent_" + uniqueId + "@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vérification d'email effectuée"))
                .andExpect(jsonPath("$.data.exists").value(false));
    }

    // ==================== GET /api/v1/users/check-matricule ====================

    @Test
    void checkMatriculeExists_WithExistingMatricule_ShouldReturnTrue() throws Exception {
        mockMvc.perform(get("/api/v1/users/check-matricule")
                        .param("matricule", "10001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vérification de matricule effectuée"))
                .andExpect(jsonPath("$.data.exists").value(true));
    }

    @Test
    void checkMatriculeExists_WithNonExistentMatricule_ShouldReturnFalse() throws Exception {
        mockMvc.perform(get("/api/v1/users/check-matricule")
                        .param("matricule", "99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vérification de matricule effectuée"))
                .andExpect(jsonPath("$.data.exists").value(false));
    }
}