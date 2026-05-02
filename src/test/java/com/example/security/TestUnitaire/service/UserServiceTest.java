package com.example.security.TestUnitaire.service;

import com.example.security.projet.Projet;
import com.example.security.projet.ProjetRepository;
import com.example.security.site.Site;
import com.example.security.site.SiteRepository;
import com.example.security.token.TokenRepository;
import com.example.security.user.*;
import com.example.security.user.PasswordResetToken;
import com.example.security.user.PasswordResetTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private ProjetRepository projetRepository;

    @InjectMocks
    private UserService userService;

    private Site testSite;
    private Projet testProjet;
    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // Configuration de la valeur base-url
        ReflectionTestUtils.setField(userService, "baseUrl", "http://localhost:8080");

        testSite = new Site();
        testSite.setId(1L);
        testSite.setName("MH1");
        testSite.setActive(true);

        testProjet = new Projet();
        testProjet.setId(1L);
        testProjet.setName("FORD");

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

        adminUser = User.builder()
                .id(2)
                .email("admin@example.com")
                .password("encodedAdminPassword")
                .firstname("Admin")
                .lastname("User")
                .matricule(99999)
                .role(Role.ADMIN)
                .site(testSite)
                .projets(Set.of(testProjet))
                .build();
    }

    // ==================== getUserListWithPermissions ====================

    @Test
    void getUserListWithPermissions_ShouldReturnListOfUserDtos() {
        when(userRepository.findAll()).thenReturn(List.of(testUser, adminUser));

        List<UserDto> result = userService.getUserListWithPermissions();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo("test@example.com");
        assertThat(result.get(1).getEmail()).isEqualTo("admin@example.com");
        assertThat(result.get(0).getPermissions()).isNotEmpty();
        verify(userRepository).findAll();
    }

    @Test
    void getUserListWithPermissions_WhenRepositoryThrowsException_ShouldReturnEmptyList() {
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        List<UserDto> result = userService.getUserListWithPermissions();

        assertThat(result).isEmpty();
    }

    // ==================== deleteUser ====================

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteAndReturnTrue() {
        when(userRepository.existsById(1)).thenReturn(true);
        doNothing().when(tokenRepository).deleteByUserId(1);
        doNothing().when(userRepository).deleteById(1);

        boolean result = userService.deleteUser(1);

        assertThat(result).isTrue();
        verify(tokenRepository).deleteByUserId(1);
        verify(userRepository).deleteById(1);
    }

    @Test
    void deleteUser_WhenUserNotExists_ShouldReturnFalse() {
        when(userRepository.existsById(99)).thenReturn(false);

        boolean result = userService.deleteUser(99);

        assertThat(result).isFalse();
        verify(tokenRepository, never()).deleteByUserId(anyInt());
        verify(userRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteUser_WhenExceptionOccurs_ShouldReturnFalse() {
        when(userRepository.existsById(1)).thenReturn(true);
        doThrow(new RuntimeException("Database error")).when(tokenRepository).deleteByUserId(1);

        boolean result = userService.deleteUser(1);

        assertThat(result).isFalse();
    }

    // ==================== checkEmailExists ====================

    @Test
    void checkEmailExists_WhenEmailExists_ShouldReturnTrue() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        boolean result = userService.checkEmailExists("test@example.com");

        assertThat(result).isTrue();
    }

    @Test
    void checkEmailExists_WhenEmailNotExists_ShouldReturnFalse() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        boolean result = userService.checkEmailExists("nonexistent@example.com");

        assertThat(result).isFalse();
    }

    // ==================== checkMatriculeExists ====================

    @Test
    void checkMatriculeExists_WhenMatriculeExists_ShouldReturnTrue() {
        when(userRepository.existsByMatricule(12345)).thenReturn(true);

        boolean result = userService.checkMatriculeExists(12345);

        assertThat(result).isTrue();
    }

    @Test
    void checkMatriculeExists_WhenMatriculeNotExists_ShouldReturnFalse() {
        when(userRepository.existsByMatricule(99999)).thenReturn(false);

        boolean result = userService.checkMatriculeExists(99999);

        assertThat(result).isFalse();
    }

    // ==================== updateUser ====================

    @Test
    void updateUser_WhenUserExists_ShouldUpdateAndReturnDto() {
        UserDto updateRequest = new UserDto();
        updateRequest.setFirstname("Updated");
        updateRequest.setLastname("Name");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setMatricule(54321);
        updateRequest.setProjets(List.of("FORD"));
        updateRequest.setSite("MH1");
        updateRequest.setRole("ING");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(siteRepository.findByName("MH1")).thenReturn(Optional.of(testSite));
        when(projetRepository.findByName("FORD")).thenReturn(Optional.of(testProjet));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto result = userService.updateUser(1, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getFirstname()).isEqualTo("Updated");
        assertThat(result.getLastname()).isEqualTo("Name");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserNotExists_ShouldReturnNull() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        UserDto result = userService.updateUser(99, new UserDto());

        assertThat(result).isNull();
    }

    @Test
    void updateUser_WhenSiteNotFound_ShouldReturnNull() {
        UserDto updateRequest = new UserDto();
        updateRequest.setFirstname("Updated");
        updateRequest.setLastname("Name");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setMatricule(54321);
        updateRequest.setProjets(List.of("FORD"));
        updateRequest.setSite("NonExistentSite");
        updateRequest.setRole("ING");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(siteRepository.findByName("NonExistentSite")).thenReturn(Optional.empty());
        when(projetRepository.findByName("FORD")).thenReturn(Optional.of(testProjet));

        UserDto result = userService.updateUser(1, updateRequest);

        assertThat(result).isNull();
    }

    @Test
    void updateUser_WhenProjetNotFound_ShouldReturnNull() {
        UserDto updateRequest = new UserDto();
        updateRequest.setFirstname("Updated");
        updateRequest.setLastname("Name");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setMatricule(54321);
        updateRequest.setProjets(List.of("NonExistentProjet"));
        updateRequest.setSite("MH1");
        updateRequest.setRole("ING");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(siteRepository.findByName("MH1")).thenReturn(Optional.of(testSite));
        when(projetRepository.findByName("NonExistentProjet")).thenReturn(Optional.empty());

        UserDto result = userService.updateUser(1, updateRequest);

        assertThat(result).isNull();
    }

    // ==================== sendPasswordResetEmail ====================

    @Test
    void sendPasswordResetEmail_WhenUserExists_ShouldCreateTokenAndSendEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        doNothing().when(passwordResetTokenRepository).deleteByUserId(testUser.getId());
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        boolean result = userService.sendPasswordResetEmail("test@example.com");

        assertThat(result).isTrue();
        verify(passwordResetTokenRepository).deleteByUserId(testUser.getId());
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendPasswordResetEmail_WhenUserNotExists_ShouldReturnFalse() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        boolean result = userService.sendPasswordResetEmail("nonexistent@example.com");

        assertThat(result).isFalse();
        verify(passwordResetTokenRepository, never()).save(any());
    }

    @Test
    void sendPasswordResetEmail_WhenEmailSendingFails_ShouldStillReturnTrue() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        doNothing().when(passwordResetTokenRepository).deleteByUserId(testUser.getId());
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("Email error")).when(mailSender).send(any(SimpleMailMessage.class));

        boolean result = userService.sendPasswordResetEmail("test@example.com");

        assertThat(result).isTrue(); // On retourne true même si l'email échoue
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    // ==================== resetPassword ====================

    @Test
    void resetPassword_WithValidToken_ShouldResetPassword() {
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token("valid-token")
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(tokenRepository).deleteByUserId(testUser.getId());

        boolean result = userService.resetPassword("valid-token", "newPassword123");

        assertThat(result).isTrue();
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(tokenRepository).deleteByUserId(testUser.getId());
    }

    @Test
    void resetPassword_WithExpiredToken_ShouldReturnFalse() {
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .token("expired-token")
                .user(testUser)
                .expiryDate(LocalDateTime.now().minusHours(1))
                .used(false)
                .build();

        when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        boolean result = userService.resetPassword("expired-token", "newPassword123");

        assertThat(result).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_WithAlreadyUsedToken_ShouldReturnFalse() {
        PasswordResetToken usedToken = PasswordResetToken.builder()
                .token("used-token")
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(true)
                .build();

        when(passwordResetTokenRepository.findByToken("used-token")).thenReturn(Optional.of(usedToken));

        boolean result = userService.resetPassword("used-token", "newPassword123");

        assertThat(result).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_WithInvalidToken_ShouldReturnFalse() {
        when(passwordResetTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        boolean result = userService.resetPassword("invalid-token", "newPassword123");

        assertThat(result).isFalse();
        verify(userRepository, never()).save(any());
    }

    // ==================== validateResetToken ====================

    @Test
    void validateResetToken_WithValidToken_ShouldReturnTrue() {
        PasswordResetToken validToken = PasswordResetToken.builder()
                .token("valid-token")
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));

        boolean result = userService.validateResetToken("valid-token");

        assertThat(result).isTrue();
    }

    @Test
    void validateResetToken_WithExpiredToken_ShouldReturnFalse() {
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .token("expired-token")
                .expiryDate(LocalDateTime.now().minusHours(1))
                .used(false)
                .build();

        when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        boolean result = userService.validateResetToken("expired-token");

        assertThat(result).isFalse();
    }

    @Test
    void validateResetToken_WithUsedToken_ShouldReturnFalse() {
        PasswordResetToken usedToken = PasswordResetToken.builder()
                .token("used-token")
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(true)
                .build();

        when(passwordResetTokenRepository.findByToken("used-token")).thenReturn(Optional.of(usedToken));

        boolean result = userService.validateResetToken("used-token");

        assertThat(result).isFalse();
    }

    @Test
    void validateResetToken_WithInvalidToken_ShouldReturnFalse() {
        when(passwordResetTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        boolean result = userService.validateResetToken("invalid-token");

        assertThat(result).isFalse();
    }

    // ==================== changePassword ====================

    @Test
    void changePassword_WhenUserExists_ShouldChangePasswordAndDeleteTokens() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(tokenRepository).deleteByUserId(1);

        boolean result = userService.changePassword(1, "newPassword123");

        assertThat(result).isTrue();
        verify(passwordEncoder).encode("newPassword123");
        verify(tokenRepository).deleteByUserId(1);
    }

    @Test
    void changePassword_WhenUserNotExists_ShouldReturnFalse() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        boolean result = userService.changePassword(99, "newPassword123");

        assertThat(result).isFalse();
        verify(tokenRepository, never()).deleteByUserId(anyInt());
    }

    @Test
    void changePassword_WhenExceptionOccurs_ShouldReturnFalse() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123")).thenThrow(new RuntimeException("Encoding error"));

        boolean result = userService.changePassword(1, "newPassword123");

        assertThat(result).isFalse();
    }
}