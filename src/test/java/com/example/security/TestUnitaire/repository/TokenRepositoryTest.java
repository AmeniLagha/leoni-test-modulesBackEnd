package com.example.security.TestUnitaire.repository;

import com.example.security.auth.AuthenticationService;
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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TokenRepositoryTest {
    @MockBean  // Add this to mock the AuthenticationService
    private AuthenticationService authenticationService;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private ProjetRepository projetRepository;

    private User testUser;
    private Token testToken;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
        siteRepository.deleteAll();
        projetRepository.deleteAll();

        Site testSite = new Site();
        testSite.setName("MH1");
        testSite.setActive(true);
        testSite = siteRepository.save(testSite);

        Projet testProjet = new Projet();
        testProjet.setName("FORD");
        testProjet.setActive(true);
        testProjet = projetRepository.save(testProjet);

        testUser = User.builder()
                .email("user@test.com")
                .password("password")
                .firstname("Test")
                .lastname("User")
                .matricule(12345)
                .role(Role.ING)
                .site(testSite)
                .projets(Set.of(testProjet))
                .build();
        testUser = userRepository.save(testUser);

        testToken = Token.builder()
                .token("test-token-123")
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .user(testUser)
                .build();
        testToken = tokenRepository.save(testToken);
    }

    @Test
    void findAllValidTokenByUser_ShouldReturnValidTokens() {
        List<Token> tokens = tokenRepository.findAllValidTokenByUser(testUser.getId());

        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getToken()).isEqualTo("test-token-123");
    }

    @Test
    void findByToken_ShouldReturnToken() {
        Token found = tokenRepository.findByToken("test-token-123").orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getToken()).isEqualTo("test-token-123");
    }

    @Test
    void findByToken_WithInvalidToken_ShouldReturnEmpty() {
        Token found = tokenRepository.findByToken("invalid-token").orElse(null);

        assertThat(found).isNull();
    }

    @Test
    void deleteByUserId_ShouldDeleteAllUserTokens() {
        Token secondToken = Token.builder()
                .token("test-token-456")
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .user(testUser)
                .build();
        tokenRepository.save(secondToken);

        assertThat(tokenRepository.findAllValidTokenByUser(testUser.getId())).hasSize(2);

        tokenRepository.deleteByUserId(testUser.getId());

        assertThat(tokenRepository.findAllValidTokenByUser(testUser.getId())).isEmpty();
    }
}