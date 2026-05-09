package com.example.security.TestUnitaire.service;

import com.example.security.config.LogoutService;
import com.example.security.token.Token;
import com.example.security.token.TokenRepository;
import com.example.security.token.TokenType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LogoutServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private LogoutService logoutService;

    private Token validToken;

    @BeforeEach
    void setUp() {
        validToken = Token.builder()
                .id(1)
                .token("valid-jwt-token-abc123")
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
    }

    // ==================== Tests logout avec token valide ====================

    @Test
    void logout_WithValidBearerToken_ShouldRevokeToken() {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-jwt-token-abc123");
        when(tokenRepository.findByToken("valid-jwt-token-abc123")).thenReturn(Optional.of(validToken));
        when(tokenRepository.save(any(Token.class))).thenReturn(validToken);

        logoutService.logout(request, response, authentication);

        assertThat(validToken.isExpired()).isTrue();
        assertThat(validToken.isRevoked()).isTrue();
        verify(tokenRepository).save(validToken);
    }

    @Test
    void logout_WithValidBearerToken_ShouldClearSecurityContext() {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-jwt-token-abc123");
        when(tokenRepository.findByToken("valid-jwt-token-abc123")).thenReturn(Optional.of(validToken));
        when(tokenRepository.save(any(Token.class))).thenReturn(validToken);

        logoutService.logout(request, response, authentication);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // ==================== Tests logout sans Authorization header ====================

    @Test
    void logout_WithNoAuthorizationHeader_ShouldDoNothing() {
        when(request.getHeader("Authorization")).thenReturn(null);

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, never()).findByToken(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void logout_WithEmptyAuthorizationHeader_ShouldDoNothing() {
        when(request.getHeader("Authorization")).thenReturn("");

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, never()).findByToken(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void logout_WithNonBearerToken_ShouldDoNothing() {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, never()).findByToken(any());
        verify(tokenRepository, never()).save(any());
    }

    // ==================== Tests logout token non trouvé en BDD ====================

    @Test
    void logout_WhenTokenNotInDatabase_ShouldDoNothing() {
        when(request.getHeader("Authorization")).thenReturn("Bearer unknown-token-xyz");
        when(tokenRepository.findByToken("unknown-token-xyz")).thenReturn(Optional.empty());

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, never()).save(any());
    }

    // ==================== Tests état du token après logout ====================

    @Test
    void logout_ShouldMarkTokenAsExpiredAndRevoked() {
        Token token = Token.builder()
                .id(2)
                .token("my-token-123")
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer my-token-123");
        when(tokenRepository.findByToken("my-token-123")).thenReturn(Optional.of(token));
        when(tokenRepository.save(any(Token.class))).thenReturn(token);

        logoutService.logout(request, response, authentication);

        assertThat(token.isExpired()).isTrue();
        assertThat(token.isRevoked()).isTrue();
    }

    @Test
    void logout_ShouldCallSaveOnce() {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-jwt-token-abc123");
        when(tokenRepository.findByToken("valid-jwt-token-abc123")).thenReturn(Optional.of(validToken));
        when(tokenRepository.save(any(Token.class))).thenReturn(validToken);

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, times(1)).save(validToken);
    }
}