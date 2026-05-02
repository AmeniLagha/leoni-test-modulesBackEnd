package com.example.security.TestUnitaire.service;

import com.example.security.auth.AuthenticationRequest;
import com.example.security.auth.AuthenticationResponse;
import com.example.security.auth.AuthenticationService;
import com.example.security.auth.RegisterRequest;
import com.example.security.config.JwtService;
import com.example.security.projet.Projet;
import com.example.security.projet.ProjetRepository;
import com.example.security.site.Site;
import com.example.security.site.SiteRepository;
import com.example.security.token.TokenRepository;
import com.example.security.user.Role;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private ProjetRepository projetRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private Site testSite;
    private Projet testProjet;
    private User testUser;

    @BeforeEach
    void setUp() {
        testSite = new Site();
        testSite.setId(1L);
        testSite.setName("Manzel Hayet");

        testProjet = new Projet();
        testProjet.setId(1L);
        testProjet.setName("LEONI-TEST");

        testUser = User.builder()
                .id(1)
                .email("test@example.com")
                .password("encodedPassword")
                .firstname("John")
                .lastname("Doe")
                .matricule(12345)
                .role(Role.ING)
                .site(testSite)
                .projets(Set.of(testProjet))
                .build();
    }

    @Test
    void register_ShouldCreateUserAndReturnTokens() {
        RegisterRequest request = RegisterRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("new@example.com")
                .password("password123")
                .matricule(12345)
                .projets(List.of("LEONI-TEST"))
                .role(Role.ING)
                .siteName("Manzel Hayet")
                .build();

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByMatricule(12345)).thenReturn(false);
        when(siteRepository.findByName("Manzel Hayet")).thenReturn(Optional.of(testSite));
        when(projetRepository.findByName("LEONI-TEST")).thenReturn(Optional.of(testProjet));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthenticationResponse response = authenticationService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");

        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
        verify(tokenRepository).save(any());
    }

    @Test
    void register_WithExistingEmail_ShouldThrowResponseStatusException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@example.com")
                .build();

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.CONFLICT, "Email déjà utilisé");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WithExistingMatricule_ShouldThrowResponseStatusException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("new@example.com")
                .matricule(99999)
                .build();

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByMatricule(99999)).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.CONFLICT, "Matricule déjà utilisé");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WithNonExistentSite_ShouldThrowResponseStatusException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("new@example.com")
                .matricule(12345)
                .siteName("NonExistentSite")
                .projets(List.of("LEONI-TEST"))
                .build();

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByMatricule(12345)).thenReturn(false);
        when(siteRepository.findByName("NonExistentSite")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.NOT_FOUND, "Site non trouvé: NonExistentSite"); // ✅ NOT_FOUND
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnTokens() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("password123")
                .siteName("Manzel Hayet")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("new-jwt-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("new-refresh-token");

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-jwt-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenRepository).findAllValidTokenByUser(testUser.getId());
    }

    @Test
    void authenticate_WithInvalidSite_ShouldThrowResponseStatusException() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("password123")
                .siteName("WrongSite")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authenticationService.authenticate(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à ce site");
    }

    @Test
    void authenticate_WithNoSiteForNonAdmin_ShouldThrowResponseStatusException() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("password123")
                .siteName(null)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authenticationService.authenticate(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status", "reason")
                .containsExactly(HttpStatus.BAD_REQUEST, "Veuillez sélectionner un site");
    }

    @Test
    void authenticate_AsAdmin_WithoutSite_ShouldWork() {
        User adminUser = User.builder()
                .id(2)
                .email("admin@example.com")
                .role(Role.ADMIN)
                .site(testSite)
                .build();

        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("admin@example.com")
                .password("admin123")
                .siteName(null)
                .build();

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(jwtService.generateToken(adminUser)).thenReturn("admin-jwt-token");
        when(jwtService.generateRefreshToken(adminUser)).thenReturn("admin-refresh-token");

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("admin-jwt-token");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void authenticate_WithNonExistentUser_ShouldThrowException() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("nonexistent@example.com")
                .password("password")
                .siteName("Manzel Hayet")
                .build();

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.authenticate(request))
                .isInstanceOf(RuntimeException.class);
    }
}