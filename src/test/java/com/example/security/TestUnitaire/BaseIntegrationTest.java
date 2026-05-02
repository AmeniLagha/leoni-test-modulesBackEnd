package com.example.security.TestUnitaire;

import com.example.security.config.JwtService;
import com.example.security.token.Token;
import com.example.security.token.TokenRepository;
import com.example.security.token.TokenType;
import com.example.security.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public abstract class BaseIntegrationTest {

    @Autowired
    protected TokenRepository tokenRepository;

    @Autowired
    protected JwtService jwtService;

    @BeforeEach
    void cleanSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void cleanTokens() {
        tokenRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    protected String generateCleanToken(User user) {
        // Supprimer les anciens tokens
        List<Token> existingTokens = tokenRepository.findByUser(user);
        if (existingTokens != null && !existingTokens.isEmpty()) {
            tokenRepository.deleteAll(existingTokens);
            tokenRepository.flush();
        }

        String token = jwtService.generateToken(user);
        Token tokenEntity = Token.builder()
                .token(token)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .user(user)
                .build();
        tokenRepository.save(tokenEntity);

        return token;
    }
}
