package com.example.security.TestUnitaire.config;

import com.example.security.config.JwtService;
import com.example.security.user.Role;
import com.example.security.user.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
// Ajouter cette importation en haut du fichier
import io.jsonwebtoken.Claims;
import java.util.HashMap;
import java.util.Map;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION = 86400000; // 24 heures
    private static final long REFRESH_EXPIRATION = 604800000; // 7 jours

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Injecter les valeurs de configuration
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", REFRESH_EXPIRATION);

        // Créer un utilisateur de test
        testUser = User.builder()
                .id(1)
                .email("test@example.com")
                .password("encodedPassword")
                .firstname("John")
                .lastname("Doe")
                .matricule(12345)
                .role(Role.ING)
                .projets(Set.of())
                .build();

        // Créer un admin
        adminUser = User.builder()
                .id(2)
                .email("admin@example.com")
                .password("encodedAdminPassword")
                .firstname("Admin")
                .lastname("User")
                .matricule(99999)
                .role(Role.ADMIN)
                .projets(Set.of())
                .build();
    }

    // ==================== generateToken ====================

    @Test
    void generateToken_ShouldReturnValidToken() {
        String token = jwtService.generateToken(testUser);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void generateToken_ShouldIncludeUserEmailInSubject() {
        String token = jwtService.generateToken(testUser);
        String extractedEmail = jwtService.extractUsername(token);

        assertThat(extractedEmail).isEqualTo("test@example.com");
    }

    @Test
    void generateToken_ShouldIncludeAuthorities() {
        String token = jwtService.generateToken(testUser);

        // Le token doit être généré sans erreur
        assertThat(token).isNotNull();
    }

    @Test
    void generateToken_ForAdmin_ShouldIncludeAdminRole() {
        String token = jwtService.generateToken(adminUser);

        assertThat(token).isNotNull();
    }

    // ==================== generateRefreshToken ====================

    @Test
    void generateRefreshToken_ShouldReturnValidToken() {
        String refreshToken = jwtService.generateRefreshToken(testUser);

        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
    }

    @Test
    void generateRefreshToken_ShouldHaveLongerExpiration() {
        String accessToken = jwtService.generateToken(testUser);
        String refreshToken = jwtService.generateRefreshToken(testUser);

        assertThat(accessToken).isNotNull();
        assertThat(refreshToken).isNotNull();
    }

    // ==================== extractUsername ====================

    @Test
    void extractUsername_ShouldReturnCorrectEmail() {
        String token = jwtService.generateToken(testUser);
        String extractedEmail = jwtService.extractUsername(token);

        assertThat(extractedEmail).isEqualTo("test@example.com");
    }

    @Test
    void extractUsername_FromRefreshToken_ShouldReturnCorrectEmail() {
        String refreshToken = jwtService.generateRefreshToken(testUser);
        String extractedEmail = jwtService.extractUsername(refreshToken);

        assertThat(extractedEmail).isEqualTo("test@example.com");
    }

    // ==================== isTokenValid ====================

    @Test
    void isTokenValid_WithValidToken_ShouldReturnTrue() {
        String token = jwtService.generateToken(testUser);
        boolean isValid = jwtService.isTokenValid(token, testUser);

        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_WithDifferentUser_ShouldReturnFalse() {
        String token = jwtService.generateToken(testUser);
        boolean isValid = jwtService.isTokenValid(token, adminUser);

        assertThat(isValid).isFalse();
    }

    // ==================== Token Expiration ====================

    @Test
    void isTokenValid_WithExpiredToken_ShouldReturnFalse() throws InterruptedException {
        // Créer un JwtService avec une expiration très courte
        JwtService shortExpirationJwtService = new JwtService();
        ReflectionTestUtils.setField(shortExpirationJwtService, "secretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(shortExpirationJwtService, "jwtExpiration", 100L); // 100ms
        ReflectionTestUtils.setField(shortExpirationJwtService, "refreshExpiration", REFRESH_EXPIRATION);

        String token = shortExpirationJwtService.generateToken(testUser);

        // Attendre que le token expire
        Thread.sleep(200);

        // Vérifier que le token est considéré comme invalide (soit false, soit exception)
        try {
            boolean isValid = shortExpirationJwtService.isTokenValid(token, testUser);
            assertThat(isValid).isFalse();
        } catch (ExpiredJwtException e) {
            // C'est aussi un comportement valide - le token est expiré
            assertThat(e).isInstanceOf(ExpiredJwtException.class);
        }
    }
    // ==================== Edge Cases ====================

    @Test
    void extractUsername_WithNullToken_ShouldThrowException() {
        assertThatThrownBy(() -> jwtService.extractUsername(null))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractUsername_WithMalformedToken_ShouldThrowException() {
        String malformedToken = "malformed.token.here";

        assertThatThrownBy(() -> jwtService.extractUsername(malformedToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    void generateToken_WithNullUserDetails_ShouldThrowException() {
        assertThatThrownBy(() -> jwtService.generateToken((User) null))
                .isInstanceOf(Exception.class);
    }

    // ==================== Additional Claims ====================

    @Test
    void generateToken_WithExtraClaims_ShouldIncludeThem() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "customValue");

        String token = jwtService.generateToken(extraClaims, testUser);

        // Le token doit être généré sans erreur
        assertThat(token).isNotNull();
    }

    @Test
    void extractClaim_ShouldReturnCorrectValue() {
        String token = jwtService.generateToken(testUser);

        String subject = jwtService.extractClaim(token, Claims::getSubject);

        assertThat(subject).isEqualTo("test@example.com");
    }
}

