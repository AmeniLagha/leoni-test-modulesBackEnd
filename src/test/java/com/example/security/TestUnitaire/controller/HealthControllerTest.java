package com.example.security.TestUnitaire.controller;

import com.example.security.TestUnitaire.BaseIntegrationTest;
import com.example.security.config.JwtService;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private ProjetRepository projetRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String adminToken;

    @BeforeEach
    void setUp() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        tokenRepository.deleteAll();
        userRepository.deleteAll();
        siteRepository.deleteAll();
        projetRepository.deleteAll();

        Site site = new Site();
        site.setName("MH1_" + uid);
        site.setActive(true);
        site = siteRepository.save(site);

        Projet projet = new Projet();
        projet.setName("FORD_" + uid);
        projet.setActive(true);
        projet = projetRepository.save(projet);

        User admin = User.builder()
                .email("admin_" + uid + "@leoni.com")
                .password(passwordEncoder.encode("admin123"))
                .firstname("Admin")
                .lastname("User")
                .matricule(99001)
                .role(Role.ADMIN)
                .site(site)
                .projets(new HashSet<>())
                .build();
        admin = userRepository.save(admin);

        adminToken = jwtService.generateToken(admin);
        Token tokenEntity = Token.builder()
                .token(adminToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .user(admin)
                .build();
        tokenRepository.save(tokenEntity);
    }

    // ==================== GET /api/v1/health ====================

    @Test
    void health_ShouldReturnStatusUp() throws Exception {
        mockMvc.perform(get("/api/v1/health")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Leoni Backend API"));
    }

    @Test
    void health_ShouldReturnDatabaseInfo() throws Exception {
        mockMvc.perform(get("/api/v1/health")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.database").exists())
                .andExpect(jsonPath("$.database.status").value("UP"));
    }

    @Test
    void health_ShouldReturnTimestamp() throws Exception {
        mockMvc.perform(get("/api/v1/health")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void health_ShouldReturnHealthyTrue() throws Exception {
        mockMvc.perform(get("/api/v1/health")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthy").value(true));
    }

    @Test
    void health_ShouldReturnVersion() throws Exception {
        mockMvc.perform(get("/api/v1/health")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }

}