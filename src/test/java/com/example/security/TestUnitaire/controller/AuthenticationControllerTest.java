package com.example.security.TestUnitaire.controller;

import com.example.security.auth.AuthenticationRequest;
import com.example.security.auth.RegisterRequest;
import com.example.security.projet.Projet;
import com.example.security.projet.ProjetRepository;
import com.example.security.site.Site;
import com.example.security.site.SiteRepository;
import com.example.security.token.TokenRepository;
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

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationControllerTest {

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

    private Site testSite;
    private Projet testProjet;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
        siteRepository.deleteAll();
        projetRepository.deleteAll();

        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        testSite = new Site();
        testSite.setName("MH1_" + uniqueId);
        testSite.setActive(true);
        testSite = siteRepository.save(testSite);

        testProjet = new Projet();
        testProjet.setName("FORD_" + uniqueId);
        testProjet = projetRepository.save(testProjet);
    }

    @Test
    void register_ShouldCreateUserAndReturnTokens() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("test_" + UUID.randomUUID() + "@example.com")
                .password("password123")
                .matricule((int)(Math.random() * 100000))
                .projets(List.of(testProjet.getName()))
                .role(Role.ING)
                .siteName(testSite.getName())
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Utilisateur créé avec succès"))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.access_token").exists())      // ✅ snake_case
                .andExpect(jsonPath("$.data.refresh_token").exists());    // ✅ snake_case
    }

    @Test
    void register_WithExistingEmail_ShouldReturnError() throws Exception {
        String uniqueEmail = "existing_" + UUID.randomUUID() + "@example.com";

        User existingUser = User.builder()
                .email(uniqueEmail)
                .password(passwordEncoder.encode("password"))
                .firstname("Existing")
                .lastname("User")
                .matricule(99999)
                .role(Role.ING)
                .site(testSite)
                .projets(new HashSet<>(List.of(testProjet)))
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = RegisterRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email(uniqueEmail)
                .password("password123")
                .matricule(12346)
                .projets(List.of(testProjet.getName()))
                .role(Role.ING)
                .siteName(testSite.getName())
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email déjà utilisé"))
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnTokens() throws Exception {
        String uniqueEmail = "auth_" + UUID.randomUUID() + "@example.com";

        User user = User.builder()
                .email(uniqueEmail)
                .password(passwordEncoder.encode("secret123"))
                .firstname("Auth")
                .lastname("User")
                .matricule(12347)
                .role(Role.ING)
                .site(testSite)
                .projets(new HashSet<>(List.of(testProjet)))
                .build();
        userRepository.save(user);

        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(uniqueEmail)
                .password("secret123")
                .siteName(testSite.getName())
                .build();

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Authentification réussie"))
                .andExpect(jsonPath("$.data.access_token").exists())     // ✅ snake_case
                .andExpect(jsonPath("$.data.refresh_token").exists());   // ✅ snake_case
    }

    @Test
    void authenticate_WithInvalidPassword_ShouldReturnUnauthorized() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("nonexistent_" + UUID.randomUUID() + "@example.com")
                .password("wrongpassword")
                .siteName(testSite.getName())
                .build();

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())  // ✅ 401 au lieu de 403 ou 500
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void authenticate_WithWrongSite_ShouldReturnError() throws Exception {
        String uniqueEmail = "siteuser_" + UUID.randomUUID() + "@example.com";

        User user = User.builder()
                .email(uniqueEmail)
                .password(passwordEncoder.encode("secret123"))
                .firstname("Site")
                .lastname("User")
                .matricule(12348)
                .role(Role.ING)
                .site(testSite)
                .projets(new HashSet<>(List.of(testProjet)))
                .build();
        userRepository.save(user);

        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(uniqueEmail)
                .password("secret123")
                .siteName("Wrong Site_" + UUID.randomUUID())
                .build();

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Vous n'avez pas accès à ce site"));
    }

    @Test
    void authenticate_AsAdmin_WithoutSite_ShouldWork() throws Exception {
        String uniqueEmail = "admin_" + UUID.randomUUID() + "@example.com";

        User admin = User.builder()
                .email(uniqueEmail)
                .password(passwordEncoder.encode("admin123"))
                .firstname("Admin")
                .lastname("User")
                .matricule(12349)
                .role(Role.ADMIN)
                .site(testSite)
                .projets(new HashSet<>(List.of(testProjet)))
                .build();
        userRepository.save(admin);

        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(uniqueEmail)
                .password("admin123")
                .siteName(null)
                .build();

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.access_token").exists());  // ✅ snake_case
    }

    @Test
    void authenticate_WithNoSiteForNonAdmin_ShouldReturnError() throws Exception {
        String uniqueEmail = "nosite_" + UUID.randomUUID() + "@example.com";

        User user = User.builder()
                .email(uniqueEmail)
                .password(passwordEncoder.encode("secret123"))
                .firstname("NoSite")
                .lastname("User")
                .matricule(12350)
                .role(Role.ING)
                .site(testSite)
                .projets(new HashSet<>(List.of(testProjet)))
                .build();
        userRepository.save(user);

        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(uniqueEmail)
                .password("secret123")
                .siteName(null)
                .build();

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Veuillez sélectionner un site"));
    }
}