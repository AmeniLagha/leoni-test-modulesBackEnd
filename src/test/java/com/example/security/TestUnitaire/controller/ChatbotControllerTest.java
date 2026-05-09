package com.example.security.TestUnitaire.controller;

import com.example.security.TestUnitaire.BaseIntegrationTest;
import com.example.security.ai.AIChatbotService;
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
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatbotControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    // Mock the Python AI service — it's not available in test environment
    @MockBean
    private AIChatbotService aiChatbotService;

    private String ingToken;
    private String ppToken;

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

        User ing = User.builder()
                .email("ing_" + uid + "@leoni.com")
                .password(passwordEncoder.encode("ing123"))
                .firstname("ING").lastname("User")
                .matricule(80001)
                .role(Role.ING)
                .site(site).projets(new HashSet<>())
                .build();
        ing = userRepository.save(ing);
        ingToken = jwtService.generateToken(ing);
        saveToken(ing, ingToken);

        User pp = User.builder()
                .email("pp_" + uid + "@leoni.com")
                .password(passwordEncoder.encode("pp123"))
                .firstname("PP").lastname("User")
                .matricule(80002)
                .role(Role.PP)
                .site(site).projets(new HashSet<>())
                .build();
        pp = userRepository.save(pp);
        ppToken = jwtService.generateToken(pp);
        saveToken(pp, ppToken);

        // Default mock response from Python IA
        when(aiChatbotService.getResponse(anyString()))
                .thenReturn("Voici ma réponse à votre question.");
    }

    private void saveToken(User user, String token) {
        tokenRepository.deleteAll(tokenRepository.findByUser(user));
        Token t = Token.builder()
                .token(token).tokenType(TokenType.BEARER)
                .expired(false).revoked(false).user(user)
                .build();
        tokenRepository.save(t);
    }

    // ==================== POST /api/v1/chatbot/ask ====================

    @Test
    void askQuestion_WithValidToken_ShouldReturnResponse() throws Exception {
        Map<String, String> body = Map.of("question", "Comment créer un cahier des charges ?");

        mockMvc.perform(post("/api/v1/chatbot/ask")
                        .header("Authorization", "Bearer " + ingToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Voici ma réponse à votre question."));
    }

    @Test
    void askQuestion_WithPpToken_ShouldReturnResponse() throws Exception {
        Map<String, String> body = Map.of("question", "Comment valider un cahier ?");

        mockMvc.perform(post("/api/v1/chatbot/ask")
                        .header("Authorization", "Bearer " + ppToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").exists());
    }

    @Test
    void askQuestion_WhenIaUnavailable_ShouldReturnFallback() throws Exception {
        when(aiChatbotService.getResponse(anyString()))
                .thenReturn("⚠️ Service IA temporairement indisponible. Veuillez réessayer plus tard.");

        Map<String, String> body = Map.of("question", "Question test");

        mockMvc.perform(post("/api/v1/chatbot/ask")
                        .header("Authorization", "Bearer " + ingToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(
                        "⚠️ Service IA temporairement indisponible. Veuillez réessayer plus tard."));
    }

    @Test
    void askQuestion_ShouldReturnResponseKey() throws Exception {
        Map<String, String> body = Map.of("question", "Test question");

        mockMvc.perform(post("/api/v1/chatbot/ask")
                        .header("Authorization", "Bearer " + ingToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isString());
    }
}